.EXPORT_ALL_VARIABLES:

all: lint clean build test

build:
	docker compose run --rm mvn -B -e clean install
	docker compose pull --quiet kafka sbemulatorns sqledge
	docker compose build --quiet

changelog:
	docker run --quiet --rm --volume "${PWD}:/mnt/source" --workdir /mnt/source ghcr.io/cbdq-io/gitchangelog > CHANGELOG.md

clean:
	docker compose run --rm mvn -B clean
	docker compose down -t 0 --remove-orphans

get-lags:
	docker compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --all-groups

lint:
	docker run --rm -i hadolint/hadolint < Dockerfile
	yamllint -s .

osv:
	docker run -it -v ${PWD}:/src -w /src ghcr.io/google/osv-scanner:latest scan --config /src/osv-scanner.toml --recursive /src

tag:
	@docker compose run --quiet --rm mvn help:evaluate -Dexpression=project.version -q -DforceStdout

test:
	docker compose up -d kafka sqledge --wait
	docker compose run --rm emulators
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.infra.external.kafka_connect.default.config --config cleanup.policy=compact
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.infra.external.kafka_connect.default.offset --partitions 25 --config cleanup.policy=compact
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.infra.external.kafka_connect.default.status --partitions 5 --config cleanup.policy=compact
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.api.v1.accounts.account.created
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.api.v1.audit_logs.audit_log.created --partitions 4
	docker compose exec kafka kafka-configs --bootstrap-server kafka:29092 --alter --entity-type topics --entity-name vault.api.v1.audit_logs.audit_log.created --add-config max.message.bytes=4096000
	docker compose exec kafka /bin/sh -c "echo 'Hello, world!' | kafka-console-producer --topic vault.api.v1.audit_logs.audit_log.created --bootstrap-server kafka:29092"
	docker compose exec kafka /bin/sh -c "python /usr/local/bin/data-gen.py -c 1000 -s 12_700 -r 200_000 | kafka-console-producer --topic vault.api.v1.audit_logs.audit_log.created --bootstrap-server kafka:29092 --producer-property max.request.size=4096000 --property parse.key=true --property key.separator=:"
	docker compose up -d connect --wait
	docker compose up -d kccinit --wait
	tests/resources/poll-until-complete.sh

trivy:
	trivy image --severity HIGH,CRITICAL --ignore-unfixed kc-connectors:latest

update-trivy-ignore:
	trivy image --format json --ignore-unfixed --severity HIGH,CRITICAL kc-connectors:latest | jq -r '.Results[1].Vulnerabilities[].VulnerabilityID' | sort -u | tee .trivyignore

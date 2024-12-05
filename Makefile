.EXPORT_ALL_VARIABLES:

TAG = 0.1.2

all: lint clean build test

build:
	make -C azure-servicebus-sink-connector build
	docker compose pull --quiet artemis kafka
	docker compose build --quiet

changelog:
	gitchangelog > CHANGELOG.md

clean:
	make -C azure-servicebus-sink-connector clean
	docker compose down -t 0 --remove-orphans

get-lags:
	docker compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --all-groups

lint:
	docker run --rm -i hadolint/hadolint < Dockerfile
	yamllint -s .

osv:
	docker run -it -v ${PWD}:/src -w /src ghcr.io/google/osv-scanner:latest scan --config /src/osv-scanner.toml --recursive /src

tag:
	@echo $(TAG)

test:
	docker compose up -d kafka artemis --wait
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.infra.external.kafka_connect.default.config --config cleanup.policy=compact
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.infra.external.kafka_connect.default.offset --partitions 25 --config cleanup.policy=compact
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.infra.external.kafka_connect.default.status --partitions 5 --config cleanup.policy=compact
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.api.v1.accounts.account.created
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.api.v1.audit_logs.audit_log.created --partitions 4
	docker compose exec kafka kafka-configs --bootstrap-server kafka:29092 --alter --entity-type topics --entity-name vault.api.v1.audit_logs.audit_log.created --add-config max.message.bytes=4096000
	docker compose exec kafka /bin/sh -c "python /usr/local/bin/data-gen.py -c 40_000 -s 12_800 -r 4_000_000 | kafka-console-producer --topic vault.api.v1.audit_logs.audit_log.created --bootstrap-server kafka:29092 --producer-property max.request.size=4096000"
	docker compose exec artemis /var/lib/artemis-instance/bin/artemis queue create --user=artemis --password=artemis --name=vault.api.v1.accounts.account.created --silent --auto-create-address
	docker compose exec artemis /var/lib/artemis-instance/bin/artemis queue create --user=artemis --password=artemis --name=vault.api.v1.audit_logs.audit_log.created --silent --auto-create-address

	docker compose up -d connect --wait
	curl -X POST -H "Content-Type: application/json" \
		--data '{"name": "azure-servicebus-sink-connector", "config": {"azure.servicebus.connection.string": "Endpoint=amqp://artemis/;SharedAccessKeyName=artemis;SharedAccessKey=artemis", "connector.class": "io.cbdq.AzureServiceBusSinkConnector", "tasks.max": "1", "topics": "vault.api.v1.accounts.account.created,vault.api.v1.audit_logs.audit_log.created", "retry.max.attempts": "5", "retry.wait.time.ms": "1000", "value.converter": "org.apache.kafka.connect.converters.ByteArrayConverter", "key.converter": "org.apache.kafka.connect.converters.ByteArrayConverter", "consumer.override.auto.offset.reset": "earliest"}}' \
		http://localhost:8083/connectors

trivy:
	trivy image --severity HIGH,CRITICAL --ignore-unfixed kc-connectors:latest

update-trivy-ignore:
	trivy image --format json --ignore-unfixed --severity HIGH,CRITICAL kc-connectors:latest | jq -r '.Results[1].Vulnerabilities[].VulnerabilityID' | sort -u | tee .trivyignore

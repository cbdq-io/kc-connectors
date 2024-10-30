all: lint clean build test

build:
	make -C azure-servicebus-sink-connector build
	docker compose build connect

clean:
	make -C azure-servicebus-sink-connector clean
	docker compose down -t 0 --remove-orphans

get-lags:
	docker compose exec kafka kafka-consumer-groups --bootstrap-server localhost:9092 --describe --all-groups

lint:
	docker run --rm -i hadolint/hadolint < Dockerfile

test:
	docker compose up -d kafka localhost.localsandbox.sh --wait
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.infra.external.kafka_connect.default.config --config cleanup.policy=compact
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.infra.external.kafka_connect.default.offset --partitions 25 --config cleanup.policy=compact
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.infra.external.kafka_connect.default.status --partitions 5 --config cleanup.policy=compact
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.api.v1.accounts.account.created
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.api.v1.audit_logs.audit_log.created
	docker compose exec kafka /bin/sh -c "echo 'Hello, world!' | kafka-console-producer --topic vault.api.v1.audit_logs.audit_log.created --bootstrap-server kafka:29092"

	docker compose up -d connect --wait
	curl -X POST -H "Content-Type: application/json" \
		--data '{"name": "azure-servicebus-sink-connector", "config": {"azure.servicebus.connection.string": "Endpoint=sb://default.default.default.localhost.localsandbox.sh;SharedAccessKeyName=1234;SharedAccessKey=password;UseDevelopmentEmulator=true", "connector.class": "io.cbdq.AzureServiceBusSinkConnector", "tasks.max": "1", "topics": "vault.api.v1.accounts.account.created,vault.api.v1.audit_logs.audit_log.created", "retry.max.attempts": "5", "retry.wait.time.ms": "1000", "value.converter": "org.apache.kafka.connect.converters.ByteArrayConverter", "key.converter": "org.apache.kafka.connect.converters.ByteArrayConverter", "consumer.override.auto.offset.reset": "earliest"}}' \
		http://localhost:8083/connectors

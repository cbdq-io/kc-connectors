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
	docker compose up -d kafka artemis --wait
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.infra.external.kafka_connect.default.config --config cleanup.policy=compact
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.infra.external.kafka_connect.default.offset --partitions 25 --config cleanup.policy=compact
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.infra.external.kafka_connect.default.status --partitions 5 --config cleanup.policy=compact
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.api.v1.accounts.account.created
	docker compose exec kafka kafka-topics --bootstrap-server kafka:29092 --create --topic vault.api.v1.audit_logs.audit_log.created
	docker compose exec kafka /bin/sh -c "echo 'Hello, world!' | kafka-console-producer --topic vault.api.v1.audit_logs.audit_log.created --bootstrap-server kafka:29092"

	docker compose exec artemis /var/lib/artemis-instance/bin/artemis queue create --user=artemis --password=artemis --name=vault.api.v1.accounts.account.created --silent --auto-create-address
	docker compose exec artemis /var/lib/artemis-instance/bin/artemis queue create --user=artemis --password=artemis --name=vault.api.v1.audit_logs.audit_log.created --silent --auto-create-address

	docker compose up -d connect --wait
	curl -X POST -H "Content-Type: application/json" \
		--data '{"name": "azure-servicebus-sink-connector", "config": {"azure.servicebus.connection.string": "Endpoint=amqp://artemis/;SharedAccessKeyName=artemis;SharedAccessKey=artemis", "connector.class": "io.cbdq.AzureServiceBusSinkConnector", "tasks.max": "1", "topics": "vault.api.v1.accounts.account.created,vault.api.v1.audit_logs.audit_log.created", "retry.max.attempts": "5", "retry.wait.time.ms": "1000", "value.converter": "org.apache.kafka.connect.converters.ByteArrayConverter", "key.converter": "org.apache.kafka.connect.converters.ByteArrayConverter", "consumer.override.auto.offset.reset": "earliest"}}' \
		http://localhost:8083/connectors

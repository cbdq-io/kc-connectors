all: build

build:
	docker compose run --rm mvn -B clean package

clean:
	docker compose run --rm mvn -B clean
	docker compose down -t 0

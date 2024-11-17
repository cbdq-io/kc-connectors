all: build

build:
	docker compose run --rm mvn -B -e clean install

clean:
	docker compose run --rm mvn -B clean
	docker compose down -t 0

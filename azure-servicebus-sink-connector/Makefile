all: build

build:
	docker compose run --rm mvn -B -e clean install

clean:
	docker compose run --rm mvn -B clean
	docker compose down -t 0

dependency_tree:
	docker compose run --rm mvn clean install
	docker compose run --rm mvn dependency:tree -X

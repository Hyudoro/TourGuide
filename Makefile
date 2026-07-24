MVN := ./mvnw

.DEFAULT_GOAL := help

.PHONY: help run test verify build clean

help: ## Show available commands
	@grep -E '^[a-zA-Z_-]+:.*?## .*$$' $(MAKEFILE_LIST) | \
		awk 'BEGIN {FS = ":.*?## "}; {printf "  \033[36m%-8s\033[0m %s\n", $$1, $$2}'

run: ## Start the Spring Boot app
	$(MVN) spring-boot:run

test: ## Run unit tests
	$(MVN) test

verify: ## Run unit + integration tests
	$(MVN) verify

build: ## Build the jar (skips tests)
	$(MVN) package -DskipTests

clean: ## Remove target/
	$(MVN) clean

# Makefile for CustomAuthenticationSPI Setup

KEYCLOAK_CONTAINER=keycloak_web
FRONT_CONTAINER=konneqt-front
SPI_JAR=target/CustomAuthenticationSPI-1.0-SNAPSHOT.jar
PROVIDER_PATH=/opt/keycloak/providers
KC_BUILD=/opt/keycloak/bin/kc.sh

# 1. Build the SPI .jar
build-spi:
	mvn clean package

# 2. Stop and remove project-specific containers, networks and volumes
clean:
	docker-compose down --remove-orphans --volumes

# 3. Start only DB and Keycloak for build
up-temp:
	docker-compose up -d postgres keycloak_web

# 4. Copy the JAR into the Keycloak container
copy-spi:
	docker cp $(SPI_JAR) $(KEYCLOAK_CONTAINER):$(PROVIDER_PATH)

# 5. Stop Keycloak before building with the new provider
stop-keycloak:
	docker stop $(KEYCLOAK_CONTAINER)

# 6. Build Keycloak with the SPI
rebuild-keycloak:
	docker start $(KEYCLOAK_CONTAINER)
	docker exec -it $(KEYCLOAK_CONTAINER) $(KC_BUILD) build

# 7. Restart Keycloak after build
restart-keycloak:
	docker restart $(KEYCLOAK_CONTAINER)

# 8. Build the frontend again
build-front:
	docker-compose build konneqt-front

# 9. Start all services
up:
	docker-compose up -d

# 10. Full setup: clean, build SPI, setup, build front and up all
start-all: clean build-spi up-temp copy-spi stop-keycloak rebuild-keycloak restart-keycloak build-front up

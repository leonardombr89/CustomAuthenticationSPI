KEYCLOAK_CONTAINER=keycloak_web
SPI_JAR=target/CustomAuthenticationSPI-1.0-SNAPSHOT.jar
PROVIDER_PATH=/opt/keycloak/providers
KC_BUILD=/opt/keycloak/bin/kc.sh

# 1. Build the SPI .jar
build-spi:
	mvn clean package

# 2. Go up the bank and keycloak temporarily just to do the build
up-temp:
	docker-compose up -d postgres keycloak_web

# 3. Copy the JAR into the container
copy-spi:
	docker cp $(SPI_JAR) $(KEYCLOAK_CONTAINER):$(PROVIDER_PATH)

# 4. For the container before building
stop-keycloak:
	docker stop $(KEYCLOAK_CONTAINER)

# 5. Run the SPI build (with container stopped)
rebuild-keycloak:
	docker start $(KEYCLOAK_CONTAINER)
	docker exec -it $(KEYCLOAK_CONTAINER) $(KC_BUILD) build

# 6. Restart Keycloak after build
restart-keycloak:
	docker restart $(KEYCLOAK_CONTAINER)

# 7. Go all up
up:
	docker-compose up -d

# 8. Full command (used at the end)
start-all: build-spi up-temp copy-spi stop-keycloak rebuild-keycloak restart-keycloak up

# Custom Endpoint no Keycloak

-------

Fiz um vídeo dessa implementação, caso queria ver o resultado final: 
https://www.youtube.com/watch?list=TLGGW5wFF-mydQ4wODAzMjAyNQ&v=WxN899vDRz8

### create mvn project
```sh
mvn archetype:generate -DgroupId=com.meusistema.keycloak -DartifactId=custom-authenticator -DarchetypeArtifactId=maven-archetype-quickstart -DinteractiveMode=false
cd custom-authenticator
```

### create docker-compose.yml
```
version: '3.7'

networks:
  backend:
    driver: "bridge"

services:
  postgres:
    image: postgres:15
    container_name: postgres
    restart: always
    environment:
      POSTGRES_DB: keycloak
      POSTGRES_USER: keycloak
      POSTGRES_PASSWORD: password
    ports:
      - "5432:5432"
    networks:
      - backend
    expose:
      - 5432
    volumes:
      - postgres_data:/var/lib/postgresql/data
    healthcheck:
      test: ["CMD-SHELL", "pg_isready -U keycloak"]
      interval: 5s
      timeout: 3s
      retries: 5

  keycloak_web:
    image: quay.io/keycloak/keycloak:24.0.3
    container_name: keycloak_web
    environment:
      KC_DB: postgres
      KC_DB_URL: jdbc:postgresql://postgres:5432/keycloak
      KC_DB_USERNAME: keycloak
      KC_DB_PASSWORD: password
      KC_HOSTNAME: localhost
      KC_HOSTNAME_STRICT: false
      KC_HOSTNAME_STRICT_HTTPS: false
      KC_LOG_LEVEL: info
      KC_METRICS_ENABLED: true
      KC_HEALTH_ENABLED: true
      KEYCLOAK_ADMIN: admin
      KEYCLOAK_ADMIN_PASSWORD: admin
    command: start-dev
    depends_on:
      postgres:
        condition: service_healthy
    ports:
      - "8080:8080"
    networks:
      - backend

volumes:
  postgres_data:
```

### start docker compose
```sh
docker compose up -d
```

### cp theme to docker
```sh
docker cp src/themes/meu-tema <CONTAINER_ID>:/opt/keycloak/themes/
```

### build project with docker
```sh
mvn clean package && docker cp target/custom-authenticator-1.0.0.jar <CONTAINER_ID>:/opt/keycloak/providers/ && docker exec -it <CONTAINER_ID> /opt/keycloak/bin/kc.sh build && docker restart <CONTAINER_ID>
```

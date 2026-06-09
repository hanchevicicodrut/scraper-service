#!/bin/bash
set -a
source .env.prod
set +a
./mvnw spring-boot:run -Dspring-boot.run.profiles=prod

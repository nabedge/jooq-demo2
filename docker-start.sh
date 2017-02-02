#!/bin/sh

docker-compose down
#docker-compose down --rmi all
#docker-compose build
#docker-compose up --force-recreate --abort-on-container-exit
#docker-compose up --abort-on-container-exit
docker-compose up -d

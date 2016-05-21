#!/bin/bash

get_ip() {
    id=${1}
    ip=`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${id}`
    echo ${ip}
}

./mvnw

docker rm --force backend_1 backend_2 backend_3
docker run -d --name backend_1 -e MODE=backend rso
docker run -d --name backend_2 -e MODE=backend rso
docker run -d --name backend_3 -e MODE=backend rso

backends=""
for id in $(docker ps | grep rso | grep backend | awk '{print $1}')
do
    ip=`get_ip ${id}`
    backends+="http://${ip}:8080/api/;"
done

docker run --rm -i -t -p 8080:8080  -e MODE=frontend -e BACKEND_ENDPOINTS=${backends} rso
docker rm --force backend_1 backend_2 backend_3

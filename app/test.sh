#!/bin/bash

check_url() {
    url=$1
    status=`curl -k --connect-timeout 5 -s -o /dev/null -w "%{http_code}" ${url}`
    exit_code=$?
    if [[ ${exit_code} -ne 0 || ${status} -lt 200 || ${status} -ge 400 ]]
    then
        echo -e "\e[31m${url} => ${status} (exit code: ${exit_code})\e[0m"
        return 0
    else
        echo -e "\e[32m${url} => ${status}\e[0m"
        return 1
    fi
}
wait_for_url() {
    while check_url ${1}; do
        sleep 3
    done
}
get_ip() {
    id=${1}
    ip=`docker inspect --format '{{ .NetworkSettings.IPAddress }}' ${id}`
    echo ${ip}
}

execute() {
    command=${@}
    >&2 echo "Running ${command}:"
    ${command}
    result=${?}
    if [[ "${result}" != "0" ]]; then
        >&2 echo -e "\e[31m${command} finished, exit code: ${result}\e[0m"
    else
        >&2 echo -e "\e[32m${command} finished, exit code: ${result}\e[0m"
    fi
}

diff_files() {
    file1=${1}
    file2=${2}
    if diff ${file1} ${file2}
    then
        echo -e "\e[32mPliki są takie same\e[0m"
    else
        echo -e "\e[31mPliki są różne\e[0m"
    fi
}

error() {
  echo "Błąd!!!"
  echo -n "Powód: "
  echo "$1" >&2
  if [ ${SERVER_PID} -ne 0 ]; then
    kill ${SERVER_PID} >/dev/null 2>&1
    sleep 0.1
    kill -9 ${SERVER_PID} >/dev/null 2>&1
  fi
  exit 1
}

docker rm --force backend_1 backend_2 backend_3 frontend_1 frontend_2 2> /dev/null

echo -n "Start serwera danych źródłowych... "
bash -c "pkill python; cd test_data && python -m SimpleHTTPServer 8888" &
echo "Serwer działa na porcie 8888"

host_ip=$(ip route | grep docker | awk '{ print $(NF) }')
echo "IP hosta w sieci docker: ${host_ip}"

execute docker run -d --name backend_1 -e MODE=backend rso
execute docker run -d --name backend_2 -e MODE=backend rso
execute docker run -d --name backend_3 -e MODE=backend rso

backends=""
for id in $(docker ps | grep rso | grep backend | awk '{print $1}')
do
    ip=`get_ip ${id}`
    backends+="http://${ip}:8080/api/;"
done

execute docker run -d -i -t -p 8080:8080 --name frontend_1 -e MODE=frontend -e BACKEND_ENDPOINTS=${backends} rso
execute docker run -d -i -t -p 8081:8080 --name frontend_2 -e MODE=frontend -e BACKEND_ENDPOINTS=${backends} rso

for id in $(docker ps | grep rso | grep rso | awk '{print $1}')
do
    ip=`get_ip ${id}`
    echo "Sprawdzam czy aplikacja już działa na ${id}"
    wait_for_url "http://${ip}:8080/health"
done

echo "Test 1: Pobranie danych do systemu z zewnątrz i z systemu do użytkownika"
read

idFile=$(mktemp rso.XXXXXXXXXX)
execute ./client.py --server http://localhost:8080/api/ upload http://${host_ip}:8888/1.in | tee ${idFile}
fileId=$(cat "${idFile}")
echo "Zlecono pogranie danych, ID: ${fileId}, ściągnąć?"
read

echo 'Ściągam plik z usługi'
execute ./client.py --server http://localhost:8080/api/ download ${fileId} test_data/1.out1
execute ./client.py --server http://localhost:8081/api/ download ${fileId} test_data/1.out2

echo 'Porównuje pliki pobrane z róœznych węzłów warstwy zewnętrznej'
diff_files test_data/1.in test_data/1.out1
diff_files test_data/1.in test_data/1.out2

echo "Test 1 - zakończony: Pobranie danych do systemu z zewnątrz i z systemu do użytkownika"
read

echo "Test 2: Pobranie danych do systemu z zewnątrz i z systemu do użytkownika (plik 100MB)"
read

idFile=$(mktemp rso.XXXXXXXXXX)
execute ./client.py --server http://localhost:8080/api/ upload http://${host_ip}:8888/2.in | tee ${idFile}
fileId=$(cat "${idFile}")
echo "Zlecono pogranie danych, ID: ${fileId}, ściągnąć?"
read

echo 'Ściągam plik z usługi'
execute ./client.py --server http://localhost:8080/api/ download ${fileId} test_data/2.out1
execute ./client.py --server http://localhost:8081/api/ download ${fileId} test_data/2.out2

echo 'Porównuje pliki pobrane z róœznych węzłów warstwy zewnętrznej'
diff_files test_data/2.in test_data/2.out1
diff_files test_data/2.in test_data/2.out2

echo "Test 2 - zakończony: Pobranie danych do systemu z zewnątrz i z systemu do użytkownika (plik 100MB)"
read

echo "Test 3: Pobranie danych do systemu z zewnątrz. Wyłączenie < 50% węzłów i pobranie z systemu do użytkownika"
read

idFile=$(mktemp rso.XXXXXXXXXX)
execute ./client.py --server http://localhost:8080/api/ upload http://${host_ip}:8888/1.in | tee ${idFile}
fileId=$(cat "${idFile}")
echo "Zlecono pogranie danych, ID: ${fileId}, ściągnąć?"
read

echo "Zatrzymanie wybranych węzłów"
execute docker stop backend_1
read

echo 'Ściągam plik z usługi'
execute ./client.py --server http://localhost:8080/api/ download ${fileId} test_data/3.out1
execute ./client.py --server http://localhost:8081/api/ download ${fileId} test_data/3.out2
execute ./client.py --server http://localhost:8081/api/ download ${fileId} test_data/3.out3

echo "Test 3 zakończony: Pobranie danych do systemu z zewnątrz. Wyłączenie < 50% węzłów i pobranie z systemu do użytkownika"
read

echo "Test 4: Pobranie danych do systemu z zewnątrz. Wyłączenie > 50% węzłów i pobranie z systemu do użytkownika"
read

idFile=$(mktemp rso.XXXXXXXXXX)
execute ./client.py --server http://localhost:8080/api/ upload http://${host_ip}:8888/1.in | tee ${idFile}
fileId=$(cat "${idFile}")
echo "Zlecono pogranie danych, ID: ${fileId}, ściągnąć?"
read

echo "Zatrzymanie wybranych węzłów"
execute docker stop backend_2
echo "Węzły zatrzymane"
read

echo 'Ściągam plik z usługi'
execute ./client.py --server http://localhost:8080/api/ download ${fileId} test_data/4.out1
execute ./client.py --server http://localhost:8081/api/ download ${fileId} test_data/4.out2
execute ./client.py --server http://localhost:8080/api/ download ${fileId} test_data/4.out3

echo "Test 3 zakończony: Pobranie danych do systemu z zewnątrz. Wyłączenie > 50% węzłów i pobranie z systemu do użytkownika"
read
docker rm --force backend_1 backend_2 backend_3 frontend_1 frontend_2

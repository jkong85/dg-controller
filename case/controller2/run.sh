#!/bin/bash
set -x
function checkIP()
{
    IPADDR=$1
    regex="\b(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[1-9])\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[0-9])\.(25[0-5]|2[0-4][0-9]|1[0-9][0-9]|[1-9][0-9]|[1-9])\b"
    ckStep2=`echo $1 | egrep $regex | wc -l`
    if [ $ckStep2 -eq 0 ]
    then
        echo "NO"
    else
        echo "$IPADDR"
    fi 
}

echo ">>>> clean previous running..."
./stop.sh

echo ">>>> Start the eureka server"
cp template/eureka.yaml.template eureka.yaml
kubectl create -f eureka.yaml
sleep 200

echo ">>>> Create the service"
cp template/service.yaml.template service.yaml
kubectl create -f service.yaml

EUREKA_SERVER_IP=
for ((i=1; i<60; i++))
do
    EUREKA_SERVER_IP="$(kubectl get pods -o wide | grep controller-eureka | awk '{print $6}' |awk '{print $1}' |awk 'NR==1{print}')"
    CUR=$(checkIP ${EUREKA_SERVER_IP})
    #if [ -n "${EUREKA_SERVER_IP}" ]; then     
    if [ "${CUR}" != "NO" ]; then     
        echo "Eureka Server starts successfully, server ip: ${EUREKA_SERVER_IP}"
        break
    fi
    EUREKA_SERVER_IP="$(kubectl get pods -o wide | grep controller-eureka | awk '{print $6}' |awk '{print $1}' |awk 'NR==2{print}')"
    CUR=$(checkIP ${EUREKA_SERVER_IP})
    if [ "${CUR}" != "NO" ]; then     
        echo "Eureka Server starts successfully, server ip: ${EUREKA_SERVER_IP}"
        break
    fi
    sleep 10
    if [ $i -eq 59 ]; then
        echo "Can NOT get Eureka server ip address, QUIT!"
        exit 1
    fi
    echo "Eureka server is starting..."
done

echo ${EUREKA_SERVER_IP} > eureka_server_ip

echo ">>>> Config the YAML based on template"
cp template/zuul.yaml.template zuul.yaml
sed -i .tmp -e "s/VALUE_EUREKA_SERVER_IP/value: ${EUREKA_SERVER_IP}/g" zuul.yaml
cp template/test.yaml.template test.yaml
sed -i .tmp -e "s/VALUE_EUREKA_SERVER_IP/value: ${EUREKA_SERVER_IP}/g" test.yaml
rm *.tmp

echo ">>>> Create the microservices"
kubectl create -f test.yaml
sleep 20 
kubectl create -f zuul.yaml


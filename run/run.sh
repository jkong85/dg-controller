#!/bin/bash
#set -x

echo "**************************************************"
echo " Label the VM node with core/edge1/edge2"
echo "**************************************************"

sudo kubectl label nodes node1 cloud.name=core --overwrite
sudo kubectl label nodes node2 cloud.name=edge1 --overwrite
sudo kubectl label nodes node3 cloud.name=edge2 --overwrite

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
#./stop.sh
rm *.ymal
rm eureka_server_ip

NODE="core"

echo ">>>> Start the eureka server"
cp template/eureka.yaml.template eureka.yaml
sed -i .tmp -e "s/NODE/${NODE}/g" eureka.yaml
kubectl create -f eureka.yaml
#sleep 120

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
    sleep 3
    if [ $i -eq 59 ]; then
        echo "Can NOT get Eureka server ip address, QUIT!"
        exit 1
    fi
    echo "Eureka server is starting..."
done

echo ${EUREKA_SERVER_IP} > eureka_server_ip
echo "IP address of eureka server is: ${EUREKA_SERVER_IP}"

echo ">>>> Config the YAML based on template"
cp template/zuul.yaml.template zuul.yaml
sed -i .tmp -e "s/NODE/${NODE}/g" zuul.yaml
sed -i .tmp -e "s/VALUE_EUREKA_SERVER_IP/value: ${EUREKA_SERVER_IP}/g" zuul.yaml
#cp template/test.yaml.template test.yaml
#sed -i .tmp -e "s/NODE/${NODE}/g" test.yaml
#sed -i .tmp -e "s/VALUE_EUREKA_SERVER_IP/value: ${EUREKA_SERVER_IP}/g" test.yaml
cp template/core.yaml.template core.yaml
sed -i .tmp -e "s/NODE/${NODE}/g" core.yaml
sed -i .tmp -e "s/VALUE_EUREKA_SERVER_IP/value: ${EUREKA_SERVER_IP}/g" core.yaml
rm *.tmp

echo ">>>> Create the microservices"
#kubectl create -f test.yaml
kubectl create -f core.yaml
sleep 1
kubectl create -f zuul.yaml

echo " clean the tmp file"
rm eureka_server_ip


echo "Show label information of VM nodes"
sudo kubectl get node --show-labels
echo "end of staring all thing!"


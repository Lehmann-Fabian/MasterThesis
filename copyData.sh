#!/bin/bash

ips=`cat kubespray/inventory/mycluster/hosts.yaml | grep -B 2 "access_ip" | awk '{print$1$2}'`

for ip in $ips 
do
    #echo $ip

    if [[ $ip == server* ]]; 
    then
        server=${ip::-1}
        echo $server
    fi

    if [[ $ip == access_ip* ]]; 
    then
        iph=`echo $ip | cut -c 11-`
        echo $server $iph
        mkdir -p ./results/setup$1/$2/logs/usage/$server
        scp -i ./MockFog2/node-manager/run/MasterThesisKey.pem -o StrictHostKeyChecking=no ec2-user@$iph:/tmp/ps.log ./results/setup$1/$2/logs/usage/$server/ps.log
    fi

done

echo copy producer data

pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\bproducer\b"`

for pod in $pods
do
    kubectl cp kafka/$pod:data ./results/setup$1/$2/data/$pod
done

echo copy analyst logs

pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\banalyst\b"`

for pod in $pods
do
    kubectl cp kafka/$pod:log.log ./results/setup$1/$2/logs/$pod/log.log
done

echo copy filter logs

pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\bfilter\b"`

for pod in $pods
do
    kubectl cp kafka/$pod:log.log ./results/setup$1/$2/logs/$pod/log.log
done

echo copy producer logs

pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\bproducer\b"`

for pod in $pods
do
    kubectl cp kafka/$pod:log.log ./results/setup$1/$2/logs/$pod/log.log
done

echo copy kafka logs

pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\bkafka\b"`

for pod in $pods
do
    mkdir results/setup$1/$2/logs/$pod/
    kubectl logs $pod -n kafka >> ./results/setup$1/$2/logs/$pod/log.log
done

echo copy zookeaper logs

pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\bzoo\b"`

for pod in $pods
do
    mkdir results/setup$1/$2/logs/$pod/
    kubectl logs $pod -n kafka >> ./results/setup$1/$2/logs/$pod/log.log
done
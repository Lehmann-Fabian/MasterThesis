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
        mkdir -p ./results/setup$1/$2/logs/$server
        scp -i ./MockFog2/node-manager/run/MasterThesisKey.pem -o StrictHostKeyChecking=no ec2-user@$iph:/tmp/ps.log ./results/setup$1/$2/logs/$server/ps.log
    fi

done

pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\bproducer\b"`

for pod in $pods
do
    kubectl cp kafka/$pod:data ./results/setup$1/$2/data/$pod
done

pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\banalyst\b"`

for pod in $pods
do
    kubectl cp kafka/$pod:log.log ./results/setup$1/$2/logs/$pod/log.log
done

pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\bfilter\b"`

for pod in $pods
do
    kubectl cp kafka/$pod:log.log ./results/setup$1/$2/logs/$pod/log.log
done

pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\bproducer\b"`

for pod in $pods
do
    kubectl cp kafka/$pod:log.log ./results/setup$1/$2/logs/$pod/log.log
done
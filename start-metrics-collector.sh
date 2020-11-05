#!/bin/bash

ips=`cat kubespray/inventory/mycluster/hosts.yaml | grep "access_ip" | awk '{print$2}'`

for ip in $ips 
do
    echo Collect data on $ip
    scp -i ./MockFog2/node-manager/run/MasterThesisKey.pem -o StrictHostKeyChecking=no collect-metrics.sh ec2-user@$ip:/tmp/collect-metrics.sh
    ssh -i ./MockFog2/node-manager/run/MasterThesisKey.pem ec2-user@$ip -o "StrictHostKeyChecking no" ConnectTimeout=60 "nohup sh /tmp/collect-metrics.sh  > /dev/null 2>&1 &"
    ssh -i ./MockFog2/node-manager/run/MasterThesisKey.pem ec2-user@$ip -o "StrictHostKeyChecking no" ConnectTimeout=60 "sudo docker pull reh69113/masterthesis:analyst"
    ssh -i ./MockFog2/node-manager/run/MasterThesisKey.pem ec2-user@$ip -o "StrictHostKeyChecking no" ConnectTimeout=60 "sudo docker pull reh69113/masterthesis:filter"
done
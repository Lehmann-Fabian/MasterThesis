#!/bin/bash


i=1
start=`date +%s`

while true; do
    
    end=`date +%s`
    timeGone=$((end-start))
    echo "Trial $i after $timeGone s $end"
    kubectl run -i --tty --rm -n kafka debug$i --image=edenhill/kafkacat:1.6.0 --restart=Never -- -b kafka.kafka.svc.cluster.local:9092 -L


    kubectl get pod -o=custom-columns=NAME:.metadata.name,STATUS:.status.phase,NODE:.spec.nodeName -n kafka

    i=$[$i+1]

    sleep 1s

done
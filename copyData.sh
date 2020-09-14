#!/bin/bash
#run this: bash run-test-deployment.sh 

pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\bproducer\b"`

for pod in $pods
do
    kubectl cp kafka/$pod:data ./data/$pod
done
#!/bin/bash
#run this: bash run-test-deployment.sh 

pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\bproducer\b"`

for pod in $pods
do
    kubectl cp kafka/$pod:data ./results/setup$1/data/$pod
done

pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\banalyst\b"`

for pod in $pods
do
    kubectl cp kafka/$pod:log.log ./results/setup$1/logs/$pod/log.log
done

pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\bfilter\b"`

for pod in $pods
do
    kubectl cp kafka/$pod:log.log ./results/setup$1/logs/$pod/log.log
done

pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\bproducer\b"`

for pod in $pods
do
    kubectl cp kafka/$pod:log.log ./results/setup$1/logs/$pod/log.log
done
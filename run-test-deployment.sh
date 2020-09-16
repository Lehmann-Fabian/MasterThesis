#!/bin/bash
#run this: bash run-test-deployment.sh 

kind delete cluster --name testcluster
kind create cluster --name=testcluster --config=./kind/config.yaml

sleep 10s

kubectl apply -f ./kubernetes-kafka/00-namespace.yml

sleep 10s
kubectl apply -k ./kubernetes-kafka/variants/my-dev

kubectl label nodes testcluster-worker monitor-patient-data=true
kubectl label nodes testcluster-worker2 monitor-patient-data=true
kubectl label nodes testcluster-worker3 monitor-patient-data=true
kubectl label nodes testcluster-worker4 monitor-patient-data=true
kubectl label nodes testcluster-worker5 monitor-patient-data=true

bash kubectl/deploy.sh

bash run-pipeline.sh
#kubectl get pod -o=custom-columns=NAME:.metadata.name,STATUS:.status.phase,NODE:.spec.nodeName -n kafka -w
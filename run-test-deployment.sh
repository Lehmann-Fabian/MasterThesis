#!/bin/bash
#run this: bash run-test-deployment.sh 

kind delete cluster --name testcluster
kind create cluster --name=testcluster --config=./kind/config.yaml

kubectl apply -f ./kubernetes-kafka/00-namespace.yml
kubectl apply -k ./kubernetes-kafka/variants/my-dev

kubectl label nodes testcluster-worker monitor-patient-data=true
kubectl label nodes testcluster-worker2 monitor-patient-data=true
kubectl label nodes testcluster-worker3 monitor-patient-data=true
kubectl label nodes testcluster-worker4 monitor-patient-data=true
kubectl label nodes testcluster-worker5 monitor-patient-data=true

#kubectl apply -k .

echo "run kubectl exec -it -n kafka producer-xyzyx -- /bin/sh"

bash run-pipeline.sh
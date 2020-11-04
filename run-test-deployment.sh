#!/bin/bash
#run this: bash run-test-deployment.sh 

kind delete cluster --name testcluster
kind create cluster --name=testcluster --config=./kind/config.yaml --wait 5m


kubectl label nodes testcluster-worker monitor-patient-data=true
kubectl label nodes testcluster-worker2 monitor-patient-data=true
kubectl label nodes testcluster-worker3 monitor-patient-data=true
kubectl label nodes testcluster-worker4 monitor-patient-data=true
kubectl label nodes testcluster-worker5 monitor-patient-data=true


kubectl label nodes testcluster-worker topology.kubernetes.io/region=a --overwrite
kubectl label nodes testcluster-worker2 topology.kubernetes.io/region=a --overwrite
kubectl label nodes testcluster-worker3 topology.kubernetes.io/region=a --overwrite
kubectl label nodes testcluster-worker4 topology.kubernetes.io/region=b --overwrite
kubectl label nodes testcluster-worker5 topology.kubernetes.io/region=b --overwrite
#kubectl label nodes testcluster-worker6 topology.kubernetes.io/region=c --overwrite
#kubectl label nodes testcluster-worker7 topology.kubernetes.io/region=c --overwrite


sleep 3s

kubectl apply -f ./kubernetes-kafka/00-namespace.yml

sleep 3s
kubectl apply -k ./kubernetes-kafka/variants/my-dev

exit 0

cd kubectl
bash deploy.sh
cd ..

bash run-pipeline.sh
#kubectl get pod -o=custom-columns=NAME:.metadata.name,STATUS:.status.phase,NODE:.spec.nodeName -n kafka -w

sleep 5m
docker stop testcluster-worker6
sleep 15m
docker stop testcluster-worker7
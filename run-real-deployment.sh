#!/bin/bash
#run this: bash run-real-deployment.sh 

cd MockFog2
bash start.sh
cd ..


python3 generateKubernetesHosts.py ./MockFog2/node-manager/run/machines/machine_meta.jsonc kubespray/inventory/mycluster/hosts.yaml

exit 0
bash startKubernetes.sh

kubectl apply -f ./kubernetes-kafka/00-namespace.yml

sleep 10s
kubectl apply -k ./kubernetes-kafka/variants/my-dev

kubectl label nodes testcluster-worker monitor-patient-data=true
kubectl label nodes testcluster-worker2 monitor-patient-data=true
kubectl label nodes testcluster-worker3 monitor-patient-data=true
kubectl label nodes testcluster-worker4 monitor-patient-data=true
kubectl label nodes testcluster-worker5 monitor-patient-data=true

cd kubectl
bash deploy.sh
cd ..

bash run-pipeline.sh
#kubectl get pod -o=custom-columns=NAME:.metadata.name,STATUS:.status.phase,NODE:.spec.nodeName -n kafka -w

sleep 5m
docker stop testcluster-worker6
sleep 15m
docker stop testcluster-worker7
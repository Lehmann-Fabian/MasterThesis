#!/bin/bash
#run this: bash run-real-deployment.sh 

cd MockFog2
bash start.sh
cd ..

python3 generateKubernetesHosts.py ./MockFog2/node-manager/run/machines/machine_meta.jsonc kubespray/inventory/mycluster/hosts.yaml

bash startKubernetes.sh

export KUBECONFIG=~MasterThesisCode/kubespray/inventory/mycluster/artifacts/admin.conf

kubectl apply -f storage/default-storage.yml

kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/master/deploy/local-path-storage.yaml

kubectl apply -f ./kubernetes-kafka/00-namespace.yml

sleep 10s
kubectl apply -k ./kubernetes-kafka/variants/my-dev

kubectl label nodes server1 monitor-patient-data=true
kubectl label nodes server2 monitor-patient-data=true
kubectl label nodes server3 monitor-patient-data=true
#kubectl label nodes server4 monitor-patient-data=true
#kubectl label nodes server5 monitor-patient-data=true

cd kubectl
bash deploy.sh
cd ..

bash run-pipeline.sh
#kubectl get pod -o=custom-columns=NAME:.metadata.name,STATUS:.status.phase,NODE:.spec.nodeName -n kafka -w

exit 0
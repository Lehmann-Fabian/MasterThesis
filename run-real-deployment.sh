#!/bin/bash
#run this: bash run-real-deployment.sh 

#start instances
cd MockFog2
bash start.sh
cd ..

#create host.yaml file for k8n
python3 generateKubernetesHosts.py ./MockFog2/node-manager/run/machines/machine_meta.jsonc kubespray/inventory/mycluster/hosts.yaml


# echo "install node agent and manipulate connection"
# cd MockFog2
# bash prepare.sh
# cd ..

#install cluster
bash startKubernetes.sh



#export k8n access data
export KUBECONFIG=~/MasterThesisCode/kubespray/inventory/mycluster/artifacts/admin.conf

exit 0

echo "install a default pvc provisioner"
kubectl apply -f storage/default-storage.yml

kubectl apply -f https://raw.githubusercontent.com/rancher/local-path-provisioner/master/deploy/local-path-storage.yaml

echo "install kafka"
kubectl apply -f ./kubernetes-kafka/00-namespace.yml

sleep 10s
kubectl apply -k ./kubernetes-kafka/variants/my-dev


echo "which nodes are next to patients"
kubectl label nodes server1 monitor-patient-data=true
kubectl label nodes server2 monitor-patient-data=true
kubectl label nodes server3 monitor-patient-data=true
kubectl label nodes server4 monitor-patient-data=true
kubectl label nodes server5 monitor-patient-data=true
kubectl label nodes server6 monitor-patient-data=true
kubectl label nodes server7 monitor-patient-data=true

echo "Start observation tool"
cd kubectl
bash deploy.sh
cd ..

#launch pipeline services
bash run-pipeline.sh
#kubectl get pod -o=custom-columns=NAME:.metadata.name,STATUS:.status.phase,NODE:.spec.nodeName -n kafka -w

#start experiment manipulation
# cd MockFog2
# bash run-orchestration.sh
# cd ..

exit 0
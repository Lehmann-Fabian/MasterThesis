#!/bin/bash

if [ -z "$1" ]; then
    echo "define a setup"
    exit 100
fi

if [ -z "$2" ]; then
    echo "define a run"
    exit 100
fi

export KUBECONFIG=~/MasterThesisCode/kubespray/inventory/mycluster/artifacts/admin.conf

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


#mark masters as unschedulable
# masters=`kubectl get nodes | grep "master" | awk '{print$1}'`

# for master in $masters
# do
#     kubectl patch node $master -p "{\"spec\":{\"unschedulable\":true}}"
# done


echo "Start observation tool"
cd kubectl
bash deploy.sh
cd ..


bash start-metrics-collector.sh

rm -r ./results/setup$1/$2/
mkdir -p ./results/setup$1/$2/logs/

#launch pipeline services
bash run-pipeline.sh
#kubectl get pod -o=custom-columns=NAME:.metadata.name,STATUS:.status.phase,NODE:.spec.nodeName -n kafka -w

#start logging
pid=`nohup bash fetchResponsibilities.sh >> ./results/setup$1/$2/logs/responsibilities.log & echo $!`

#start experiment manipulation
cd MockFog2
bash run-orchestration.sh
cd ..

#Experiment 0 has only one state
if [ $1 -eq 0 ]; then
    sleep 22m
else
    sleep 7m
fi

kill $pid

#stop network manipulation ==> to copy faster
cp ./orchestration/fast.json ./MockFog2/node-manager/run/config/orchestration.jsonc
cd MockFog2
bash run-orchestration.sh
cd ..


bash copyData.sh $1 $2

exit 0
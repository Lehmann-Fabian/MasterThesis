#!/bin/bash
#run this: bash run-real-deployment.sh 


if [ -z "$1" ]; then
    echo "define a setup"
    exit 100
fi

cp ./orchestration/case_$1.json ./MockFog2/node-manager/run/config/orchestration.jsonc
cp ./orchestration/infrastructure.jsonc ./MockFog2/node-manager/run/config/infrastructure.jsonc

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
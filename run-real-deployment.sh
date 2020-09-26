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
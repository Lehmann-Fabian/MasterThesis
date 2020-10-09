#!/bin/bash

#start instances
cd MockFog2
#bash start.sh
cd ..

#create host.yaml file for k8n
python3 generateKubernetesHosts.py ./MockFog2/node-manager/run/machines/machine_meta.jsonc kubespray/inventory/mycluster/hosts.yaml

#install cluster
bash startKubernetes.sh

#prepare instances for traffic manipulation
cd MockFog2
bash prepare.sh
cd ..


#export k8n access data
export KUBECONFIG=~/MasterThesisCode/kubespray/inventory/mycluster/artifacts/admin.conf

exit 0
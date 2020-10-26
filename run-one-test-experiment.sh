#!/bin/bash

export KUBECONFIG=~/MasterThesisCode/kubespray/inventory/mycluster/artifacts/admin.conf

#bash reset-Kubernetes.sh

bash run-real-experiment.sh $1 $2 | tee results/runs/exp$1-$2.log
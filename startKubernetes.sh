#!/bin/bash

cd kubespray/
ansible-playbook -i inventory/mycluster/hosts.yaml cluster.yml --private-key=../MockFog2/node-manager/run/MasterThesisKey.pem -b --become-user=root -vvv --flush-cache -e 'ansible_python_interpreter=/bin/python2.7'

#ansible-playbook -i inventory/mycluster/hosts.yaml cluster.yml --private-key=../MasterThesisTest.pem -b --become-user=root -vvv --flush-cache -e 'ansible_python_interpreter=/bin/python2.7'
#'ansible_python_interpreter=/usr/bin/python3'
#
export KUBECONFIG=./inventory/mycluster/artifacts/admin.conf

cd ..
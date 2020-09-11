#bin/bash

kind delete cluster --name testcluster
kind create cluster --name=testcluster --config=./kind/config.yaml

kubectl apply -f ./kubernetes-kafka/00-namespace.yml
kubectl apply -k ./kubernetes-kafka/variants/my-dev

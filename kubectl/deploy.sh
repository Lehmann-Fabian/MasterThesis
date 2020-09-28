kubectl create configmap cluster-manager --from-file=cluster-manager.sh
#kubectl create configmap cluster-manager --from-file=cluster-manager.sh -o yaml --dry-run=client | kubectl apply -f - 
#kubectl delete -f kubectl.yaml
kubectl apply -f kubectl.yaml
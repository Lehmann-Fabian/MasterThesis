#!/bin/sh

echo "lets go"

checkState(){

  pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\b$1\b"`

  for pod in $pods
  do
      podState=`kubectl get pods -n kafka | grep "\b$pod\b" | awk '{print$3}'`

      if [ "$podState" = "Terminating" ]; then
          echo "Wrong state $podRunning $pod"
          kubectl delete pod $pod -n kafka  --grace-period=0 --force
          echo "Triggered restart!"
          sleep 5s
      fi

  done

  # get unreachable Nodes
  nodes=`kubectl get nodes | grep "\bNotReady\b" | awk '{print$1}'`

  for node in $nodes
  do
      pods=`kubectl get pod -o=custom-columns=NAME:.metadata.name,NODE:.spec.nodeName -n kafka | grep "$node" | awk '{print$1}'`

      for pod in $pods
      do
        if [ "$pod" != "${pod#analyst}" ] || [ "$pod" != "${pod#filter}" ]; then
          kubectl delete pod $pod -n kafka  --grace-period=0 --force
          echo "Triggered restart!"
          sleep 5s
        fi
      done

  done

}

while [ true ]
do
  {
    checkState analyst
    checkState filter
    sleep 5s
  } || {
    echo "There was an error!"
  }
done
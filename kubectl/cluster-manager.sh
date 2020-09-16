#!/bin/sh

echo "lets go"

checkState(){

  pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name" | grep "\b$1\b"`

  for pod in $pods
  do
      podState=`kubectl get pods -n kafka | grep "\b$pod\b" | awk '{print$3}'`

      if [ "$podState" = "Terminating" ]; then
          echo "Wrong state $podRunning $pod"
          kubectl delete pod $pod -n kafka --force
          echo "Triggered restart!"
          sleep 5s
      fi

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
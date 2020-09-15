#!/bin/bash

wait_ready(){

    state="wait"

    counter=0
    counter=$((counter+1))

    while [ "$counter" -lt 5 ]; do

        sleep 1s
        counter=$((counter+1))

        pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name"`

        for pod in $pods
        do
            podState=`kubectl get pods -n kafka | grep "\b$pod\b" | awk '{print$2}'`
            podRunning=`kubectl get pods -n kafka | grep "\b$pod\b" | awk '{print$3}'`

            if [ "$podRunning" != "Running" ]; then
                echo "wrong state $podRunning"
                counter=0
            fi
            if [ "$podState" != "1/1" ]; then
                echo "wrong state $podState"
                counter=0
            fi

        done

        if [ "$counter" -gt 1 ]; then
            sleep 5s
        fi

    done

}


wait_ready

echo "start analyst"

kubectl apply -f analyst/analyst.yaml

wait_ready

echo "start filter"

kubectl apply -f filter/filter.yaml

wait_ready

echo "start producer"

kubectl apply -f producer/producer.yaml
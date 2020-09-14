#!/bin/bash

state="wait"
pods=`kubectl get pods -n kafka -o custom-columns=":metadata.name"`

wait_ready(){

    sleep 10s

    while [ "wait" = "$state" ]; do
        

        error=false

        for pod in $pods
        do
            podState=`kubectl get pods -n kafka | grep "\b$pod\b" | awk '{print$2}'`
            podRunning=`kubectl get pods -n kafka | grep "\b$pod\b" | awk '{print$3}'`
            
            echo "state $podRunning"

            if [ "$podRunning" != "Running" ]; then
                echo "wrong state $podRunning"
                error=true
            fi
            if [ "$podState" != "1/1" ]; then
                echo "wrong state $podState"
                error=true
            fi

        done

        if [ $error = false ]; then
            sleep 10s
            return
        fi

        sleep 1s

    done

}


wait_ready

echo "start analyst"

kubectl apply -f analyst/analyst.yaml

wait_ready

echo "start filter"

#kubectl apply -f analyst/filter.yaml

#wait_ready

#echo "start producer"

#kubectl apply -f analyst/producer.yaml
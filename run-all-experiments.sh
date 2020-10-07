#!/bin/bash

counter=0


while [ "$counter" -lt 4 ]; do
    
    bash run-real-deployment.sh $counter

    sleep 20s

    bash run-real-experiment.sh $counter

    sleep 20s

    cd MockFog2

    bash destroy.sh

    cd ..

    counter=$((counter+1))
done
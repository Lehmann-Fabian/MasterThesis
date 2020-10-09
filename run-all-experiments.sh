#!/bin/bash

experiment=0
counter=0


while [ "$counter" -lt 2 ]; do
    
    bash run-real-deployment.sh $experiment

    sleep 20s

    bash run-real-experiment.sh $experiment $counter

    sleep 20s

    cd MockFog2

    bash destroy.sh

    cd ..

    counter=$((counter+1))
done
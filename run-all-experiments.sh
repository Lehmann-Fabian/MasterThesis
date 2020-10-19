#!/bin/bash

mkdir -p ./results/runs/

experiment=1
counter=0


while [ "$counter" -lt 1 ]; do

    echo exp $experiment run $counter
    
    bash run-one-experiment.sh $experiment $counter | tee results/runs/exp$experiment-$counter.log

    counter=$((counter+1))
done
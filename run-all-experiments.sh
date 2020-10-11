#!/bin/bash

experiment=0
counter=0


while [ "$counter" -lt 3 ]; do

    echo exp $experiment run $counter
    
    bash run-one-experiment.sh $experiment $counter &> results/runs/exp$experiment-$counter.log

    counter=$((counter+1))
done
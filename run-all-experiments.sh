#!/bin/bash

mkdir -p ./results/runs/

experimentStart=0
counter=0


experiment=$experimentStart

while [ "$experiment" -lt 4 ]; do
    while [ "$counter" -lt 3 ]; do

        echo exp $experiment run $counter

        bash run-one-experiment-wrapper.sh $experiment $counter
        counter=$((counter+1))

    done
    experiment=$((experiment+1))
    counter=0
done

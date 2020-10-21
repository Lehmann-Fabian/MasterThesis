#!/bin/bash

mkdir -p ./results/runs/

experiment=1
counter=0


while [ "$counter" -lt 1 ]; do

    echo exp $experiment run $counter

    bash run-one-experiment-wrapper.sh $experiment $counter
    

    counter=$((counter+1))
done

exit 0

experiment=3
counter=0


while [ "$counter" -lt 2 ]; do

    echo exp $experiment run $counter

    bash run-one-experiment-wrapper.sh $experiment $counter
    

    counter=$((counter+1))
done

experiment=0
counter=0


while [ "$counter" -lt 3 ]; do

    echo exp $experiment run $counter

    bash run-one-experiment-wrapper.sh $experiment $counter
    

    counter=$((counter+1))
done
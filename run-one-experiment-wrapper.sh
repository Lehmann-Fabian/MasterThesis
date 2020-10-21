#!/bin/bash

code=124

while [[ "$code" -eq 124 ]]; do

    rm results/runs/exp$1-$2.log
    bash run-one-experiment.sh $1 $2 | tee results/runs/exp$1-$2.log
    code=$?

done
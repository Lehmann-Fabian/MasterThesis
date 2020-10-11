#!/bin/bash

bash run-real-deployment.sh $1

sleep 20s

bash run-real-experiment.sh $1 $2

sleep 20s

cd MockFog2

bash destroy.sh

cd ..
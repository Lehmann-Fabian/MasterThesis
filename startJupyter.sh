#!/bin/bash
#run this: bash run-test-deployment.sh 

docker run --rm -p 8888:8888 -e JUPYTER_ENABLE_LAB=yes -v "$PWD":/home/jovyan/work --name notebook jupyter/datascience-notebook
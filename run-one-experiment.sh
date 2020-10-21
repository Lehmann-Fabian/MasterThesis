#!/bin/bash



timeout --foreground 35m bash <<EOT
    bash run-real-deployment.sh $1
EOT

code=$?

if [[ "$code" -ne 0 ]]; then
    cd MockFog2
    bash destroy.sh
    cd ..
    exit 124

fi

sleep 20s

timeout --foreground 50m bash <<EOT
    bash run-real-experiment.sh $1 $2
EOT

code=$?

if [[ "$code" -ne 0 ]]; then
    cd MockFog2
    bash destroy.sh
    cd ..
    exit 124

fi

sleep 20s

cd MockFog2
bash destroy.sh
cd ..
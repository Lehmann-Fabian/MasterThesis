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

code=124
while [[ "$code" -eq 124 ]]; do

timeout --foreground 5m bash <<EOT
    bash destroy.sh
EOT
code=$?

done

cd ..
exit 0
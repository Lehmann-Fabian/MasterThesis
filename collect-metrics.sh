#!/bin/bash

while true; 
do 
    time=`date +%s`
    docker_processes=`sudo docker ps --format "table {{.ID}};{{.Names}};{{.Image}};{{.Status}}" | sed -e "s/ /_/g" | awk '{if(NR > 1) print $0}'` 
    echo Time $time  >> /tmp/ps.log
    for p in $docker_processes; do
        containerID=`echo $p | awk -F";" '{print$1}'`
        containerName=`echo $p | awk -F";" '{print$2}'`
        containerImage=`echo $p | awk -F";" '{print$3}'`
        containerStatus=`echo $p | awk -F";" '{print$4}'`
        pid=`sudo docker inspect -f '{{.State.Pid}}' $containerID`
        result=`ps -o pcpu,pmem,pid,time --pid $pid | awk '{if(NR > 1) print $0}'`
        if [ ! -z "$result" ]; then
            echo $result $containerName $containerImage $containerStatus $pid  >> /tmp/ps.log
        fi
        result=""
    done
    sleep 1; 
done

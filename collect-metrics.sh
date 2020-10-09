#!/bin/bash

while true; 
do 
    time=`date +%s`
    (echo "%CPU %MEM ARGS $time" && ps -e -o pcpu,pmem,pid,time,args --sort=pid | cut -d" " -f1-10) >> /tmp/ps.log; 
    sleep 2; 
done

#!/bin/bash

while true; 
do 
    (echo "%CPU %MEM ARGS $(date)" && ps -e -o pcpu,pmem,args --sort=pcpu | cut -d" " -f1-5) >> /tmp/ps.log; 
    sleep 5; 
done

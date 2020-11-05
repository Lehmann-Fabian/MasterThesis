cd producer

REM cgroup propblem:
REM docker-machine ssh
REM sudo mkdir /sys/fs/cgroup/systemd
REM sudo mount -t cgroup -o none,name=systemd cgroup /sys/fs/cgroup/systemd
REM Can not resolve addresses
REM sudo sh -c 'echo "nameserver 8.8.8.8" > /etc/resolv.conf'

echo "Build Producer"
docker build -t producer .
docker tag producer reh69113/masterthesis:producer
docker push reh69113/masterthesis:producer

echo "Build Filter"
cd ../filter
docker build -t filter .
docker tag filter reh69113/masterthesis:filter
docker push reh69113/masterthesis:filter

echo "Build Analyst"
cd ../analyst
docker build -t analyst .
docker tag analyst reh69113/masterthesis:analyst
docker push reh69113/masterthesis:analyst

cd ..
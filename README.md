# README

# Dowadload and Use

git clone https://github.com/jkong85/dg-controller.git
cd dg-controller/
git checkout origin/dev
git checkout -b dev
git branch --set-upstream-to=origin/dev dev
git pull
git branch -a

# TODOLIST
- Install go compiler on each VM node
- Copy MongDB scripts and HTTP server to /opt/dgmongo on EACH VM node
- JUnit test framework
- Private docker reporeitory
- Save and load Docker images by Vagrant
```
#将镜像存储
cd vagrant-workdir
sudo docker save adoptopenjdk/openjdk8-openj9:jdk8u162-b12_openj9-0.8.0-alpine > jdk.tar
#导入镜像文件
docker load --input jdk.tar
#通过符号的方式来导入
docker load < jdk.tar
```

# Issues

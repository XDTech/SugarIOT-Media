#!/bin/sh

tag="media_server"
version="1.0.0"
region="registry.cn-hangzhou.aliyuncs.com"
workspace="xdsy_safety"

mvn clean && mvn package -DskipTests && docker build -t ${region}/${workspace}/${tag}:${version} . \
&& docker push ${region}/${workspace}/${tag}:${version}
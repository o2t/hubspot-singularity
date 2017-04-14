#!/bin/bash
set -e

RELEASE_VERSION=$1
NEW_VERSION=$2
USER_NAME=${3:-hubspot}

git checkout "Singularity-$RELEASE_VERSION"
mvn clean package docker:build -DskipTests

git checkout master
mvn clean package docker:build -DskipTests

docker tag $USER_NAME/singularityservice:$NEW_VERSION $USER_NAME/singularityservice:latest
docker tag $USER_NAME/singularityexecutorslave:$NEW_VERSION $USER_NAME/singularityexecutorslave:latest
docker push $USER_NAME/singularityservice:$RELEASE_VERSION && \ 
	docker push $USER_NAME/singularityservice:$NEW_VERSION && \
	docker push $USER_NAME/singularityservice:latest && \
	docker push $USER_NAME/singularityexecutorslave:$RELEASE_VERSION && \
	docker push $USER_NAME/singularityexecutorslave:$NEW_VERSION && \
	docker push $USER_NAME/singularityexecutorslave:latest


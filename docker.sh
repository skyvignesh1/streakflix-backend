#!/usr/bin/env bash

# Variable declarations
artifactName=streakflix-backend-1.0.0-SNAPSHOT.jar
containerName=streakflixbackendcontainer
tagName=streakflixbackend
containerPort=8081
localPort=8081

# Build docker image
docker build --build-arg ARTIFACT_NAME=$artifactName --build-arg PORT=$containerPort --tag $tagName .

# Run docker container
docker run --publish $localPort:$containerPort --name $containerName $tagName
# Jenkins with the tools our pipeline needs (Maven and JDK 25).
# Installed once when the image is built, so restarts stay fast.
FROM jenkins/jenkins:2.580.1-lts

USER root
RUN apt-get update \
    && apt-get install -y maven openjdk-25-jdk-headless \
    && rm -rf /var/lib/apt/lists/*
USER jenkins

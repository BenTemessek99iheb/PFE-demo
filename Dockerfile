FROM jenkins/jenkins:lts

USER root
  
  # Install Maven
RUN apt-get update && \
apt-get install -y maven && \
apt-get clean

# Install Docker CLI
RUN apt-get update && \
    apt-get install -y apt-transport-https ca-certificates curl software-properties-common && \
    curl -fsSL https://download.docker.com/linux/ubuntu/gpg | apt-key add - && \
    add-apt-repository "deb [arch=amd64] https://download.docker.com/linux/ubuntu focal stable" && \
    apt-get update && \
    apt-get install -y docker-ce-cli


USER jenkins

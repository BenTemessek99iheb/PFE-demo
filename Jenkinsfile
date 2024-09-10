pipeline {
    agent any

    tools {
        maven 'Maven 3.9.8'
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build and Test') {
            steps {
                sh 'mvn clean test'
            }
        }

        stage('SonarQube Analysis') {
            steps {
                script {
                    def mvn = tool 'Maven 3.9.8'
                   withSonarQubeEnv() {
                       sh "${mvn}/bin/mvn clean verify sonar:sonar -Dsonar.projectKey=ioit -Dsonar.projectName='ioit-dashboard' -Dsonar.login='sqa_7bcfb12aa4bd988098682fbbbc1832a79532936e'"
                     }
                }
            }
        }

                stage('Start Prometheus and Grafana') {
                    steps {
                        script {
                            sh 'docker run -d --name prometheus -p 9090:9090 prom/prometheus:latest'
                            sh 'docker run -d --name grafana -p 3000:3000 grafana/grafana:latest'
                        }
                    }
                }

                stage('Monitor Application') {
                    steps {
                        script {
                    // Monitoring steps if any can be added here.
                    // The application is already running as part of docker-compose
                        }
                    }
                }


    }

    post {
        success {
            echo 'Build and tests succeeded BROOOOO !'
        }
        failure {
            echo 'Build or tests failed!'
        }
    }
}

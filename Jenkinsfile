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
       stages {
           stage('Check Docker') {
               steps {
                   sh 'docker --version'
               }
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
                // If there are no monitoring steps, remove this script block
                echo 'Monitoring steps could be added here.'
            }
        }
    }

    post {
        success {
            echo 'Build and tests succeeded Yo !'
        }
        failure {
            echo 'Build or tests failed!'
        }
    }
}

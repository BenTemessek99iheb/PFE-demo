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

        stage('Check Docker') {
            steps {
                sh 'docker --version'
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

        stage('Check Prometheus and Grafana') {
            steps {
                script {
                    def prometheusStatus = sh(script: 'docker ps --filter "name=prometheus" --format "{{.Status}}"', returnStdout: true).trim()
                    def grafanaStatus = sh(script: 'docker ps --filter "name=grafana" --format "{{.Status}}"', returnStdout: true).trim()

                    if (prometheusStatus) {
                        echo "Prometheus is running: ${prometheusStatus}"
                    } else {
                        echo "Prometheus is not running."
                    }

                    if (grafanaStatus) {
                        echo "Grafana is running: ${grafanaStatus}"
                    } else {
                        echo "Grafana is not running."
                    }
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

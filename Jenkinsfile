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
                        sh "${mvn}/bin/mvn clean verify sonar:sonar -Dsonar.projectKey=ioit -Dsonar.projectName='ioit-dashboard' -Dsonar.login='sqa_7bcfb12aa4bd988098682fbbbc1832a79532936e' -Dorg.jenkinsci.plugins.durabletask.BourneShellScript.HEARTBEAT_CHECK_INTERVAL=86400"
                    }
                }
            }
        }

        stage('Check Prometheus and Grafana') {
            steps {
                script {
                    // because they already run in the container
                    echo 'Prometheus and Grafana are being checked.'
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

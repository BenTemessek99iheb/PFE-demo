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
                    // Check if Prometheus is reachable
                    def prometheusStatus = sh(script: 'curl -s -o /dev/null -w "%{http_code}" http://172.16.1.208:9090', returnStdout: true).trim()
                    if (prometheusStatus == '200') {
                        echo 'Prometheus is reachable and works!'
                    } else {
                        error 'Prometheus is not reachable!'
                    }

                    // Check if Grafana is reachable
                    def grafanaStatus = sh(script: 'curl -s -o /dev/null -w "%{http_code}" http://172.16.1.208:3000', returnStdout: true).trim()
                    if (grafanaStatus == '200') {
                        echo 'Grafana is reachable and works!'
                    } else {
                        error 'Grafana is not reachable!'
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

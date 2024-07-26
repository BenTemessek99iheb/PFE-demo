pipeline {
    agent any
 tools {
        maven 'Maven 3.9.8'
    }

    environment {
        SONAR_TOKEN = credentials('sqp_33a45c15a06f12199f4591ad0f4e4a6ad4cf7444') // Use the ID of the SonarQube token credential
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
                        sh "mvn clean verify sonar:sonar
                        -Dsonar.projectKey=ioit-dashboard
                        -Dsonar.projectName='ioit-dashboard'
                        -Dsonar.login=${env.SONAR_TOKEN}"
                    }
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

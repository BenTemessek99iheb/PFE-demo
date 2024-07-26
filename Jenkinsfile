pipeline {
    agent any

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
                    def mvn = tool 'Default Maven'
                    withSonarQubeEnv() {
                        sh "${mvn}/bin/mvn clean verify sonar:sonar -Dsonar.projectKey=ioit-dashboard -Dsonar.projectName='ioit-dashboard'"
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

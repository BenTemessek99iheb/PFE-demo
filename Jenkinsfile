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
                        sh "mvn clean verify sonar:sonar " +
                           "-Dsonar.projectKey=ioit " +
                           "-Dsonar.projectName='ioit-dashboard' " +
                           "-Dsonar.login='sqp_c4ab2ba3b47fe209977a8b4cab22bf0b48bc4497'"
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

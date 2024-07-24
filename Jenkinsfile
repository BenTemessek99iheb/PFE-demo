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

        stage('SonarQube analysis') {
            steps {
                withSonarQubeEnv('Sonar') {
                    script {
                        withSonarQubeEnv('Sonar') {
                            sh """
                                mvn sonar:sonar \
                                -Dsonar.projectKey=ioit \
                                -Dsonar.host.url=http://172.16.1.208:9000/ \
                                -Dsonar.login=sqp_dcb9e1f5f8e10bef20bd3c53da3338083ba87fd8

                            """
                        }
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

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
                                -Dsonar.login=sqa_122e87606771a250d5e229d7558d903e9b2bee3d
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

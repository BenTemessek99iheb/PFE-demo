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
                                -Dsonar.projectKey=ioit-dashboard \
                                -Dsonar.host.url=http://172.16.1.208:9000/ \
                                -Dsonar.login=sqp_33a45c15a06f12199f4591ad0f4e4a6ad4cf7444

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

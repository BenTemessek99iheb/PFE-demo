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
                                  ./mvnw sonar:sonar \
                                  -Dsonar.projectKey=ioit \
                                  -Dsonar.host.url=http://172.16.1.208:9000/ \
                                  -Dsonar.login=squ_b4673ea432bf65de1c22ad3f6827268d5b084e22
                              """
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

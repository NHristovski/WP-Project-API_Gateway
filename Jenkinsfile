pipeline {

    environment {
        registry = "nikolancaid/api-gateway"
        registryCredential = 'dockerhub'
        dockerImage = ''

    }
    agent any

    stages {
        stage('Git clone') {
            steps {
                git 'https://github.com/NHristovski/WP-Project-API_Gateway.git'
            }
        }

        stage('Building docker image') {
            steps {
                script {
                    dockerImage = docker.build registry + ":latest"
                }
            }
        }

        stage('Deploy Image') {
            steps {
                script {
                    docker.withRegistry('', registryCredential) {
                        dockerImage.push()
                    }
                }
            }
        }

        stage('Remove Unused docker image') {
            steps {
                sh "docker rmi $registry:latest"
            }
        }


        stage('Pull docker image on host') {
            steps {
                sh 'ssh -o StrictHostKeyChecking=no nhristov@10.10.10.57 "cat ~/docker_password.txt | docker login --username nikolancaid --password-stdin"'

                script {
                    try {
                        sh 'ssh -o StrictHostKeyChecking=no nhristov@10.10.10.57 "docker rm -f api-gateway"'
                    } catch (Exception e) {
                        sh 'echo "Failed to delete container api-gateway"'
                    }
                }

                sh 'ssh -o StrictHostKeyChecking=no nhristov@10.10.10.57 "docker pull nikolancaid/api-gateway:latest"'
            }
        }

        stage('Start the application'){
            steps {
                sh 'ssh -o StrictHostKeyChecking=no nhristov@10.10.10.57 "docker run --name api-gateway -p 8762:8762 -d nikolancaid/api-gateway:latest"'
            }
        }
    }
}

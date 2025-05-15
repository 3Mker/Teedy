pipeline {
    agent any

    environment {
        DEPLOYMENT_NAME = "hello-node"
        CONTAINER_NAME = "teedy"
        IMAGE_NAME = "3mker/teedy:latest"
        PATH = "/usr/local/bin:/opt/homebrew/bin:$PATH"
        MINIKUBE = "/opt/homebrew/bin/minikube"
        KUBECTL = "/opt/homebrew/bin/kubectl"
    }

    stages {
        stage('Start Minikube') {
            steps {
                sh '''
                if ! ${MINIKUBE} status | grep -q "Running"; then
                    echo "Starting Minikube..."
                    ${MINIKUBE} start
                else
                    echo "Minikube already running."
                fi
                '''
            }
        }

        stage('Set Image') {
            steps {
                sh '''
                echo "Setting image for deployment..."
                ${KUBECTL} set image deployment/${DEPLOYMENT_NAME} ${CONTAINER_NAME}=${IMAGE_NAME}
                '''
            }
        }

        stage('Verify') {
            steps {
                sh '''
                echo "Checking rollout status..."
                ${KUBECTL} rollout status deployment/${DEPLOYMENT_NAME}
                echo "Getting pod list..."
                ${KUBECTL} get pods
                '''
            }
        }
    }
}
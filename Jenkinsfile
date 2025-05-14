pipeline {
    agent any

    environment {
        // Docker Hub 的凭证 ID，确保在 Jenkins 的凭据管理中正确配置
        DOCKER_HUB_CREDENTIALS = '3Mker'
        // Docker 镜像名称
        DOCKER_IMAGE = '3mker/teedy'
        // Docker 标签，使用 Jenkins 构建编号
        DOCKER_TAG = "${env.BUILD_NUMBER}"
    }

    stages {
        stage('Build') {
            steps {
                // 检出代码，确保 URL 正确
                checkout scmGit(
                    branches: [[name: '*/master']],
                    userRemoteConfigs: [[url: 'https://github.com/3Mker/Teedy.git']]
                )
                // 构建 Maven 项目，确保 Maven 路径正确
                sh 'mvn -B -DskipTests clean package'
            }
        }

        stage('Building image') {
            steps {
                script {
                    // 构建 Docker 镜像
                    // sh "/usr/local/bin/docker build -t ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} ."
                    sudo docker.build("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}")
                }
            }
        }

        // stage('Login to Docker Hub') {
        //     steps {
        //         script {
        //             // 使用 Jenkins 凭据管理中的 Docker Hub 凭据
        //             withCredentials([usernamePassword(credentialsId: '12212606', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
        //                 sh '''
        //                     echo "$DOCKER_PASSWORD" | /usr/local/bin/docker login -u "$DOCKER_USER" --password-stdin
        //                 '''
        //             }
        //         }
        //     }
        // }

        stage('Upload image') {
            steps {
                script {
                    // 推送镜像
                    docker.withRegistry('https://registry.hub.docker.com', 
'DOCKER_HUB_CREDENTIALS') { 
// push image 
docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").push()
                    }
                }
            }
        }

        stage('Run containers') {
            steps {
                script {
                    script { 
// stop then remove containers if exists 
sh 'docker stop teedy-container-8081 || true' 
sh 'docker rm teedy-container-8081 || true' 
// run Container 
docker.image("${env.DOCKER_IMAGE}:${env.DOCKER_TAG}").run( 
'--name teedy-container-8081 -d -p 8081:8080' 
) 
// Optional: list all teedy-containers 
sh 'docker ps --filter "name=teedy-container"' 
}
                }
            }
        }
    }
}

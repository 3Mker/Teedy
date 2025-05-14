pipeline {
    agent any

    environment {
        // Docker Hub 的凭证 ID，确保在 Jenkins 的凭据管理中正确配置
        DOCKER_HUB_CREDENTIALS = '12213012'
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
                sh '/opt/homebrew/bin/mvn -B -DskipTests clean package'
            }
        }

        stage('Building image') {
            steps {
                script {
                    // 构建 Docker 镜像
                    sh "/usr/local/bin/docker build -t ${env.DOCKER_IMAGE}:${env.DOCKER_TAG} ."
                }
            }
        }

        stage('Login to Docker Hub') {
            steps {
                script {
                    // 使用 Jenkins 凭据管理中的 Docker Hub 凭据
                    withCredentials([usernamePassword(credentialsId: '12213012', usernameVariable: 'DOCKER_USER', passwordVariable: 'DOCKER_PASSWORD')]) {
                        sh '''
                            echo "$DOCKER_PASSWORD" | /usr/local/bin/docker login -u "$DOCKER_USER" --password-stdin
                        '''
                    }
                }
            }
        }

        stage('Upload image') {
            steps {
                script {
                    // 推送镜像
                    sh '''
                        /usr/local/bin/docker push "$DOCKER_IMAGE:$DOCKER_TAG"
                    '''
                }
            }
        }

        stage('Run containers') {
            steps {
                script {
                    // 定义端口列表
                    def ports = [8081, 8082, 8083]
                    
                    // 遍历端口，启动多个容器
                    ports.each { port ->
                        sh """
                            # 停止并移除旧容器（如果存在）
                            /usr/local/bin/docker stop teedy-container-${port} || true
                            /usr/local/bin/docker rm teedy-container-${port} || true
                            # 启动新容器
                            /usr/local/bin/docker run --name teedy-container-${port} -d -p ${port}:8080 "$DOCKER_IMAGE:$DOCKER_TAG"
                        """
                    }
                    
                    // 查看所有容器状态
                    sh "/usr/local/bin/docker ps --filter 'name=teedy-container-'"
                }
            }
        }
    }
}
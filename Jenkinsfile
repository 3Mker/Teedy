pipeline {
    agent any
    
    
    // 方法2: 在环境变量中设置Maven路径
    environment {
        macOS下Maven的路径 (如果上面的tools配置不起作用，可以取消下面注释)
        MAVEN_HOME = '/opt/homebrew/Cellar/maven/3.9.9'
        PATH = "${MAVEN_HOME}/bin:${PATH}"
    }
    
    stages {
        stage('Clean') {
            steps {
                sh 'mvn clean'
            }
        }
        stage('Compile') {
            steps {
                sh 'mvn compile'
            }
        }
        stage('Javadoc') {
            steps {
                sh 'mvn javadoc:javadoc'
            }
        }
        stage('Site') {
            steps {
                sh 'mvn site'
            }
        }
        stage('Package') {
            steps {
                sh 'mvn package -DskipTests'
            }
        }
    }
    post {
        always {
            archiveArtifacts artifacts: '**/target/site/**/*.*', fingerprint: true
            archiveArtifacts artifacts: '**/target/**/*.jar', fingerprint: true
            archiveArtifacts artifacts: '**/target/**/*.war', fingerprint: true
            junit '**/target/surefire-reports/*.xml'
        }
    }
}

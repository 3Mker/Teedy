pipeline {
    agent any
    stages {
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

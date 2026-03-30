pipeline {
    agent any
    
    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }
    
    environment {
        PYTHON_PATH = "${tool 'Python-3.10'}"
    }
    
    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }
        
        stage('Setup Python') {
            steps {
                sh '''
                    ${PYTHON_PATH}/pip install -r scripts/requirements.txt
                    cd scripts && ${PYTHON_PATH}/python train_model.py
                '''
            }
        }
        
        stage('Build') {
            steps {
                sh './mvnw clean compile'
            }
        }
        
        stage('Test') {
            steps {
                sh './mvnw test'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                }
            }
        }
        
        stage('Package JVM') {
            steps {
                sh './mvnw package -DskipTests'
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/quarkus-app/**/*', fingerprint: true
                }
            }
        }
        
        stage('Package Native') {
            agent {
                docker {
                    image 'ghcr.io/graalvm/graalvm-community:17'
                    args '-v $HOME/.m2:/root/.m2'
                }
            }
            steps {
                sh '''
                    gu install native-image
                    ./mvnw package -Dnative
                '''
            }
            post {
                success {
                    archiveArtifacts artifacts: 'target/*-runner', fingerprint: true
                }
            }
            when {
                branch 'main'
            }
        }
        
        stage('Docker Build') {
            steps {
                script {
                    docker.build("sklearn-model-service:${env.BUILD_ID}", "-f src/main/docker/Dockerfile.jvm .")
                }
            }
            when {
                branch 'main'
            }
        }
        
        stage('Integration Test') {
            steps {
                script {
                    docker.image("sklearn-model-service:${env.BUILD_ID}").withRun('-p 8080:8080') {
                        sh '''
                            sleep 30
                            curl -f http://localhost:8080/q/health
                            curl -f http://localhost:8080/api/model/status
                            curl -f -X POST http://localhost:8080/api/predict \
                                -H "Content-Type: application/json" \
                                -d '{"features": [5.1, 3.5, 1.4, 0.2]}'
                        '''
                    }
                }
            }
            when {
                branch 'main'
            }
        }
    }
    
    post {
        always {
            cleanWs()
        }
    }
}

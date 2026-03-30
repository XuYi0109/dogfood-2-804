pipeline {
    agent any

    tools {
        maven 'Maven-3.9'
        jdk 'JDK-17'
    }

    environment {
        MAVEN_OPTS = '-Xmx1024m'
        DOCKER_REGISTRY = credentials('docker-registry')
    }

    stages {
        stage('Checkout') {
            steps {
                checkout scm
            }
        }

        stage('Build') {
            steps {
                sh './mvnw clean compile -B'
            }
        }

        stage('Unit Tests') {
            steps {
                sh './mvnw test -B'
            }
            post {
                always {
                    junit 'target/surefire-reports/*.xml'
                    publishHTML([
                        allowMissing: false,
                        alwaysLinkToLastBuild: true,
                        keepAll: true,
                        reportDir: 'target/surefire-reports',
                        reportFiles: '*.html',
                        reportName: 'Test Reports'
                    ])
                }
            }
        }

        stage('Integration Tests') {
            steps {
                sh './mvnw verify -B'
            }
        }

        stage('Package JVM') {
            steps {
                sh './mvnw package -DskipTests -B'
                archiveArtifacts artifacts: 'target/quarkus-app/**', fingerprint: true
            }
        }

        stage('CICD Model Validation') {
            steps {
                script {
                    // Start application
                    sh 'java -jar target/quarkus-app/quarkus-run.jar &'
                    sh 'sleep 15'

                    // Health check
                    sh '''
                        echo "Testing health endpoint..."
                        curl -f http://localhost:8080/health/ready || exit 1
                        echo "✓ Health check passed"
                    '''

                    // List models
                    sh '''
                        echo "Testing model list..."
                        curl -f http://localhost:8080/api/v1/models || exit 1
                        echo "✓ Model list passed"
                    '''

                    // Validate model
                    sh '''
                        echo "Testing model validation..."
                        curl -f -X POST http://localhost:8080/api/v1/models/mock-model/validate || exit 1
                        echo "✓ Model validation passed"
                    '''

                    // Test prediction
                    sh '''
                        echo "Testing prediction..."
                        curl -f -X POST http://localhost:8080/api/v1/models/mock-model/predict \
                          -H "Content-Type: application/json" \
                          -d '{"data":[{"feature1":1.0,"feature2":2.0,"feature3":3.0}]}' || exit 1
                        echo "✓ Prediction test passed"
                    '''

                    // Validate all models
                    sh '''
                        echo "Testing all models validation..."
                        curl -f -X POST http://localhost:8080/api/v1/models/validate-all || exit 1
                        echo "✓ All models validation passed"
                    '''

                    // Stop application
                    sh 'pkill -f quarkus-run.jar || true'
                }
            }
        }

        stage('Build Native Image') {
            when {
                branch 'main'
            }
            steps {
                sh './mvnw package -Dnative -DskipTests -B'
                archiveArtifacts artifacts: 'target/*-runner', fingerprint: true
            }
        }

        stage('Build Docker Images') {
            when {
                anyOf {
                    branch 'main'
                    branch 'develop'
                }
            }
            steps {
                script {
                    // Build JVM image
                    sh 'docker build --target jvm -t sklearn-model-validator:jvm-${BUILD_NUMBER} .'
                    sh 'docker build --target jvm -t sklearn-model-validator:jvm-latest .'

                    // Build native image (if native build succeeded)
                    sh '''
                        if [ -f target/*-runner ]; then
                            docker build -f Dockerfile.native -t sklearn-model-validator:native-${BUILD_NUMBER} . || true
                            docker build -f Dockerfile.native -t sklearn-model-validator:native-latest . || true
                        fi
                    '''
                }
            }
        }

        stage('Deploy to Test') {
            when {
                branch 'develop'
            }
            steps {
                script {
                    sh '''
                        docker stop sklearn-validator-test || true
                        docker rm sklearn-validator-test || true
                        docker run -d -p 8080:8080 --name sklearn-validator-test sklearn-model-validator:jvm-latest
                    '''
                }
            }
        }
    }

    post {
        always {
            // Cleanup
            sh 'pkill -f quarkus-run.jar || true'

            // Clean workspace
            cleanWs()
        }
        success {
            echo 'Pipeline completed successfully!'
        }
        failure {
            echo 'Pipeline failed!'
        }
    }
}

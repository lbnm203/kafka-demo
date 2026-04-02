pipeline {
    agent any

    environment {
        // Khai báo biến cần sử dụng cho Pipeline
        DOCKER_CMD = 'docker-compose' 
    }

    stages {
        stage('Checkout Code') {
            steps {
                // Tự động kéo code từ branch đang cấu hình tại giao diện
                checkout scm
                echo 'Pull code thành công!'
            }
        }

        stage('Build Spring Boot (Backend)') {
            steps {
                dir('be_kafka') {
                    // Cấp quyền và chạy Gradle build để tạo file JAR (.jar)
                    sh 'chmod +x gradlew'
                    sh './gradlew clean build -x test' 
                }
            }
        }

        stage('Build React/Vite (Frontend)') {
            steps {
                dir('fe_kafka') {
                    // Cài library và build Frontend ra mục dist/
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Build Docker Images & Deploy') {
            steps {
                // Tắt các container hiện tại (nếu có), build lại image và up lên
                echo 'Đang khởi động Docker Compose với source code mới...'
                sh "${DOCKER_CMD} down"
                sh "${DOCKER_CMD} up -d --build"
            }
        }
    }

    post {
        success {
            echo "✅ Build & Deploy thành công! Cụm Kafka, Backend và Frontend đã sẵn sàng."
        }
        failure {
            echo "❌ Pipeline bị lỗi. Vui lòng kiểm tra lại Jenkins Log."
        }
    }
}

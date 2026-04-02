pipeline {
    agent any

    tools {
        // Khai báo để Jenkins tự động nạp NodeJS vào PATH
        // Tên 'node20' phải trùng với tên bạn đặt trong Manage Jenkins > Tools
        nodejs 'node20'
    }

    options {
        // Chỉ giữ lại log và dữ liệu của 3 lần Build gần nhất để tiết kiệm ổ cứng
        buildDiscarder(logRotator(numToKeepStr: '3'))
    }

    environment {
        DOCKER_CMD = 'docker compose' 
    }

    stages {
        stage('Checkout Code') {
            steps {
                checkout scm
                echo '✅ Pull code thành công!'
            }
        }

        stage('Build Spring Boot (Backend)') {
            steps {
                dir('be_kafka') {
                    sh 'chmod +x gradlew'
                    // Sử dụng ./gradlew để đảm bảo đúng version Gradle của dự án
                    sh './gradlew clean build -x test' 
                }
            }
        }

        stage('Build React/Vite (Frontend)') {
            steps {
                dir('fe_kafka') {
                    echo '🧹 Cleaning old files and installing dependencies...'
                    // Xóa node_modules và package-lock để tránh xung đột phiên bản cũ
                    sh 'rm -rf node_modules package-lock.json'
                    sh 'npm install'
                    sh 'npm run build'
                }
            }
        }

        stage('Build Docker Images & Deploy') {
            steps {
                echo '🚀 Đang khởi động Docker Compose với source code mới...'
                // Dùng dấu nháy kép để bao quanh biến môi trường
                sh "${DOCKER_CMD} down --remove-orphans"
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
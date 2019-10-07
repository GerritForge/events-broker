pipeline {
    agent { label 'bazel-debian' }
    stages {
        stage('build') {
            steps {
                gerritReview labels: [Verified: 0], message: "Build started: ${env.BUILD_URL}"
                sh 'mvn test'
            }
        }
    }
    post {
        success { gerritReview labels: [Verified: 1] }
        unstable { gerritReview labels: [Verified: 0], message: "Build is unstable: ${env.BUILD_URL}" }
        failure { gerritReview labels: [Verified: -1], message: "Build failed: ${env.BUILD_URL}" }
    }
}

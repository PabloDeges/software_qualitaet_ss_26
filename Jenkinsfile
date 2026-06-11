// ─────────────────────────────────────────────────────────────────────────────
//  Jenkinsfile  –  software_qualitaet_ss_26
//  Microservices: mqtt-project | rest-api | frontend (Vue 3 / Vite)
//  Registry:      GitHub Container Registry (ghcr.io)
//  Deploy target: remote server via SSH + docker compose
// ─────────────────────────────────────────────────────────────────────────────

pipeline {

    // Run on any agent that has Docker available.
    // If you have a dedicated agent label, replace 'any' with e.g. 'docker'.
    agent any

    // ── Environment ──────────────────────────────────────────────────────────
    environment {
        // GitHub Container Registry
        REGISTRY         = 'ghcr.io'
        GITHUB_OWNER     = 'YOUR_GITHUB_USERNAME'          // ← change me
        IMAGE_MQTT       = "${REGISTRY}/${GITHUB_OWNER}/mqtt-project"
        IMAGE_REST       = "${REGISTRY}/${GITHUB_OWNER}/rest-api"
        IMAGE_FRONTEND   = "${REGISTRY}/${GITHUB_OWNER}/frontend"

        // Jenkins credential IDs (configure these in Manage Jenkins → Credentials)
        GHCR_CREDENTIALS = 'ghcr-credentials'   // Username + Password (PAT)
        SSH_CREDENTIALS  = 'deploy-ssh-key'      // SSH private key
        DEPLOY_HOST      = 'YOUR_SERVER_IP'      // ← change me
        DEPLOY_USER      = 'ubuntu'              // ← change me
        DEPLOY_PATH      = '/opt/your-app'       // ← change me
    }

    // ── Pipeline options ─────────────────────────────────────────────────────
    options {
        // Keep the last 10 builds to avoid filling disk
        buildDiscarder(logRotator(numToKeepStr: '10'))
        // Fail the whole pipeline if a stage takes longer than 30 minutes
        timeout(time: 30, unit: 'MINUTES')
        // Coloured console output (requires AnsiColor plugin)
        ansiColor('xterm')
    }

    // ── Trigger: poll GitHub every minute (or use a GitHub webhook instead) ──
    triggers {
        githubPush()
    }

    // ═════════════════════════════════════════════════════════════════════════
    stages {

        // ── 1. Checkout ───────────────────────────────────────────────────────
        stage('Checkout') {
            steps {
                checkout scm
                script {
                    // Capture short commit SHA for image tagging
                    env.GIT_SHA = sh(returnStdout: true, script: 'git rev-parse --short HEAD').trim()
                    env.BRANCH  = env.GIT_BRANCH?.replaceAll('origin/', '') ?: 'unknown'
                    echo "Branch: ${env.BRANCH}  |  Commit: ${env.GIT_SHA}"
                }
            }
        }

        // ── 2. Build Java services in PARALLEL ───────────────────────────────
        // We run Maven inside Docker so no JDK/Maven installation is needed
        // on the Jenkins agent itself.
        stage('Build Java') {
            parallel {

                stage('Build mqtt-project') {
                    steps {
                        dir('mqtt-project') {
                            sh '''
                                docker run --rm \
                                  -v "$PWD":/app \
                                  -v maven-cache:/root/.m2 \
                                  -w /app \
                                  maven:3.9-eclipse-temurin-21 \
                                  mvn -B package -DskipTests
                            '''
                        }
                    }
                }

                stage('Build rest-api') {
                    steps {
                        dir('rest-api') {
                            sh '''
                                docker run --rm \
                                  -v "$PWD":/app \
                                  -v maven-cache:/root/.m2 \
                                  -w /app \
                                  maven:3.9-eclipse-temurin-21 \
                                  mvn -B package -DskipTests
                            '''
                        }
                    }
                }
            }
        }

        // ── 3. Test Java services in PARALLEL ────────────────────────────────
        // Tests need a MongoDB instance. We spin one up as a Docker sidecar
        // using the --network flag so Maven can reach it at localhost:27017.
        stage('Test Java') {
            parallel {

                stage('Test mqtt-project') {
                    steps {
                        dir('mqtt-project') {
                            sh '''
                                # Create an isolated network for this test run
                                docker network create mqtt-test-net-${BUILD_NUMBER} || true

                                # Start a temporary MongoDB
                                docker run -d --name mongo-mqtt-${BUILD_NUMBER} \
                                  --network mqtt-test-net-${BUILD_NUMBER} \
                                  mongo:7

                                # Run tests wired to that MongoDB
                                docker run --rm \
                                  -v "$PWD":/app \
                                  -v maven-cache:/root/.m2 \
                                  --network mqtt-test-net-${BUILD_NUMBER} \
                                  -e SPRING_DATA_MONGODB_URI=mongodb://mongo-mqtt-${BUILD_NUMBER}:27017/testdb \
                                  -e MQTT_BROKER=tcp://localhost:1883 \
                                  -e MQTT_USERNAME=test \
                                  -e MQTT_PASSWORD=test \
                                  -e MQTT_CLIENT_ID=test-client \
                                  -w /app \
                                  maven:3.9-eclipse-temurin-21 \
                                  mvn -B test
                            '''
                        }
                    }
                    post {
                        always {
                            sh '''
                                docker stop  mongo-mqtt-${BUILD_NUMBER} || true
                                docker rm    mongo-mqtt-${BUILD_NUMBER} || true
                                docker network rm mqtt-test-net-${BUILD_NUMBER} || true
                            '''
                            junit 'mqtt-project/target/surefire-reports/*.xml'
                        }
                    }
                }

                stage('Test rest-api') {
                    steps {
                        dir('rest-api') {
                            sh '''
                                docker network create rest-test-net-${BUILD_NUMBER} || true

                                docker run -d --name mongo-rest-${BUILD_NUMBER} \
                                  --network rest-test-net-${BUILD_NUMBER} \
                                  mongo:7

                                docker run --rm \
                                  -v "$PWD":/app \
                                  -v maven-cache:/root/.m2 \
                                  --network rest-test-net-${BUILD_NUMBER} \
                                  -e SPRING_DATA_MONGODB_URI=mongodb://mongo-rest-${BUILD_NUMBER}:27017/testdb \
                                  -e MQTT_BROKER=tcp://localhost:1883 \
                                  -e MQTT_USERNAME=test \
                                  -e MQTT_PASSWORD=test \
                                  -e MQTT_CLIENT_ID=test-rest-client \
                                  -w /app \
                                  maven:3.9-eclipse-temurin-21 \
                                  mvn -B test
                            '''
                        }
                    }
                    post {
                        always {
                            sh '''
                                docker stop  mongo-rest-${BUILD_NUMBER} || true
                                docker rm    mongo-rest-${BUILD_NUMBER} || true
                                docker network rm rest-test-net-${BUILD_NUMBER} || true
                            '''
                            junit 'rest-api/target/surefire-reports/*.xml'
                        }
                    }
                }

                stage('Test frontend') {
                    steps {
                        dir('frontend') {
                            sh '''
                                docker run --rm \
                                  -v "$PWD":/app \
                                  -w /app \
                                  node:22-alpine \
                                  sh -c "npm ci && npm run build"
                            '''
                        }
                    }
                }
            }
        }

        // ── 4. Build Docker images (only on main branch) ─────────────────────
        stage('Docker Build') {
            when {
                branch 'main'
            }
            parallel {

                stage('Image: mqtt-project') {
                    steps {
                        sh '''
                            docker build \
                              -t ${IMAGE_MQTT}:${GIT_SHA} \
                              -t ${IMAGE_MQTT}:latest \
                              ./mqtt-project
                        '''
                    }
                }

                stage('Image: rest-api') {
                    steps {
                        sh '''
                            docker build \
                              -t ${IMAGE_REST}:${GIT_SHA} \
                              -t ${IMAGE_REST}:latest \
                              ./rest-api
                        '''
                    }
                }

                stage('Image: frontend') {
                    steps {
                        sh '''
                            docker build \
                              -t ${IMAGE_FRONTEND}:${GIT_SHA} \
                              -t ${IMAGE_FRONTEND}:latest \
                              ./frontend
                        '''
                    }
                }
            }
        }

        // ── 5. Push to ghcr.io (only on main branch) ─────────────────────────
        stage('Push to GHCR') {
            when {
                branch 'main'
            }
            steps {
                withCredentials([usernamePassword(
                    credentialsId: env.GHCR_CREDENTIALS,
                    usernameVariable: 'GHCR_USER',
                    passwordVariable: 'GHCR_TOKEN'
                )]) {
                    sh '''
                        echo "$GHCR_TOKEN" | docker login ghcr.io -u "$GHCR_USER" --password-stdin

                        docker push ${IMAGE_MQTT}:${GIT_SHA}
                        docker push ${IMAGE_MQTT}:latest

                        docker push ${IMAGE_REST}:${GIT_SHA}
                        docker push ${IMAGE_REST}:latest

                        docker push ${IMAGE_FRONTEND}:${GIT_SHA}
                        docker push ${IMAGE_FRONTEND}:latest
                    '''
                }
            }
        }

        // ── 6. Deploy to server (only on main branch) ─────────────────────────
        stage('Deploy') {
            when {
                branch 'main'
            }
            steps {
                withCredentials([sshUserPrivateKey(
                    credentialsId: env.SSH_CREDENTIALS,
                    keyFileVariable: 'SSH_KEY'
                )]) {
                    sh '''
                        ssh -o StrictHostKeyChecking=no \
                            -i "$SSH_KEY" \
                            ${DEPLOY_USER}@${DEPLOY_HOST} << 'ENDSSH'

                            cd /opt/your-app

                            # Pull the freshly built images
                            docker pull ghcr.io/YOUR_GITHUB_USERNAME/mqtt-project:latest
                            docker pull ghcr.io/YOUR_GITHUB_USERNAME/rest-api:latest
                            docker pull ghcr.io/YOUR_GITHUB_USERNAME/frontend:latest

                            # Restart only changed containers (zero-downtime rolling update)
                            docker compose up -d --remove-orphans

                            # Clean up dangling images
                            docker image prune -f

ENDSSH
                    '''
                }
            }
        }
    }
    // ═════════════════════════════════════════════════════════════════════════

    // ── Post-pipeline notifications ──────────────────────────────────────────
    post {
        success {
            echo "✅  Pipeline passed  |  Branch: ${env.BRANCH}  |  Commit: ${env.GIT_SHA}"
        }
        failure {
            echo "❌  Pipeline FAILED  |  Branch: ${env.BRANCH}  |  Commit: ${env.GIT_SHA}"
            // Uncomment to send an email:
            // mail to: 'team@example.com',
            //      subject: "FAILED: ${env.JOB_NAME} #${env.BUILD_NUMBER}",
            //      body:    "Check ${env.BUILD_URL}"
        }
        always {
            // Remove locally built images to keep agent disk clean
            sh '''
                docker rmi ${IMAGE_MQTT}:${GIT_SHA}    || true
                docker rmi ${IMAGE_MQTT}:latest        || true
                docker rmi ${IMAGE_REST}:${GIT_SHA}    || true
                docker rmi ${IMAGE_REST}:latest        || true
                docker rmi ${IMAGE_FRONTEND}:${GIT_SHA} || true
                docker rmi ${IMAGE_FRONTEND}:latest    || true
            '''
        }
    }
}

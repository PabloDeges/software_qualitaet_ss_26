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
        GITHUB_OWNER     = 'PabloDeges'       
        IMAGE_MQTT       = "${REGISTRY}/${GITHUB_OWNER}/mqtt-project"
        IMAGE_REST       = "${REGISTRY}/${GITHUB_OWNER}/rest-api"
        IMAGE_FRONTEND   = "${REGISTRY}/${GITHUB_OWNER}/frontend"

        // Jenkins credential IDs (configure these in Manage Jenkins → Credentials)
        GHCR_CREDENTIALS = 'ghcr-credentials'   // Username + Password (PAT)
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

        stage('Build Java') {
            parallel {

                stage('Build mqtt-project') {
                    steps {
                        dir('mqtt-project') {
                            sh 'mvn -B package -DskipTests'
                        }
                    }
                }

                stage('Build rest-api') {
                    steps {
                        dir('rest-api') {
                            sh 'mvn -B package -DskipTests'
                        }
                    }
                }
            }
        }

        // ── 3. Test Java services in PARALLEL ────────────────────────────────
        stage('Test Java') {
    parallel {

        stage('Test mqtt-project') {
            steps {
                dir('mqtt-project') {
                    sh 'mvn -B test -Dspring.data.mongodb.uri=mongodb://localhost:27017/testdb -Dmqtt.broker=tcp://localhost:1883 -Dmqtt.username=test -Dmqtt.password=test -Dmqtt.client-id=test-mqtt'
                }
            }
            post {
                always {
                    junit 'mqtt-project/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Test rest-api') {
            steps {
                dir('rest-api') {
                    sh 'mvn -B test -Dspring.data.mongodb.uri=mongodb://localhost:27017/testdb -Dmqtt.broker=tcp://localhost:1883 -Dmqtt.username=test -Dmqtt.password=test -Dmqtt.client-id=test-rest'
                }
            }
            post {
                always {
                    junit 'rest-api/target/surefire-reports/*.xml'
                }
            }
        }

        stage('Test frontend') {
            steps {
                dir('frontend') {
                    sh 'npm ci && npm run build'
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

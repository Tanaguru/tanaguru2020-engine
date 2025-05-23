def COLOR_MAP = [
    'SUCCESS': 'good', 
    'FAILURE': 'danger',
    'UNSTABLE': 'warning',
]

def createDockerEnvFileContent(String propertyFileName){
    configFileProvider([configFile(fileId: propertyFileName, variable: 'configFile')]) {
         def props = readProperties file: "$configFile"
         return "SERVER_ADDRESS=" + props['SERVER_ADDRESS'] + "\n" +
             "SERVER_PORT=" + props['SERVER_PORT'] + "\n" +
             "DB_URL=" + props['DB_URL'] + "\n" +
             "DB_USERNAME=" + props['DB_USERNAME'] + "\n" +
             "DB_PASSWORD=" + props['DB_PASSWORD'] + "\n" +
             "MAIL_FROM_ADDRESS=" + props['MAIL_FROM_ADDRESS'] + "\n" +
             "MAIL_HOST=" + props['MAIL_HOST'] + "\n" +
             "MAIL_PORT=" + props['MAIL_PORT'] + "\n" +
             "MAIL_USERNAME=" + props['MAIL_USERNAME'] + "\n" +
             "MAIL_PASSWORD=" + props['MAIL_PASSWORD'] + "\n" +
             "MAIL_TTLS_ENABLED=false\n" +
             "MAIL_SMTP_AUTH=false\n" +
             "CRYPTO_KEY=" + props['CRYPTO_KEY'] + "\n" +
             "PASSWORD_TOKEN_VALIDITY=" + props['PASSWORD_TOKEN_VALIDITY'] + "\n" +
             "AUDITRUNNER_PROXY_EXCLUSION_URLS=" + props['AUDITRUNNER_PROXY_EXCLUSION_URLS'] + "\n" +
             "AUDITRUNNER_PROXY_USERNAME=" + props['AUDITRUNNER_PROXY_USERNAME'] + "\n" +
             "AUDITRUNNER_PROXY_PASSWORD=" + props['AUDITRUNNER_PROXY_PASSWORD'] + "\n" +
             "AUDITRUNNER_PROXY_PORT=" + props['AUDITRUNNER_PROXY_PORT'] + "\n" +
             "AUDITRUNNER_PROXY_HOST=" + props['AUDITRUNNER_PROXY_HOST'] + "\n" +
             "AUDITRUNNER_IMPLICIT_WAIT=" + props['AUDITRUNNER_IMPLICIT_WAIT'] + "\n" +
             "AUDITRUNNER_PAGE_LOAD_TIMEOUT=" + props['AUDITRUNNER_PAGE_LOAD_TIMEOUT'] + "\n" +
             "AUDITRUNNER_SCRIPT_TIMEOUT=" + props['AUDITRUNNER_SCRIPT_TIMEOUT'] + "\n" +
             "AUDITRUNNER_FIREFOX_PROFILE=" + props['AUDITRUNNER_FIREFOX_PROFILE'] + "\n" +
             "AUDITRUNNER_MAX_CONCURRENT_AUDITS=" + props['AUDITRUNNER_MAX_CONCURRENT_AUDITS'] + "\n" +
             "CORS_ORIGIN=" + props['CORS_ORIGIN'] + "\n" +
             "WEBAPP_URL=" + props['WEBAPP_URL'] + "\n" +
             "SESSION_TIMEOUT=" + props['SESSION_TIMEOUT'] + "\n" +
             "AUDITRUNNER_ACTIVE_BROWSER=" + props['AUDITRUNNER_ACTIVE_BROWSER'] + "\n" +
             "STATISTICS_FIXED_DELAY=" + props['STATISTICS_FIXED_DELAY'] + "\n" +
             "SPRING_PROFILE_ACTIVE=" + props['SPRING_PROFILE_ACTIVE'] + "\n" +
             "OAUTH2_ENABLED=" + props['OAUTH2_ENABLED'] + "\n" +
             "OAUTH2_USER_INFO_URI=" + props['OAUTH2_USER_INFO_URI'] + "\n" +
             "OAUTH2_CLIENT_ID=" + props['OAUTH2_CLIENT_ID'] + "\n" +
             "OAUTH2_CLIENT_SECRET=" + props['OAUTH2_CLIENT_SECRET'] + "\n" +
             "OAUTH2_REDIRECT_URI=" + props['OAUTH2_REDIRECT_URI'] + "\n" +
             "OAUTH2_TOKEN_URI=" + props['OAUTH2_TOKEN_URI'] + "\n" +
             "OAUTH2_OIDC_ENABLED=" + props['OAUTH2_OIDC_ENABLED'] + "\n" +
             "OAUTH2_OIDC_JWK=" + props['OAUTH2_OIDC_JWK'] + "\n" +
             "OAUTH2_USER_CREATE_IF_NOT_FOUND=" + props['OAUTH2_USER_CREATE_IF_NOT_FOUND'] + "\n" + 
             "OAUTH2_USER_CREATE_CONTRACT=" + props['OAUTH2_USER_CREATE_CONTRACT'] + "\n" +
             "OAUTH2_TOKEN_VALIDITY=" + props['OAUTH2_TOKEN_VALIDITY'] + "\n" +
             "OAUTH2_JWT_SECRET=" + props['OAUTH2_JWT_SECRET'] + "\n" +
             "OAUTH2_OIDC_ISS=" + props['OAUTH2_OIDC_ISS'] + "\n" +
             "MESSAGE_LANG=" + props['MESSAGE_LANG'] + "\n" +
             "HIKARI_MIN_IDLE=" + props['HIKARI_MIN_IDLE'] + "\n" +
             "HIKARI_MAX_POOL_SIZE=" + props['HIKARI_MAX_POOL_SIZE'] + "\n" +
             "HIKARI_CONNECTION_TIMEOUT=" + props['HIKARI_CONNECTION_TIMEOUT'] + "\n" +
             "HIKARI_IDLE_TIMEOUT=" + props['HIKARI_IDLE_TIMEOUT'] + "\n" +
             "HIKARI_MAX_LIFETIME=" + props['HIKARI_MAX_LIFETIME'] + "\n" +
             "TOMCAT_THREADS_MAX=" + props['TOMCAT_THREADS_MAX'] + "\n" +
             "TOMCAT_THREADS_MIN_SPARE=" + props['TOMCAT_THREADS_MIN_SPARE']
    }
}

pipeline {
    agent any
    stages {
        stage('Build') {
            agent {
                    docker 'maven'
            }
            steps {
                sh 'mvn clean package -X'
                sh '''
                REST_VERSION=$(mvn -q \
                    -Dexec.executable=echo \
                    -Dexec.args='${project.version}' \
                    --non-recursive \
                    exec:exec)
                echo ${REST_VERSION} > version.txt
                '''
                stash name: 'tanaguru2020-rest', includes: 'tanaguru-rest/target/tanaguru2020-rest-*.tar.gz'
                stash name: 'version', includes: 'version.txt'
            }
        }

        stage('Build docker image') {
            when {
                anyOf{
                    branch 'develop'
                    branch 'master'
                    branch 'beta'
                }
            }
            steps {
                git(url: "https://github.com/Tanaguru/tanaguru2020-docker", branch: "master", credentialsId: "f310470a-0f9a-44d3-8738-0b57bfa91fa6")
                unstash 'tanaguru2020-rest'
                unstash 'version'
                sh '''
                REST_VERSION=$(cat version.txt)
                mv tanaguru-rest/target/tanaguru2020-rest-*.tar.gz ./tanaguru2020-rest/image/tanaguru2020-rest-${REST_VERSION}.tar.gz
                docker build -t tanaguru2020-rest:${REST_VERSION} \
                    --build-arg TANAGURU_REST_ARCHIVE_PATH=tanaguru2020-rest-${REST_VERSION}.tar.gz \
                    --build-arg FIREFOX_VERSION=93.0 \
                    --build-arg GECKODRIVER_VERSION=0.29.1 \
                    ./tanaguru2020-rest/image/
                '''
            }
        }

        stage('Deploy dev') {
            when {
                anyOf{
                    branch 'develop'
                }
            }
            steps {
                script{
                    unstash 'version'
                    def devDockerEnv = createDockerEnvFileContent('812179c5-a3f7-4664-aa64-72e047016d28');
                    writeFile file: "./.env", text: devDockerEnv
                    sh '''
                    REST_VERSION=$(cat version.txt)
                    cat ./.env

                    docker rename tanaguru2020-rest-dev tanaguru2020-rest-dev-old || true
                    docker stop tanaguru2020-rest-dev-old || true
                    docker image prune -f

                    docker run -d --rm \
                        --name tanaguru2020-rest-dev \
                        --shm-size=2gb \
                        --env-file ./.env \
                        --label "traefik.enable=true" \
                        --label "traefik.frontend.redirect.entryPoint=secure" \
                        --label "traefik.http.routers.tanaguru-rest-dev.entrypoints=secure" \
                        --label "traefik.http.routers.tanaguru-rest-dev.rule=Host(\\`devapi.tanaguru.com\\`)" \
                        --label "traefik.http.routers.tanaguru-rest-dev.tls=true" \
                        --label "traefik.port=9002" \
                        --network=web \
                        tanaguru2020-rest:${REST_VERSION}
                    '''
                }

            }
        }

        stage('Deploy prod') {
            when {
                branch 'master'
            }
            steps {
                script{
                    unstash 'version'
                    def devDockerEnv = createDockerEnvFileContent('647f5360-4c98-456b-aa1f-0d2a3ea62f43');
                    writeFile file: "./.env", text: devDockerEnv
                    sh '''
                    REST_VERSION=$(cat version.txt)
                    cat ./.env

                    docker rename tanaguru2020-rest-prod tanaguru2020-rest-prod-old || true
                    docker stop tanaguru2020-rest-prod-old || true
                    docker image prune -f

                    docker run -d --rm \
                        --name tanaguru2020-rest-prod \
                        --shm-size=2gb \
                        --env-file ./.env \
                        --label "traefik.enable=true" \
                        --label "traefik.frontend.redirect.entryPoint=secure" \
                        --label "traefik.http.routers.tanaguru-rest-prod.entrypoints=secure" \
                        --label "traefik.http.routers.tanaguru-rest-prod.rule=Host(\\`prodapi.tanaguru.com\\`)" \
                        --label "traefik.http.routers.tanaguru-rest-prod.tls=true" \
                        --label "traefik.port=9002" \
                        --network=web \
                        tanaguru2020-rest:${REST_VERSION}
                    '''
                }

            }
        }

        stage('Store packages') {
            when {
                anyOf{
                    branch 'master'
                    branch 'beta'
                }
            }
            steps {
                unstash 'tanaguru2020-rest'
                unstash 'version'

                sh '''
                    REST_VERSION=$(cat version.txt)
                    DIR=/html/tanaguru2020-rest/${REST_VERSION}
                    if [ -d "$DIR" ]; then rm -Rf $DIR; fi
                    mkdir -p $DIR
                    mv -f tanaguru-rest/target/tanaguru2020-rest-*.tar.gz /html/tanaguru2020-rest/${REST_VERSION}/tanaguru2020-rest-${REST_VERSION}.tar.gz
                    chown 1000:1000 /html/tanaguru2020-rest/${REST_VERSION}/tanaguru2020-rest-${REST_VERSION}.tar.gz
                '''
            }
        }

        stage('Push beta image to registry') {
            when {
                branch 'beta'
            }
            steps {
                script{
                    unstash 'version'
                    def TIMESTAMP =sh(
                        script: "date +%Y-%m-%d",
                        returnStdout: true
                    ).trim()

                    def REST_VERSION = sh(
                        script: "cat version.txt",
                        returnStdout: true
                    ).trim()

                    def image = docker.image("tanaguru2020-rest:${REST_VERSION}")
                    docker.withRegistry("https://registry.tanaguru.com", "registry") {
                        image.push("beta-${TIMESTAMP}")
                    }
                }
            }
        }

        stage('Push image to registry') {
            when {
                branch 'master'
            }
            steps {
                unstash 'version'
                script{
                    script{
                        def REST_VERSION = sh(
                            script: "cat version.txt",
                            returnStdout: true
                        ).trim()

                        def image = docker.image("tanaguru2020-rest:${REST_VERSION}")
                        docker.withRegistry("https://registry.tanaguru.com", "registry") {
                            image.push()
                        }
                    }
                }
            }
        }
    }
}

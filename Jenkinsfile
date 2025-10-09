pipeline {
    agent any
    environment {
        ANDROID_COMPILE_SDK = "36"
        ANDROID_BUILD_TOOLS = "33.0.2"
        ANDROID_SDK_TOOLS = "9477386"
        ANDROID_HOME = "${WORKSPACE}/android-sdk-root"
        // Используем общий кеш Gradle — аналог m2/repository
        GRADLE_USER_HOME = "${HOME}/.gradle"
        // Обновленные параметры памяти
        //GRADLE_OPTS = "-XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8"
      	//GRADLE_OPTS = "-XX:+HeapDumpOnOutOfMemoryError -Dfile.encoding=UTF-8 -Xmx1024m"
        TEST_OPTS = "-Dorg.gradle.workers.max=1 -Dkotlin.compiler.execution.strategy=in-process"
        GRADLE_OPTS = "-Dfile.encoding=UTF-8 -Xmx512m"
        APP_VERSION = "0.0.${BUILD_NUMBER}"
    }
    stages {
        stage('Setup Agent') {
            steps {
                script {
                    sh '''
                    echo "RAM total: $(free -m | awk '/^Mem:/ { print $2 \" MB\" }')" || true
                    echo "RAM total: $(awk '/MemTotal/ {printf \"%.2f GB\\n\", $2/1024/1024}' /proc/meminfo)" || true
                    echo "CPU cores: $(grep -c ^processor /proc/cpuinfo)" || true
                    echo "CPU cores: $(nproc)" || true

                    java -version
                    echo "JAVA_HOME: $JAVA_HOME"

                    '''
                  	sh '''
                    if [ ! -f /swapfile ]; then
                      sudo fallocate -l 2G /swapfile
                      sudo chmod 600 /swapfile
                      sudo mkswap /swapfile
                      sudo swapon /swapfile
                      echo "✅ Swap включён"
                    else
                      echo "ℹ️ Swap уже существует"
                    fi
                    '''
                }
            }
        }
        stage('Setup Android SDK') {
            steps {
                script {
                    sh '''
                    if [ -d "$ANDROID_HOME" ]; then rm -rf "$ANDROID_HOME"; fi
                    mkdir -p $ANDROID_HOME
                    wget --no-verbose --output-document=$ANDROID_HOME/cmdline-tools.zip \
                        https://dl.google.com/android/repository/commandlinetools-linux-${ANDROID_SDK_TOOLS}_latest.zip
                    unzip -q -d "$ANDROID_HOME/cmdline-tools" "$ANDROID_HOME/cmdline-tools.zip"
                    rm -rf "$ANDROID_HOME/cmdline-tools/tools"
                    mv -T "$ANDROID_HOME/cmdline-tools/cmdline-tools" "$ANDROID_HOME/cmdline-tools/tools"
                    export PATH=$PATH:$ANDROID_HOME/cmdline-tools/tools/bin
                    yes | sdkmanager --licenses > /dev/null || true
                    sdkmanager "platforms;android-${ANDROID_COMPILE_SDK}"
                    sdkmanager "platform-tools"
                    sdkmanager "build-tools;${ANDROID_BUILD_TOOLS}"
                    '''
                }
            }
        }
        stage('Clean and Prepare') {
            steps {
                script {
                    sh '''
                    ./gradlew --stop || true
                    ./gradlew clean || true
                    rm -rf ~/.gradle/caches
                    rm -rf ~/.gradle/daemon
                    '''
                }
            }
        }
        stage('Build') {
            steps {
                script {
                    sh '''
                    ./gradlew clean assembleRelease \
                        --no-daemon \
                        --build-cache \
                        --stacktrace \
                        --info \
                        --max-workers=2 \
                        -Dkotlin.compiler.execution.strategy=in-process \
                        -Dorg.gradle.testing.maxParallelForks=1 \
                        -Pandroid.testOptions.unitTests.all.maxParallelForks=1\
                        -PversName=${APP_VERSION} \
                        -PversCode=${BUILD_NUMBER}
                    '''
                }
            }
        }
        stage('Build Signed AAB') {
            steps {
                withCredentials([
                        file(credentialsId: 'keystore', variable: 'KEYSTORE_FILE'),
                        string(credentialsId: 'ANDROID_KEYSTORE_PASSWORD', variable: 'KEYSTORE_PASSWORD'),
                        usernamePassword(credentialsId: 'ANDROID_KEY', usernameVariable: 'KEY_ALIAS', passwordVariable: 'KEY_PASSWORD')
                ]) {
                    sh '''
                    ./gradlew bundleRelease \
                        --no-daemon \
                        --build-cache \
                        --stacktrace \
                        --info \
                        --max-workers=2 \
                        -Dkotlin.compiler.execution.strategy=in-process \
                        -Dorg.gradle.testing.maxParallelForks=1 \
                        -Pandroid.testOptions.unitTests.all.maxParallelForks=1 \
                        -Pandroid.injected.signing.store.file="${KEYSTORE_FILE}" \
                        -Pandroid.injected.signing.store.password="${KEYSTORE_PASSWORD}" \
                        -Pandroid.injected.signing.key.alias="${KEY_ALIAS}" \
                        -Pandroid.injected.signing.key.password="${KEY_PASSWORD}" \
                        -PversName=${APP_VERSION} \
                        -PversCode=${BUILD_NUMBER}
                    '''
                }
            }
        }
        stage('Archive APK') {
            steps {
                archiveArtifacts artifacts: '**/*.apk', fingerprint: true
            }
        }
        stage('Archive AAB') {
            steps {
                archiveArtifacts artifacts: '**/*.aab', fingerprint: true
            }
        }


        stage('Deploy to RuStore') {
            when {
                anyOf {
                    branch 'master'
                    branch 'develop'
                }
            }
            steps {
                withCredentials([
                        usernamePassword(credentialsId: 'RUSTORE_CREDENTIALS', usernameVariable: 'KEY_ID', passwordVariable: 'PRIVATE_KEY_CONTENT')
                ]) {
                    script {
                        sh '''
                set -e

                echo "==> Получение параметров..."

                PKG_NAME="ru.dada.tuda"    # TODO: заменить на переменную из CI
                APP_NAME="DADA-TUDA"                 # TODO: заменить на переменную из CI
                APP_TYPE="MAIN"
  				echo "сохраненный VERSION_ID=${VERSION_ID}"
                APK_PATH=$(find . -name "*.aab" | grep release | head -n 1)

                if [ -z "$APK_PATH" ]; then
                    echo "AAB не найден!"
                    exit 1
                fi

                echo "==> Генерация подписи RuStore..."

                TIMESTAMP=$(date -u +"%Y-%m-%dT%H:%M:%S.%3NZ")
                MESSAGE="${KEY_ID}${TIMESTAMP}"

                TMP_KEY=$(mktemp)
                echo "$PRIVATE_KEY_CONTENT" | base64 --decode > "$TMP_KEY"

                SIGNATURE=$(echo -n "$MESSAGE" | openssl dgst -sha512 -sign "$TMP_KEY" | base64 | tr -d '\\n')
                rm "$TMP_KEY"

                AUTH_PAYLOAD=$(jq -c -n --arg keyId "$KEY_ID" --arg ts "$TIMESTAMP" --arg sig "$SIGNATURE"                     '{keyId: $keyId, timestamp: $ts, signature: $sig}')

                echo "==> Запрос токена..."
                TOKEN_RESPONSE=$(curl --silent --show-error                     --header "Content-Type: application/json"                     --request POST "https://public-api.rustore.ru/public/auth/"                     --data "$AUTH_PAYLOAD")

                TOKEN=$(echo "$TOKEN_RESPONSE" | jq -r .body.jwe)

                if [ -z "$TOKEN" ] || [ "$TOKEN" == "null" ]; then
                    echo "Ошибка авторизации: не удалось получить токен"
                    exit 1
                fi

                echo "OK Токен получен"

                echo "==> Создание черновика версии"
                CREATE_DRAFT_RESPONSE=$(curl --silent                     --location --request POST "https://public-api.rustore.ru/public/v1/application/$PKG_NAME/version"                     --header "Content-Type: application/json"                     --header "Public-Token: $TOKEN"                     --data "{ \\"appName\\": \\"$APP_NAME\\", \\"publishType\\": \\"MANUAL\\", \\"appType\\": \\"$APP_TYPE\\" }")

                VERSION_ID_NEW=$(echo "$CREATE_DRAFT_RESPONSE" | jq -r '.body')
                VERSION_ID=''
                if [ -z "$VERSION_ID_NEW" ] || [ "$VERSION_ID_NEW" = "null" ]; then
                	VERSION_ID=$(echo "$CREATE_DRAFT_RESPONSE" | grep -oE 'ID = [0-9]+' | sed 's/ID = //')
                    echo "Ошибка создания черновика версии, использую $VERSION_ID "
                else
                  VERSION_ID="$VERSION_ID_NEW"
                  echo "OK Черновик создан. VERSION_ID: $VERSION_ID"
                fi

                echo "==> Загрузка APK $APK_PATH в черновик $VERSION_ID"

                UPLOAD_RESPONSE=$(curl --silent                      --location --request POST "https://public-api.rustore.ru/public/v1/application/$PKG_NAME/version/$VERSION_ID/aab?servicesType=Unknown&isMainApk=true"                     --header "Public-Token: $TOKEN"                     --form "file=@$APK_PATH")

                echo "OK Загрузка завершена. Ответ:"
                echo "$UPLOAD_RESPONSE"
                MODERATE_RESPONSE=$(curl --silent --location --request POST "https://public-api.rustore.ru/public/v1/application/$PKG_NAME/version/$VERSION_ID/commit"     --header "Public-Token: $TOKEN")
                echo "$MODERATE_RESPONSE"
                echo " Завершено"
                '''
                    }
                }
            }
        }



        stage('Deploy') {
            when {
                anyOf {
                    branch 'master'
                    branch 'develop'
                }
            }
            steps {
                script {
                    sh '''
                    if [ ! -d ".ci" ]; then mkdir -p .ci; fi
                    if [ ! -f ".ci/deployment_done" ]; then
                        echo "Running post-build script for the first time..."
                        chmod +x ./build_and_deploy.sh && ./build_and_deploy.sh && touch .ci/deployment_done
                    else
                        echo "Post-build script has already been executed. Skipping..."
                    fi
                    '''
                }

            }
        }
    }
    options {
        lock 'gradle'
        skipStagesAfterUnstable()
        disableConcurrentBuilds()
        timeout(time: 30, unit: 'MINUTES')
    }
    post {
        always {
            script {
                sh './gradlew --stop || true'
            }
        }
    }
}

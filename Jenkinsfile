pipeline {
    agent none
    environment {
        ANDROID_COMPILE_SDK = "36"
        ANDROID_BUILD_TOOLS = "33.0.2"
        ANDROID_SDK_TOOLS = "9477386"
        ANDROID_HOME = "${WORKSPACE}/android-sdk-root"
        GRADLE_USER_HOME = "${HOME}/.gradle"
        TEST_OPTS = "-Dorg.gradle.workers.max=1 -Dkotlin.compiler.execution.strategy=in-process"
        GRADLE_OPTS = "-Dfile.encoding=UTF-8 -Xmx512m"
        APP_VERSION = "0.0.${BUILD_NUMBER}"
        IOS_SCHEME = 'iosApp'
        IOS_PROJECT = 'iosApp/iosApp.xcodeproj'
        IOS_CONFIGURATION = 'Release'
    }
    stages {
        stage('Setup Agent') {
            agent { label 'android' }
            steps {
                script {
                    sh '''
                    echo "RAM total: $(free -m | awk '/^Mem:/ { print $2 " MB" }')" || true
                    echo "RAM total: $(awk '/MemTotal/ {printf "%.2f GB\n", $2/1024/1024}' /proc/meminfo)" || true
                    echo "CPU cores: $(grep -c ^processor /proc/cpuinfo)" || true
                    echo "CPU cores: $(nproc)" || true
                    
                    java -version
                    echo "JAVA_HOME: $JAVA_HOME"
                    
                    '''
                  	sh '''
                    if [ ! -f /swapfile ]; then
                      sudo fallocate -l 2G /swapfile || sudo dd if=/dev/zero of=/swapfile bs=1M count=2048
                      sudo chmod 600 /swapfile
                      sudo mkswap /swapfile
                      sudo swapon /swapfile
                      echo "Swap включён"
                    fi
                    '''
                }
            }
        }
        stage('Setup Android SDK') {
            agent { label 'android' }
            steps {
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
                sdkmanager "platforms;android-${ANDROID_COMPILE_SDK}" "platform-tools" "build-tools;${ANDROID_BUILD_TOOLS}"
                '''
            }
        }
        stage('Clean and Prepare') {
            agent { label 'android' }
            steps { sh './gradlew clean || true' }
        }
        stage('Build APK') {
            agent { label 'android' }
            steps {
                sh '''
                ./gradlew assembleRelease \
                  --no-daemon --build-cache --stacktrace --info --max-workers=2 \
                  -Dkotlin.compiler.execution.strategy=in-process \
                  -Dorg.gradle.testing.maxParallelForks=1 \
                  -Pandroid.testOptions.unitTests.all.maxParallelForks=1 \
                  -PversName=${APP_VERSION} -PversCode=${BUILD_NUMBER}
                '''
            }
        }
        stage('Build Signed AAB') {
            agent { label 'android' }
            steps {
                withCredentials([
                  file(credentialsId: 'keystore', variable: 'KEYSTORE_FILE'),
                  string(credentialsId: 'ANDROID_KEYSTORE_PASSWORD', variable: 'KEYSTORE_PASSWORD'),
                  usernamePassword(credentialsId: 'ANDROID_KEY', usernameVariable: 'KEY_ALIAS', passwordVariable: 'KEY_PASSWORD')
                ]) {
                    sh '''
                    ./gradlew bundleRelease \
                      --no-daemon --build-cache --stacktrace --info --max-workers=2 \
                      -Dkotlin.compiler.execution.strategy=in-process \
                      -Dorg.gradle.testing.maxParallelForks=1 \
                      -Pandroid.testOptions.unitTests.all.maxParallelForks=1 \
                      -Pandroid.injected.signing.store.file="${KEYSTORE_FILE}" \
                      -Pandroid.injected.signing.store.password="${KEYSTORE_PASSWORD}" \
                      -Pandroid.injected.signing.key.alias="${KEY_ALIAS}" \
                      -Pandroid.injected.signing.key.password="${KEY_PASSWORD}" \
                      -PversName=${APP_VERSION} -PversCode=${BUILD_NUMBER}
                    '''
                }
            }
        }
        stage('Archive Android Artifacts') {
            agent { label 'android' }
            steps {
                archiveArtifacts artifacts: '**/*.apk', fingerprint: true, allowEmptyArchive: true
                archiveArtifacts artifacts: '**/*.aab', fingerprint: true, allowEmptyArchive: true
            }
        }
        stage('Build iOS') {
            agent { label 'ios' }
            options { timeout(time: 40, unit: 'MINUTES') }
            steps {
                withCredentials(optional: true, bindings: [string(credentialsId: 'APPLE_TEAM_ID', variable: 'DEVELOPER_TEAM_ID')]) {
                    sh '''
                    set -e
                    echo "==> iOS: обновление версий Info.plist"
                    INFO_PLIST="iosApp/iosApp/Info.plist"
                    if [ ! -f "$INFO_PLIST" ]; then echo "Info.plist не найден"; exit 1; fi
                    /usr/libexec/PlistBuddy -c "Print :CFBundleShortVersionString" "$INFO_PLIST" >/dev/null 2>&1 || \
                      /usr/libexec/PlistBuddy -c "Add :CFBundleShortVersionString string ${APP_VERSION}" "$INFO_PLIST"
                    /usr/libexec/PlistBuddy -c "Set :CFBundleShortVersionString ${APP_VERSION}" "$INFO_PLIST"
                    /usr/libexec/PlistBuddy -c "Print :CFBundleVersion" "$INFO_PLIST" >/dev/null 2>&1 || \
                      /usr/libexec/PlistBuddy -c "Add :CFBundleVersion string ${BUILD_NUMBER}" "$INFO_PLIST"
                    /usr/libexec/PlistBuddy -c "Set :CFBundleVersion ${BUILD_NUMBER}" "$INFO_PLIST"
                    echo "==> iOS: сборка"
                    xcodebuild -version
                    xcodebuild \
                      -project ${IOS_PROJECT} \
                      -scheme ${IOS_SCHEME} \
                      -configuration ${IOS_CONFIGURATION} \
                      -sdk iphoneos \
                      -archivePath iosApp/build/iosApp.xcarchive \
                      CODE_SIGN_STYLE=Automatic \
                      archive | xcpretty || true
                    if [ -n "${DEVELOPER_TEAM_ID}" ] && [ "${DEVELOPER_TEAM_ID}" != "null" ]; then
                      cat > ExportOptions.plist <<EOF
<?xml version=\"1.0\" encoding=\"UTF-8\"?>
<!DOCTYPE plist PUBLIC \"-//Apple//DTD PLIST 1.0//EN\" \"http://www.apple.com/DTDs/PropertyList-1.0.dtd\">
<plist version=\"1.0\">
<dict>
  <key>method</key><string>ad-hoc</string>
  <key>signingStyle</key><string>automatic</string>
  <key>teamID</key><string>${DEVELOPER_TEAM_ID}</string>
  <key>compileBitcode</key><false/>
  <key>stripSwiftSymbols</key><true/>
</dict>
</plist>
EOF
                      xcodebuild -exportArchive -archivePath iosApp/build/iosApp.xcarchive -exportPath iosApp/build/export -exportOptionsPlist ExportOptions.plist | xcpretty || true
                    else
                      echo "TEAM_ID отсутствует — пропуск экспорта IPA"
                    fi
                    '''
                }
            }
        }
        stage('Archive iOS Artifacts') {
            agent { label 'ios' }
            steps {
                archiveArtifacts artifacts: 'iosApp/build/iosApp.xcarchive/**/*', fingerprint: true, allowEmptyArchive: true
                archiveArtifacts artifacts: 'iosApp/build/export/*.ipa', fingerprint: true, allowEmptyArchive: true
                archiveArtifacts artifacts: 'iosApp/build/iosApp.xcarchive/dSYMs/*.dSYM/**/*', fingerprint: true, allowEmptyArchive: true
            }
        }
        stage('Deploy to RuStore') {
            agent { label 'android' }
            when { anyOf { branch 'master'; branch 'develop' } }
            steps {
                withCredentials([
                  usernamePassword(credentialsId: 'RUSTORE_CREDENTIALS', usernameVariable: 'KEY_ID', passwordVariable: 'PRIVATE_KEY_CONTENT')
                ]) {
                    sh '''
                    set -e
                    PKG_NAME="ru.dada.tuda"
                    APP_NAME="DADA-TUDA"
                    APP_TYPE="MAIN"
                    AAB=$(find . -name "*.aab" | grep release | head -n 1)
                    if [ -z "$AAB" ]; then echo "AAB не найден"; exit 1; fi
                    TS=$(date -u +"%Y-%m-%dT%H:%M:%S.%3NZ")
                    MSG="${KEY_ID}${TS}"
                    TMP=$(mktemp)
                    echo "$PRIVATE_KEY_CONTENT" | base64 --decode > $TMP
                    SIG=$(echo -n "$MSG" | openssl dgst -sha512 -sign $TMP | base64 | tr -d '\n')
                    rm $TMP
                    AUTH=$(jq -c -n --arg keyId "$KEY_ID" --arg ts "$TS" --arg sig "$SIG" '{keyId:$keyId,timestamp:$ts,signature:$sig}')
                    TOKEN=$(curl --silent --show-error -H 'Content-Type: application/json' -d "$AUTH" -X POST https://public-api.rustore.ru/public/auth/ | jq -r .body.jwe)
                    [ -z "$TOKEN" -o "$TOKEN" = null ] && echo 'Нет токена' && exit 1
                    CREATE=$(curl --silent -H 'Content-Type: application/json' -H "Public-Token: $TOKEN" -d "{\"appName\":\"$APP_NAME\",\"publishType\":\"MANUAL\",\"appType\":\"$APP_TYPE\"}" -X POST https://public-api.rustore.ru/public/v1/application/$PKG_NAME/version)
                    VID=$(echo "$CREATE" | jq -r .body)
                    [ -z "$VID" -o "$VID" = null ] && VID=$(echo "$CREATE" | grep -oE 'ID = [0-9]+' | sed 's/ID = //')
                    echo "VERSION_ID=$VID"
                    curl --silent -H "Public-Token: $TOKEN" -F "file=@$AAB" -X POST https://public-api.rustore.ru/public/v1/application/$PKG_NAME/version/$VID/aab?servicesType=Unknown&isMainApk=true >/dev/null
                    curl --silent -H "Public-Token: $TOKEN" -X POST https://public-api.rustore.ru/public/v1/application/$PKG_NAME/version/$VID/commit >/dev/null
                    echo 'RuStore deploy done'
                    '''
                }
            }
        }
        stage('Deploy (Custom Script)') {
            agent { label 'android' }
            when { anyOf { branch 'master'; branch 'develop' } }
            steps {
                sh '''
                mkdir -p .ci
                if [ ! -f .ci/deployment_done ]; then
                  echo 'Running custom deploy script...'
                  chmod +x ./build_and_deploy.sh && ./build_and_deploy.sh && touch .ci/deployment_done
                else
                  echo 'Custom deploy already done.'
                fi
                '''
            }
        }
    }
    options {
        lock 'gradle'
        skipStagesAfterUnstable()
        disableConcurrentBuilds()
        timeout(time: 40, unit: 'MINUTES')
    }
    post {
        always {
            script { sh './gradlew --stop || true' }
        }
    }
}

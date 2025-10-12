#!/bin/bash

# Скрипт для локальной сборки iOS приложения
# Использование: ./build_ios.sh [simulator|device|framework]

set -e

echo "==> iOS Build Script для DADATUDA"

# Определяем тип сборки
BUILD_TYPE=${1:-"simulator"}
APP_VERSION=${2:-"1.0.0"}
BUILD_NUMBER=${3:-"1"}

# Параметры приложения
IOS_APP_NAME="DADATUDA"
IOS_BUNDLE_ID="ru.dada.tuda.DADATUDA"

echo "==> Тип сборки: $BUILD_TYPE"
echo "==> Версия: $APP_VERSION (Build: $BUILD_NUMBER)"

# Проверяем среду
if [[ "$OSTYPE" != "darwin"* ]]; then
    echo "❌ Этот скрипт работает только на macOS"
    exit 1
fi

if ! command -v xcodebuild >/dev/null 2>&1; then
    echo "❌ Xcode не найден"
    exit 1
fi

# Очистка
echo "==> Очистка..."
./gradlew clean

case $BUILD_TYPE in
    "framework")
        echo "==> Сборка только XCFramework..."
        ./gradlew :composeApp:assembleXCFramework \
            --no-daemon \
            --build-cache \
            --stacktrace \
            -PversName=$APP_VERSION \
            -PversCode=$BUILD_NUMBER

        echo "✅ XCFramework собран в composeApp/build/XCFrameworks/"
        ;;

    "simulator")
        echo "==> Сборка XCFramework..."
        ./gradlew :composeApp:assembleXCFramework \
            --no-daemon \
            --build-cache \
            --stacktrace \
            -PversName=$APP_VERSION \
            -PversCode=$BUILD_NUMBER

        echo "==> Сборка iOS приложения для симулятора..."
        cd iosApp

        # Обновляем версию
        echo "CURRENT_PROJECT_VERSION=$BUILD_NUMBER" > Configuration/Config.xcconfig
        echo "MARKETING_VERSION=$APP_VERSION" >> Configuration/Config.xcconfig
        echo "TEAM_ID=" >> Configuration/Config.xcconfig
        echo "PRODUCT_NAME=$IOS_APP_NAME" >> Configuration/Config.xcconfig
        echo "PRODUCT_BUNDLE_IDENTIFIER=$IOS_BUNDLE_ID" >> Configuration/Config.xcconfig

        xcodebuild -project iosApp.xcodeproj \
            -scheme iosApp \
            -sdk iphonesimulator \
            -configuration Release \
            -derivedDataPath ./DerivedData \
            clean build \
            CODE_SIGNING_ALLOWED=NO \
            ONLY_ACTIVE_ARCH=NO

        cd ..
        echo "✅ Приложение собрано для симулятора"
        echo "📱 Путь: iosApp/DerivedData/Build/Products/Release-iphonesimulator/"
        ;;

    "device")
        echo "==> Сборка XCFramework..."
        ./gradlew :composeApp:assembleXCFramework \
            --no-daemon \
            --build-cache \
            --stacktrace \
            -PversName=$APP_VERSION \
            -PversCode=$BUILD_NUMBER

        echo "==> Сборка iOS приложения для устройств..."

        if [ -z "$IOS_TEAM_ID" ]; then
            echo "❌ Для сборки на устройство необходимо установить переменную IOS_TEAM_ID"
            echo "   Пример: export IOS_TEAM_ID=XXXXXXXXXX"
            exit 1
        fi

        cd iosApp

        # Обновляем версию
        echo "CURRENT_PROJECT_VERSION=$BUILD_NUMBER" > Configuration/Config.xcconfig
        echo "MARKETING_VERSION=$APP_VERSION" >> Configuration/Config.xcconfig
        echo "TEAM_ID=$IOS_TEAM_ID" >> Configuration/Config.xcconfig
        echo "PRODUCT_NAME=$IOS_APP_NAME" >> Configuration/Config.xcconfig
        echo "PRODUCT_BUNDLE_IDENTIFIER=$IOS_BUNDLE_ID$IOS_TEAM_ID" >> Configuration/Config.xcconfig

        xcodebuild -project iosApp.xcodeproj \
            -scheme iosApp \
            -sdk iphoneos \
            -configuration Release \
            -derivedDataPath ./DerivedData \
            -archivePath ./build/$IOS_APP_NAME.xcarchive \
            clean archive \
            DEVELOPMENT_TEAM="$IOS_TEAM_ID"

        # Экспорт IPA (опционально)
        if [ -f "./build/$IOS_APP_NAME.xcarchive" ]; then
            echo "==> Создание IPA файла..."

            # Создаем ExportOptions.plist
            cat > ./build/ExportOptions.plist << EOF
<?xml version="1.0" encoding="UTF-8"?>
<!DOCTYPE plist PUBLIC "-//Apple//DTD PLIST 1.0//EN" "http://www.apple.com/DTDs/PropertyList-1.0.dtd">
<plist version="1.0">
<dict>
    <key>method</key>
    <string>development</string>
    <key>teamID</key>
    <string>$IOS_TEAM_ID</string>
</dict>
</plist>
EOF

            xcodebuild -exportArchive \
                -archivePath ./build/$IOS_APP_NAME.xcarchive \
                -exportPath ./build/ \
                -exportOptionsPlist ./build/ExportOptions.plist

            echo "✅ IPA создан: iosApp/build/$IOS_APP_NAME.ipa"
        fi

        cd ..
        echo "✅ Приложение собрано для устройств"
        ;;

    *)
        echo "❌ Неизвестный тип сборки: $BUILD_TYPE"
        echo "Доступные типы: simulator, device, framework"
        exit 1
        ;;
esac

echo "==> Сборка завершена ✅"

#!/bin/bash

modules=("core" "room" "sfubot")

read -p "Do you want to perform publishGitHubPackages dry run? (Y/N): " dry_run_choice

#Edit build.gradle
for module in "${modules[@]}"
do
    echo "=> Editing build.gradle for $module"
    gradle_file="/$(pwd)/$module/build.gradle"

    if [ "$module" == "core" ]; then
        sed -i '' 's#implementation project(":libwebrtc")#implementation "com.ntt.skyway:libwebrtc:${libwebrtcVer}"#' "$gradle_file"
    elif [ "$module" == "sfubot" ]; then
        sed -i '' 's#implementation project(":core")#implementation "com.ntt.skyway:core:${SkyWayVer}"#' "$gradle_file"
    elif [ "$module" == "room" ]; then
        sed -i '' 's#implementation project(":core")#api "com.ntt.skyway:core:${SkyWayVer}"#' "$gradle_file"
        sed -i '' 's#implementation project(":sfubot")#api "com.ntt.skyway:sfubot:${SkyWayVer}"#' "$gradle_file"
    fi
done

echo "=> Running Sync gradle"
./gradlew :prepareKotlinBuildScriptModel

#publishGitHubPackages
for module in "${modules[@]}"
do
    echo "=> Running publishGitHubPackages for $module"
    if [ "$dry_run_choice" == "Y" ] || [ "$dry_run_choice" == "y" ]; then
        ./gradlew :$module:publishGitHubPackagesPublicationToGitHubPackagesRepository --dry-run
    else
        ./gradlew :$module:publishGitHubPackagesPublicationToGitHubPackagesRepository
    fi

    # check the exit status of the command
    if [ $? -eq 0 ]; then
        echo "=> publishGitHubPackages for $module completed"
    else
        echo "=> publishGitHubPackages for $module failed"
    fi
done

echo "=> publishGitHubPackages for all modules completed"

afplay /$(pwd)/scripts/Ping.aiff


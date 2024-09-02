#!/bin/bash

modules_all=("libskywaytest" "core" "room" "sfubot")
modules_libskywaytest=("libskywaytest")
modules_others=("core" "room" "sfubot")

echo "Select the module set to run tests:"
options=("all" "libskywaytest" "android platform test (core, room, sfubot)")
select opt in "${options[@]}"
do
    case $opt in
        "all")
            modules=("${modules_all[@]}")
            break
            ;;
        "libskywaytest")
            modules=("${modules_libskywaytest[@]}")
            break
            ;;
        "android PF (core, room, sfubot)")
            modules=("${modules_others[@]}")
            break
            ;;
        *) echo "Invalid option $REPLY";;
    esac
done

echo "Select a target ABI for faster build:"
options=("arm64-v8a" "armeabi-v7a" "x86" "x86_64")
select opt in "${options[@]}"
do
    case $opt in
        "arm64-v8a")
            targetAbi="arm64-v8a"
            break
            ;;
        "armeabi-v7a")
            targetAbi="armeabi-v7a"
            break
            ;;
        "x86")
            targetAbi="x86"
            break
            ;;
        "x86_64")
            targetAbi="x86_64"
            break
            ;;
        *) echo "Invalid option $REPLY";;
    esac
done

echo "=> Running Sync gradle"
./gradlew :prepareKotlinBuildScriptModel

for module in "${modules[@]}"
do
    echo "=> Running tests for $module"
    ./gradlew :$module:connectedDebugAndroidTest -PtargetAbi=$targetAbi  -i | grep -E "> Task :$module:connectedDebugAndroidTest|tests on|SUCCESS|FAILED|XmlResultReporter"

    # check the exit status of the test command
    if [ $? -eq 0 ]; then
        echo "=> Tests for $module completed"
    else
        echo "=> Tests for $module failed"
    fi
done

echo "=> All tests completed"

for module in "${modules[@]}"
do
    echo "Module: $module"
    html_file="$module/build/reports/androidTests/connected/index.html"
    echo "=> Test Results: file://$(pwd)/$html_file"
done

afplay /$(pwd)/scripts/Ping.aiff


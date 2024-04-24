#!/bin/bash

modules=("core" "room" "sfubot")

echo "=> Running Sync gradle"
./gradlew :prepareKotlinBuildScriptModel

for module in "${modules[@]}"
do
    echo "=> Running tests for $module"
    ./gradlew :$module:connectedDebugAndroidTest -i | grep -E "> Task :$module:connectedDebugAndroidTest|tests on|SUCCESS|FAILED|XmlResultReporter"

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


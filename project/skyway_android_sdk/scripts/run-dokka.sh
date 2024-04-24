#!/bin/bash

modules=("core" "room" "sfubot")

for module in "${modules[@]}"
do
    echo "=> Running Dokka HTML for $module"
    ./gradlew :$module:dokkaHtml

    # check the exit status of the command
    if [ $? -eq 0 ]; then
        echo "=> Dokka HTML generation for $module completed"
    else
        echo "=> Dokka HTML generation for $module failed"
    fi
done

echo "=> Dokka HTML generation for all modules completed"

afplay /$(pwd)/scripts/Ping.aiff


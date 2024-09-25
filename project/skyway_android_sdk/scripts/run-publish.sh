#!/bin/bash

# Initialize flags for skip options
skip_clean=false
skip_deploy=false

# Process command-line arguments
modules=()
for arg in "$@"; do
    case $arg in
        --skip-clean)
            skip_clean=true
            ;;
        --skip-deploy)
            skip_deploy=true
            ;;
        *)
            modules+=("$arg") # Add any other arguments as modules
            ;;
    esac
done

# Check if any modules are passed as arguments
if [ ${#modules[@]} -eq 0 ]; then
    echo "No modules provided. Please provide at least one module."
    echo "avaliable module names are: core, room, sfubot, libwebrtc"
    echo "sample: run-publish.sh core room --skip-deploy"
    exit 1
else
    echo "Modules provided: ${modules[@]}"
fi

# Determine if dry run should be enabled
if [ "$skip_deploy" = false ]; then
    dry_run=""
    echo "Dry run mode not enabled."
else
    dry_run="--dry-run"
    echo "Dry run mode enabled for maven central deployment."
fi

echo "=> Running Sync gradle"
./gradlew :prepareKotlinBuildScriptModel


# Optionally skip cleaning
if [ "$skip_clean" = false ]; then
    echo "=> Clean SkyWay Android SDK Project"
    ./gradlew clean
else
    echo "=> Skipping clean process"
fi

# publish: save maven central packages to local folder
# build/maven-central-staging-deploy
publish_maven_central_success=true
for module in "${modules[@]}"
do
    echo "=> Running publishMavenCentral for $module"
    ./gradlew :$module:publishMavenCentralPublicationToMavenCentralRepository

    # check the exit status of the command
    if [ $? -eq 0 ]; then
        echo "=> publishMavenCentral for $module completed"

    else
        echo "=> publishGitHubPackages for $module failed"
        publish_maven_central_success=false
    fi
done

maven_central_local_location="$(pwd)/build/maven-central-staging-deploy"
echo "=> AAR's location for maven central: file://$maven_central_local_location"
open "$maven_central_local_location"
afplay /$(pwd)/scripts/Ping.aiff

# deploy maven central pacakges to maven central repository
# https://central.sonatype.com/search?q=skyway
if [ "$publish_maven_central_success" = true ]; then
    echo "=> All modules published successfully, deploy to maven central..."
    # do not run the submodule's jreleaserDeploy since it would cause a outputDirectory not found error
    # ref. https://github.com/jreleaser/jreleaser/discussions/1657
    # to run this, make sure you have the necessary secret keys in ~/.jreleaser/config.toml file.
    ./gradlew :jreleaserDeploy $dry_run
else
    echo "=> One or more modules failed to publish, skipping maven central deploy."
fi




#!/bin/bash
set -e

git config --global user.name "Travis CI"
git config --global user.email "noreply+travis@fossasia.org"

[[ $TRAVIS_BRANCH =~ ^(master)$ && $TRAVIS_REPO_SLUG =~ ^(fossasia/open-event-attendee-android)$ ]] && IS_PUBLISH_BRANCH=true || IS_PUBLISH_BRANCH=false
[[ $TRAVIS_BRANCH =~ ^(development)$ && $TRAVIS_REPO_SLUG =~ ^(fossasia/open-event-attendee-android)$ ]] && IS_DEV_BRANCH=true || IS_DEV_BRANCH=false
[[ $TRAVIS_PULL_REQUEST =~ ^(false)$ && $TRAVIS_REPO_SLUG =~ ^(fossasia/open-event-attendee-android)$ && $TRAVIS_BRANCH =~ ^(development|master)$ ]] && DEV_OR_MASTER_BUILD=true || DEV_OR_MASTER_BUILD=false
[[ $IS_PUBLISH_BRANCH && $TRAVIS_PULL_REQUEST_SLUG =~ ^(fossasia/open-event-attendee-android)$ ]] && PR_FOR_RELEASE=true || PR_FOR_RELEASE=false

if $PR_FOR_RELEASE; then
    FASTLANE_DRY_RUN="--validate_only true"
fi

if ! ( $DEV_OR_MASTER_BUILD || $PR_FOR_RELEASE ); then
	echo "We upload apk only for changes in development or master, and not PRs. So, let's skip this shall we ? :)"
	exit 0
fi

./gradlew bundlePlayStoreRelease

git clone --quiet --branch=apk https://fossasia:$GITHUB_API_KEY@github.com/fossasia/open-event-attendee-android apk > /dev/null
cd apk

if ! $PR_FOR_RELEASE; then
    if $IS_DEV_BRANCH; then
        /bin/rm -f open-event-attendee-dev-*
    else
        /bin/rm -f  *
    fi
fi

find ../app/build/outputs -type f -name '*.apk' -exec cp -v {} . \;
find ../app/build/outputs -type f -name '*.aab' -exec cp -v {} . \;


for file in app*; do
    if $IS_DEV_BRANCH; then
        if [[ ${file} =~ ".aab" ]]; then
                mv $file eventyay-attendee-dev-${file}
        else
                mv $file eventyay-attendee-dev-${file:4}
        fi

    else
        if [[ ${file} =~ ".aab" ]]; then
            mv $file eventyay-attendee-master-${file}
        else
            mv $file eventyay-attendee-master-${file:4}
        fi

    fi
done

if $IS_PUBLISH_BRANCH ;then
    cd ..
    gem install fastlane
    fastlane supply --aab ./apk/eventyay-attendee-master-app-playStore-release.aab --skip_upload_apk true --track alpha --json_key ./scripts/fastlane.json --package_name $PACKAGE_NAME $FASTLANE_DRY_RUN
    if $PR_FOR_RELEASE ;then
        exit 0
    fi
fi

# Create a new branch that will contains only latest apk
git checkout --orphan temporary

# Add generated APK
git add --all .
git commit -am "[Auto] Update Test Apk ($(date +%Y-%m-%d.%H:%M:%S))"

# Delete current apk branch
git branch -D apk
# Rename current branch to apk
git branch -m apk

# Force push to origin since histories are unrelated
git push origin apk --force --quiet > /dev/null

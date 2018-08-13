#!/bin/bash
set -e

git config --global user.name "Travis CI"
git config --global user.email "noreply+travis@fossasia.org"

export DEPLOY_BRANCH=${DEPLOY_BRANCH:-development}
export PUBLISH_BRANCH=${PUBLISH_BRANCH:-master}

if [ "$TRAVIS_PULL_REQUEST" != "false" -o "$TRAVIS_REPO_SLUG" != "fossasia/open-event-android" ] || ! [ "$TRAVIS_BRANCH" == "$DEPLOY_BRANCH" -o "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then
	echo "We upload apk only for changes in development or master, and not PRs. So, let's skip this shall we ? :)"
	exit 0
fi


git clone --quiet --branch=apk https://fossasia:$GITHUB_API_KEY@github.com/fossasia/open-event-android apk > /dev/null
cd apk

if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then
	/bin/rm -f  open-event-master-app-playStore-release.apk open-event-master-app-fdroid-release.apk open-event-master-app-playStore-debug.apk open-event-master-app-fdroid-debug.apk
else
	/bin/rm -f open-event-dev-app-fdroid-debug.apk open-event-dev-app-playStore-debug.apk open-event-dev-app-playStore-release.apk open-event-dev-app-fdroid-release.apk
fi

\cp -r ../app/build/outputs/apk/playStore/*/**.apk .
\cp -r ../app/build/outputs/apk/fdroid/*/**.apk .
\cp -r ../app/build/outputs/apk/playStore/debug/output.json playStore-debug-output.json
\cp -r ../app/build/outputs/apk/playStore/release/output.json playStore-release-output.json
\cp -r ../app/build/outputs/apk/fdroid/debug/output.json fdroid-debug-output.json
\cp -r ../app/build/outputs/apk/fdroid/release/output.json fdroid-release-output.json

if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then
	for file in app*; do
		if [[ $file = "open-event"* ]]; then
			continue
		fi
		cp $file open-event-master-${file%%}
	done
fi

if [ "$TRAVIS_BRANCH" == "$DEPLOY_BRANCH" ]; then
	for file in app*; do
		if [[ $file = "open-event"* ]]; then
			continue
		fi
		cp $file open-event-dev-${file%%}
	done
fi

# Signing Apps

if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then
    echo "Push to master branch detected, signing the app..."
    cp app-release-unsigned.apk app-release-unaligned.apk
	jarsigner -verbose -tsa http://timestamp.comodoca.com/rfc3161 -sigalg SHA1withRSA -digestalg SHA1 -keystore ../scripts/key.jks -storepass $STORE_PASS -keypass $KEY_PASS app-release-unaligned.apk $ALIAS
	${ANDROID_HOME}/build-tools/27.0.3/zipalign -v -p 4 app-release-unaligned.apk app-release.apk
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

# Publish App to Play Store
if [ "$TRAVIS_BRANCH" != "$PUBLISH_BRANCH" ]; then
    echo "We publish apk only for changes in master branch. So, let's skip this shall we ? :)"
    exit 0
fi

gem install fastlane
fastlane supply --apk test-app-release.apk --track alpha --json_key ../scripts/fastlane.json --package_name $PACKAGE_NAME
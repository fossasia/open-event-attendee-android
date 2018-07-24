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
	/bin/rm -f open-event-master-app-debug.apk open-event-master-app-release-unsigned.apk 
else
	/bin/rm -f open-event-master-app-fdroid-debug.apk open-event-master-app-playStore-debug.apk open-event-master-app-playStore-release.apk open-event-master-app-fdroid-release.apk
fi

\cp -r ../app/build/outputs/apk/playStore/*/**.apk .
\cp -r ../app/build/outputs/apk/fdroid/*/**.apk .
\cp -r ../app/build/outputs/apk/playStore/debug/output.json playStore-debug-output.json
\cp -r ../app/build/outputs/apk/playStore/release/output.json playStore-release-output.json
\cp -r ../app/build/outputs/apk/fdroid/debug/output.json fdroid-debug-output.json
\cp -r ../app/build/outputs/apk/fdroid/release/output.json fdroid-release-output.json

if [ "$TRAVIS_BRANCH" == "$PUBLISH_BRANCH" ]; then
	for file in app*; do
		cp $file open-event-master-${file%%}
	done
fi

if [ "$TRAVIS_BRANCH" == "$DEPLOY_BRANCH" ]; then
	for file in app*; do
		cp $file open-event-dev-${file%%}
	done
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

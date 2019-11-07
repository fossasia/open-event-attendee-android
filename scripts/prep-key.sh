#!/bin/sh
set -e

[[ $TRAVIS_REPO_SLUG =~ ^(fossasia/open-event-attendee-android)$ ]] && IS_FOSS_SLUG=true || IS_FOSS_SLUG=false
[[ $TRAVIS_PULL_REQUEST_SLUG =~ ^(fossasia/open-event-attendee-android)$ ]] && IS_FOSS_PR=true || IS_FOSS_PR=false
[[ $TRAVIS_BRANCH =~ ^(development|master)$ && $IS_FOSS_SLUG ]] && BRANCH_DEPLOYORDEV=true || BRANCH_DEPLOYORDEV=false

[[ $TRAVIS_BRANCH =~ ^(master)$ && $IS_FOSS_SLUG ]] && IS_PUBLISH_BRANCH=true || IS_PUBLISH_BRANCH=false

[[ $TRAVIS_PULL_REQUEST =~ ^(false)$ && $BRANCH_DEPLOYORDEV ]] && export DEV_OR_MASTER_BUILD=${DEV_OR_MASTER_BUILD:-true} || export DEV_OR_MASTER_BUILD=${DEV_OR_MASTER_BUILD:-false}

[[ $IS_PUBLISH_BRANCH && $IS_FOSS_PR =~ ^(true)$ ]] && export PR_FOR_RELEASE=${PR_FOR_RELEASE:-true} || export PR_FOR_RELEASE=${PR_FOR_RELEASE:-false}

if ! ( $DEV_OR_MASTER_BUILD || $PR_FOR_RELEASE ); then
    echo "We decrypt key only for pushes to the master branch and not PRs. So, skip."
    exit 0
fi

# Decrypt keys
openssl aes-256-cbc -K $encrypted_59a1db41ee4d_key -iv $encrypted_59a1db41ee4d_iv -in ./scripts/secrets.tar.enc -out ./scripts/secrets.tar -d
tar xvf ./scripts/secrets.tar -C scripts/

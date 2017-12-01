#!/bin/bash -e

targetUser=kiereleaseuser
remoteUrl=git@github.com:kiereleaseuser/appformer.git
DATE=$(date "+%Y-%m-%d")

# clone the repository and branch for appformer
git clone git@github.com:kiegroup/appformer.git --branch $baseBranch
cd $WORKSPACE/appformer
prBranch=appformer-upgrade-version-$DATE-$baseBranch
git checkout -b $prBranch $baseBranch
git remote add $targetUser $remoteUrl

# upgrades the version to next development version of appformer
sh scripts/release/update-version.sh $newVersion

#upgrades the errai version
sed -i "$!N;s/<version.org.kie>.*.<\/version.org.kie>/<version.org.kie>$kieVersion<\/version.org.kie>/;P;D" pom.xml
sed -i "$!N;s/<version.org.jboss.errai>.*.<\/version.org.jboss.errai>/<version.org.jboss.errai>$erraiVersion<\/version.org.jboss.errai>/;P;D " pom.xml
# git add and commit the version update changes

# git add and commit the version update changes 
git add .
commitMsg="update to next development version $newVersion"
git commit -m "$commitMsg"

# do a build of appformer
mvn -B -e -U clean install -Dmaven.test.failure.ignore=true -Dgwt.memory.settings="-Xmx2g -Xms1g -XX:MaxPermSize=256m -XX:PermSize=128m -Xss1M"

# Raise a PR
source=appformer
git push $targetUser $prBranch
hub pull-request -m "$commitMsg" -b $source:$baseBranch -h $targetUser:$prBranch

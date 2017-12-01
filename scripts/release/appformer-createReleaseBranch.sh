#!/bin/bash -e

# removing Uberfire artifacts from local maven repo (basically all possible SNAPSHOTs)
if [ -d $MAVEN_REPO_LOCAL ]; then
    rm -rf $MAVEN_REPO_LOCAL/org/jboss/errai/
    rm -rf $MAVEN_REPO_LOCAL/org/uberfire/
fi

# clones appformer branch
git clone git@github.com:kiegroup/appformer.git -b $baseBranch

cd appformer

# checkout the release branch 
git checkout -b $releaseBranch $baseBranch

# upgrades the version to the release/tag version
sh scripts/release/update-version.sh $newVersion

# update files that are not automatically changed with the update-versions-all.sh script
sed -i "$!N;s/<version.org.kie>.*.<\/version.org.kie>/<version.org.kie>$kieVersion<\/version.org.kie>/;P;D" pom.xml
sed -i "$!N;s/<version.org.jboss.errai>.*.<\/version.org.jboss.errai>/<version.org.jboss.errai>$erraiVersion<\/version.org.jboss.errai>/;P;D" pom.xml

# git add and commit the version update changes 
git add .
commitMsg="update to version $newVersion"
git commit -m "$commitMsg"

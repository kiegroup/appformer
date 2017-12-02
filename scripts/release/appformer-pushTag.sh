#!/bin/bash -e

# clone the repository and the release-branch

if [ "$target" == "productized" ]; then
   git clone git@github.com:jboss-integration/appformer.git --branch $releaseBranch
else
   git clone git@github.com:kiegroup/appformer.git --branch $releaseBranch
fi

commitMsg="Tagging $tag"
cd $WORKSPACE/appformer

#create the tag
git tag -a $tag -m "$commitMsg"

# pushes the TAG to ssh://jb-ip-tooling-jenkins@code.engineering.redhat.com/kiegroup/kie-soup [IMPORTANT: "push -n" (--dryrun) should be replaced by "push" when script is ready]
if [ "$target" == "productized" ]; then
   git remote add gerrit ssh://jb-ip-tooling-jenkins@code.engineering.redhat.com/kiegroup/appformer
   git push gerrit $tag
else
   git push origin $tag
fi

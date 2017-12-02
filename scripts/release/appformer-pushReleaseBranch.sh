#!/bin/bash -e

# pushes the release-branche to github.com:jboss-integration or github.com:kiegroup/appformer [IMPORTANT: "push -n" (--dryrun) should be replaced by "push" when script is finished and will be applied]
if [ "$target" == "community" ]; then
   git push origin $releaseBranch
else
   git remote add upstream git@github.com:jboss-integration/appformer.git
   git push upstream $releaseBranch
fi
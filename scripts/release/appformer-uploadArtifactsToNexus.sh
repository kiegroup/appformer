#!/bin/bash -e

if [ "$target" == "community" ]; then
   stagingProfile=15c58a1abc895b
else
   stagingProfile=15c3321d12936e
fi

deployDir=$WORKSPACE/deployDir

# upload the content to remote staging repo
mvn -B -e org.sonatype.plugins:nexus-staging-maven-plugin:1.6.5:deploy-staged-repository -DnexusUrl=https://repository.jboss.org/nexus -DserverId=jboss-releases-repository\
 -DrepositoryDirectory=$deployDir -DstagingProfileId=$stagingProfile -DstagingDescription="appformer $newVersion" -DstagingProgressTimeoutMinutes=30
#!/bin/bash -e

# clone the appformer
if [ "$target" == "community" ]; then
   source=kiegroup
else
   source=jboss-integration
fi

git clone git@github.com:"$source"/appformer.git --branch $releaseBranch

cd appformer

# build the repo & deploy into local dir (will be later copied into staging repo)
deployDir=$WORKSPACE/deployDir
# (1) do a full build, but deploy only into local dir
# we will deploy into remote staging repo only once the whole build passed (to save time and bandwith)
mvn -B -e -U clean deploy -Dfull -Drelease -T1C -DaltDeploymentRepository=local::default::file://$deployDir -Dmaven.test.failure.ignore=true \
 -Dgwt.memory.settings="-Xmx2g -Xms1g -Xss1M" -Dgwt.compiler.localWorkers=2
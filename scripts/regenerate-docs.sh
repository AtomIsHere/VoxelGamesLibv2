#!/usr/bin/env bash
echo 'wget cli.jar'
wget https://github.com/VoxelGamesLib/VoxelGamesLibv2/blob/gh-pages/commandline-tools/tools/commandline-tools-1.0-SNAPSHOT-all.jar?raw=true -O cli.jar
echo 'run cli.jar'
java -jar cli.jar -generateDocs -docsFolder docs
echo 'push'
cd docs
git push
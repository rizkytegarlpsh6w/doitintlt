pushd %~dp0
java -jar ../dtalk-demo-${project.version}-standalone.jar %*
popd
pushd %~dp0
java -jar dtalk-client-${project.version}-standalone.jar %*
popd
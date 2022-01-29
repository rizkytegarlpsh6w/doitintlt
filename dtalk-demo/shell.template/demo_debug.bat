pushd %~dp0
java -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n -jar ../dtalk-demo-${project.version}-standalone.jar %*
popd
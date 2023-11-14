pushd %~dp0
java %DTALK_OPTS% %DTALK_DEBUG_OPTS% -jar ../${project.build.finalName}-standalone.jar %*
popd
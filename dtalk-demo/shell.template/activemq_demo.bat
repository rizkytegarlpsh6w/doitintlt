pushd %~dp0
java %DTALK_OPTS% %DTALK_DEBUG_OPTS% -cp ../${project.build.finalName}-standalone.jar gu.dtalk.engine.demo.DemoActivemqConfig %*
popd

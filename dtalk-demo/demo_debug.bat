pushd %~dp0
java -Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n -jar target\dtalk-demo-0.0.1-SNAPSHOT-standalone.jar
popd
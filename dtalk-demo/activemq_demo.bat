@echo off
pushd %~dp0
call target\shell\%~nx0 %*
popd
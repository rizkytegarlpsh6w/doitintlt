#!/bin/bash
sh_folder=$(dirname $(readlink -f $0))
export DTALK_DEBUG_OPTS=-Xrunjdwp:transport=dt_socket,server=y,address=8000,suspend=n
$sh_folder/activemq_demo.sh $*
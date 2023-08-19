#!/bin/bash
sh_folder=$(dirname $(readlink -f $0))
$sh_folder/target/shell/$(basename $0) $*

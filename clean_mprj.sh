#!/bin/bash
# clean maven project

# 如果$1指定的文件夹为maven项目(含有pom.xml),则尝试清除(递归)所有中间生成文件及eclipse工程文件
# $1 项目文件夹
function clean_eclipse_project_folder(){
    [ ! -d "$1" ] && return 
    [ ! -f "$1/pom.xml" ] && return 
    echo clean $1
    rm -fr "$1/.settings" "$1/.classpath" "$1/.project" "$1/*.log" "$1/target" "$1/log"
}

folder=$1
[ -z "$folder" ] && folder=.
[ ! -d "$folder" ] && echo "$folder not a folder" && exit 0 
[ ! -f "$folder/pom.xml" ] && echo "$folder not a maven project folder" && exit 0

clean_eclipse_project_folder $folder 

for f in $(ls $folder)
do
  clean_eclipse_project_folder $folder/$f
done
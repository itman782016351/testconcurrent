#! /bin/bash
nohup java -jar ../lib/testconcurrent-1.0-SNAPSHOT.jar  > ../log.txt 2>&1 &
echo '启动成功'
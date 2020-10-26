#!/usr/bin/env bash

script_dir=$(dirname $0)

FILE=${script_dir}/../executor.port
EXEC_PORT=`cat $FILE`
echo "exec port: $EXEC_PORT"

curl -G localhost:$EXEC_PORT/executor?action=activate && echo
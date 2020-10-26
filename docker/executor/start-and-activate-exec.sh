#!/bin/bash

script_dir=$(dirname $0)

echo "starting executor..."
${script_dir}/start-exec.sh

sleep 5

echo "activating executor..."
${script_dir}/activate-executor.sh &

tail -f ${script_dir}/../*.out


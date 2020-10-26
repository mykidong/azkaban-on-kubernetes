#!/bin/sh

# create mysql server.
kubectl apply -f mysql.yaml;

# wait for mysql pod being ready.
while [[ $(kubectl get pods -n azkaban -l app=mysql -o 'jsonpath={..status.conditions[?(@.type=="Ready")].status}') != "True" ]]; do echo "waiting for mysql pod being ready" && sleep 1; done

# configmaps
kubectl create configmap azkaban-cfg --dry-run --from-file=azkaban-executor.properties --from-file=azkaban-web.properties -o yaml -n azkaban | kubectl apply -f -

# create db and tables.
kubectl apply -f init-schema.yaml;

# wait for job being completed.
while [[ $(kubectl get pods -n azkaban -l job-name=azakban-initschema -o jsonpath={..status.phase}) != *"Succeeded"* ]]; do echo "waiting for finishing init schema job" && sleep 2; done

# create azkaban.
kubectl apply -f azkaban.yaml;
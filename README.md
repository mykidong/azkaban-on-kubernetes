# Azkaban on Kubernetes
Azkaban is a popular workflow engine which is used to run jobs especially in data lake(from my experience).
This will show you how to run Azkaban(https://azkaban.readthedocs.io/en/latest/) on kubernetes.

## Build Azkaban with source(Optional)
You can build azkaban with source codes and package it as tar files.
This step is optional, you can skip this section and move to the next section.
```
cd ~;

git clone https://github.com/azkaban/azkaban.git
cd azkaban;

git checkout tags/3.90.0;

# Build and install distributions
./gradlew installDist

# package azkaban as tar files.
## db.
cd ~/azkaban/azkaban-db/build/install;
tar -zcf azkaban-db-3.90.0.tar.gz azkaban-db;

## executor.
cd ~/azkaban/azkaban-exec-server/build/install;
tar -zcf azkaban-exec-server-3.90.0.tar.gz azkaban-exec-server;

## web.
d ~/azkaban/azkaban-web-server/build/install;
tar -zcf azkaban-web-server-3.90.0.tar.gz azkaban-web-server;
```

You can upload these packages for instance, to google drive.
The azkaban packages used in the section are already uploaded onto the google drive, but you can change it to suit your needs.


## Create Azkaban Images
Azkaban packages are downloaded from google drive, and azkaban docker images will be built based on these azkaban packages.
There are three docker images built by the following steps, namely, azkaban db, azkaban executor server and azkaban web server.
```
# remove azkaban docker images.
docker rmi -f $(docker images -a | grep azkaban | awk '{print $3}')

# azkaban db docker image.
cd <src>/docker/db;
docker build . -t yourrepo/azkaban-db:3.90.0;

## push.
docker push yourrepo/azkaban-db:3.90.0;


# azkaban executor image.
cd <src>/docker/executor;
docker build . -t yourrepo/azkaban-exec-server:3.90.0;

## push.
docker push yourrepo/azkaban-exec-server:3.90.0;

# azkaban web image.
cd <src>/docker/web;
docker build . -t yourrepo/azkaban-web-server:3.90.0;

## push.
docker push yourrepo/azkaban-web-server:3.90.0;
```

Please, note that you should replace `yourrepo` with your docker repo name in the above.

## Run Azkaban on kubernetes
Storage class of PVC for mysql looks like this, let's see `mysql.yaml`:
```
storageClassName: direct.csi.min.io
```
You can change it to suit to your environment.

Note that you have to change the `yourrepo` in the docker image repo name in the manifest yaml files.


Now, run azkaban executors and web server on kubernetes.
```
## ---- init.
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


## ---- azkaban.
# create azkaban executor.
kubectl apply -f azkaban-executor.yaml;

# wait for azkaban executor being run
while [[ $(kubectl get pods -n azkaban -l app=azkaban-executor -o jsonpath={..status.phase}) != *"Running"* ]]; do echo "waiting for executor being run" && sleep 2; done


# create azkaban web.
kubectl apply -f azkaban-web.yaml;
```

Let's see the pods in azkaban namespace. It looks like this:
```
 kubectl get po -n azkaban
NAME                           READY   STATUS       RESTARTS   AGE
azakban-initschema-hr4bn       0/1     Init:Error   0          4h3m
azakban-initschema-kg75t       0/1     Completed    0          4h3m
azakban-initschema-ppngd       0/1     Init:Error   0          4h3m
azkaban-executor-0             1/1     Running      0          3h19m
azkaban-executor-1             1/1     Running      0          3h18m
azkaban-executor-2             1/1     Running      0          3h18m
azkaban-web-664967cb99-xhmrf   1/1     Running      0          3h9m
mysql-statefulset-0            1/1     Running      0          4h3m
```
As seen here, a mysql server, three executor servers, one web server are running on kubernetes.

## Access UI
To access UI, let's see the services in azkaban namespaces.
```
kubectl get svc -n azkaban
NAME               TYPE           CLUSTER-IP      EXTERNAL-IP     PORT(S)          AGE
azkaban-executor   ClusterIP      None            <none>          <none>           3h20m
azkaban-web        LoadBalancer   10.233.49.152   52.231.165.73   8081:31538/TCP   3h9m
mysql-service      ClusterIP      10.233.53.51    <none>          3306/TCP         4h4m
```

With the external ip of `azkaban-web` Service, you can access UI in browser:
```
http://52.231.165.73:8081/
```

## Azkaban Smoke Test
You can test azkaban with running example projects.
```
# install azkaban cli.
sudo pip install --upgrade "urllib3==1.22" azkaban;

# download sample projects and create project with azkaban cli.
wget https://github.com/azkaban/azkaban/raw/master/az-examples/flow20-projects/basicFlow20Project.zip;
wget https://github.com/azkaban/azkaban/raw/master/az-examples/flow20-projects/embeddedFlow20Project.zip;

azkaban upload -c -p basicFlow20Project -u azkaban@http://52.231.165.73:8081 ./basicFlow20Project.zip;
azkaban upload -c -p embeddedFlow20Project -u azkaban@http://52.231.165.73:8081 ./embeddedFlow20Project.zip;
```

## A little bit real example: Run shell in remote machine from azkaban executor
It is another example in which azkaban executor will call the remote shell to run spark job. Let's say, because spark and kubectl are installed on the remote machine, it is ready to submit spark job to kubernetes there.
To do so, ssh access to the remote machine from azkaban executor must be enabled. 
```
# list pods.
kubectl get po -n azkaban
NAME                           READY   STATUS       RESTARTS   AGE
azakban-initschema-9bgbh       0/1     Completed    0          16h
azakban-initschema-dtgg7       0/1     Init:Error   0          16h
azakban-initschema-fw7gt       0/1     Init:Error   0          16h
azkaban-executor-0             1/1     Running      0          16h
azkaban-executor-1             1/1     Running      0          16h
azkaban-executor-2             1/1     Running      0          16h
azkaban-web-664967cb99-z8dzn   1/1     Running      0          16h
mysql-statefulset-0            1/1     Running      0          16h

# access executor pod to get public key.
kubectl exec -it azkaban-executor-0 -n azkaban -- cat .ssh/id_rsa.pub;
ssh-rsa AAAAB3NzaC1yc2EAAAADAQABAAABAQC0vuKKMz4dD0aBrJKtlVU8fDmYgqkwpkDXTzoUTqm57CqEmzHa5EDS90xGch1rAN4HucOR6dzUGvb2VlATBGIi5VZ6w0OuRR+r50KHqiC0TLdEXzX1/TRO/uHftI/xdUMFDHOWTuZnsYS5V7DCrw1yJnPzHTHktgXDyycM/iEspdfslzgZuIV4zT3HNVAYIplQPyy8TKRy7gojm7OYw5W2S14hqiY5/HL/CZ9CQpKV37qJvd3E4u/pOZCHH7r1Tm5E3bnUX9U8z7Nj0Fb+TZSkxiEbwoKB/Ib07Urc0il2f4mug2bKazZRsU+/bb1+VjoMW0ek+9Rvk1JTkaXIu8k/ executor@33842653d6db

# copy this executor public key and paste it to authorized_keys file in remote machine.
## in remote machine.
vi ~/.ssh/authorized_keys;
... paste public key.

# chmod 600.
chmod 600 ~/.ssh/authorized_keys;

# copy the public keys of the other executors and paste them to the remote machine.
...
```
and then, login to remote machine via ssh in the individual azkaban executor:
```
kubectl exec -it azkaban-executor-0 -n azkaban -- sh;
ssh pcp@x.x.x.x;
...
exit;
```

Let's create shell to run spark job in the remote machine:
```
# spark job run shell.
vi run-spark-example.sh;
...

############## spark job: create delta table

# submit spark job onto kubernetes.
export MASTER=k8s://https://xxxx:6443;
export NAMESPACE=ai-developer;
export ENDPOINT=http://$(kubectl get svc s3g-service -n ai-developer -o jsonpath={.status.loadBalancer.ingress[0].ip}):9898;
export HIVE_METASTORE=metastore.ai-developer:9083;

spark-submit \
--master ${MASTER} \
--deploy-mode cluster \
--name spark-delta-example \
--class io.spongebob.spark.examples.DeltaLakeExample \
--packages com.amazonaws:aws-java-sdk-s3:1.11.375,org.apache.hadoop:hadoop-aws:3.2.0 \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.checkpointpvc.mount.path=/checkpoint \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.checkpointpvc.mount.subPath=checkpoint \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.checkpointpvc.mount.readOnly=false \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.checkpointpvc.options.claimName=spark-driver-pvc \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.checkpointpvc.mount.path=/checkpoint \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.checkpointpvc.mount.subPath=checkpoint \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.checkpointpvc.mount.readOnly=false \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.checkpointpvc.options.claimName=spark-exec-pvc \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.mount.path=/localdir \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.mount.readOnly=false \
--conf spark.kubernetes.driver.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.options.claimName=spark-driver-localdir-pvc \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.mount.path=/localdir \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.mount.readOnly=false \
--conf spark.kubernetes.executor.volumes.persistentVolumeClaim.spark-local-dir-localdirpvc.options.claimName=spark-exec-localdir-pvc \
--conf spark.kubernetes.file.upload.path=s3a://mykidong/spark-examples \
--conf spark.kubernetes.container.image.pullPolicy=Always \
--conf spark.kubernetes.namespace=$NAMESPACE \
--conf spark.kubernetes.container.image=xxx/spark:v3.0.0 \
--conf spark.kubernetes.authenticate.driver.serviceAccountName=spark \
--conf spark.hadoop.hive.metastore.client.connect.retry.delay=5 \
--conf spark.hadoop.hive.metastore.client.socket.timeout=1800 \
--conf spark.hadoop.hive.metastore.uris=thrift://$HIVE_METASTORE \
--conf spark.hadoop.hive.server2.enable.doAs=false \
--conf spark.hadoop.hive.server2.thrift.http.port=10002 \
--conf spark.hadoop.hive.server2.thrift.port=10016 \
--conf spark.hadoop.hive.server2.transport.mode=binary \
--conf spark.hadoop.metastore.catalog.default=spark \
--conf spark.hadoop.hive.execution.engine=spark \
--conf spark.hadoop.hive.input.format=io.delta.hive.HiveInputFormat \
--conf spark.hadoop.hive.tez.input.format=io.delta.hive.HiveInputFormat \
--conf spark.sql.warehouse.dir=s3a:/mykidong/apps/spark/warehouse \
--conf spark.hadoop.fs.defaultFS=s3a://mykidong \
--conf spark.hadoop.fs.s3a.access.key=any-access-key \
--conf spark.hadoop.fs.s3a.secret.key=any-secret-key \
--conf spark.hadoop.fs.s3a.connection.ssl.enabled=true \
--conf spark.hadoop.fs.s3a.endpoint=$ENDPOINT \
--conf spark.hadoop.fs.s3a.impl=org.apache.hadoop.fs.s3a.S3AFileSystem \
--conf spark.hadoop.fs.s3a.fast.upload=true \
--conf spark.hadoop.fs.s3a.path.style.access=true \
--conf spark.driver.extraJavaOptions="-Divy.cache.dir=/tmp -Divy.home=/tmp" \
--conf spark.executor.instances=3 \
--conf spark.executor.memory=2G \
--conf spark.executor.cores=1 \
--conf spark.driver.memory=1G \
file:///home/pcp/xxx/examples/spark/target/spark-example-1.0.0-SNAPSHOT-spark-job.jar \
--master ${MASTER};

...



# chmod set to be executable.
chmod a+x run-spark-example.sh;
```


Now, create an azkaban flow like this:
```
---
config:
  failure.emails: mykidong@gmail.com

nodes:
- name: Start
  type: noop


- name: RunSparkJob
  type: command
  config:
    command: ssh pcp@x.x.x.x "/home/pcp/run-spark-example.sh"
  dependsOn:
  - Start

- name: End
  type: noop
  dependsOn:
  - RunSparkJob
  ```
  
  
  After zipping and deploying your new project with this flow to azkaban web, you can execute the job in the azkaban ui.

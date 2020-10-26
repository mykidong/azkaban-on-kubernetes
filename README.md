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



#!/bin/bash

for i in "$@"
do
case $i in
    --encryption.key=*)
    ENCRYPTION_KEY="${i#*=}"
    shift # past argument=value
    ;;
    --s3.access.key=*)
    S3_ACCESS_KEY="${i#*=}"
    shift # past argument=value
    ;;
    --s3.secret.key=*)
    S3_SECRET_KEY="${i#*=}"
    shift # past argument=value
    ;;
    --s3.endpoint=*)
    S3_ENDPOINT="${i#*=}"
    shift # past argument=value
    ;;
    --run.job.url=*)
    RUN_JOB_URL="${i#*=}"
    shift # past argument=value
    ;;
    --encryptor.path=*)
    ENCRYPTOR_PATH="${i#*=}"
    shift # past argument=value
    ;;
    --admin.kubeconfig=*)
    ADMIN_KUBECONFIG="${i#*=}"
    shift # past argument=value
    ;;
    --job.type=*)
    JOB_TYPE="${i#*=}"
    shift # past argument=value
    ;;
    *)
          # unknown option
    ;;
esac
done

echo "ENCRYPTION_KEY  = ${ENCRYPTION_KEY}"
echo "S3_ACCESS_KEY   = ${S3_ACCESS_KEY}"
echo "S3_SECRET_KEY   = ${S3_SECRET_KEY}"
echo "S3_ENDPOINT     = ${S3_ENDPOINT}"
echo "RUN_JOB_URL     = ${RUN_JOB_URL}"
echo "ENCRYPTOR_PATH  = ${ENCRYPTOR_PATH}"
echo "ADMIN_KUBECONFIG = ${ADMIN_KUBECONFIG}"
echo "JOB_TYPE  = ${JOB_TYPE}"


# decrypt properties and do something.
java -cp ./pod-runner-1.0.0-SNAPSHOT-fat.jar \
io.mykidong.kubernetes.JobRunner \
--encryption.key ${ENCRYPTION_KEY} \
--s3.access.key ${S3_ACCESS_KEY} \
--s3.secret.key ${S3_SECRET_KEY} \
--s3.endpoint ${S3_ENDPOINT} \
--run.job.url ${RUN_JOB_URL} \
--encryptor.path ${ENCRYPTOR_PATH} \
--admin.kubeconfig ${ADMIN_KUBECONFIG};


# set s3 endpoint.
export S3_ENDPOINT=$(cat s3-endpoint.txt);

# run job.
chmod a+x ./run-job.sh;
echo "ready to run main job..."
./run-job.sh;



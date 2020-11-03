package io.mykidong.kubernetes;

import io.mykidong.encrypt.KeyEncryptorProcessExecutor;
import io.mykidong.util.*;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class JobRunner {
    private static Logger LOG = LoggerFactory.getLogger(JobRunner.class);

    public static void main(String[] args) {
        // load log4j.
        Log4jConfigurer.loadLog4j(null);

        // parse arguments.
        OptionParser parser = new OptionParser();
        parser.accepts("encryption.key").withRequiredArg().ofType(String.class);
        parser.accepts("s3.access.key").withRequiredArg().ofType(String.class);
        parser.accepts("s3.secret.key").withRequiredArg().ofType(String.class);
        parser.accepts("s3.endpoint").withRequiredArg().ofType(String.class);
        parser.accepts("run.job.url").withRequiredArg().ofType(String.class);
        parser.accepts("encryptor.path").withRequiredArg().ofType(String.class);
        parser.accepts("admin.kubeconfig").withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse(args);

        String encryptedKey = (String) options.valueOf("encryption.key");
        String encryptedS3AccessKey = (String) options.valueOf("s3.access.key");
        String encryptedS33SecretKey = (String) options.valueOf("s3.secret.key");
        String encryptedS3Endpoint = (String) options.valueOf("s3.endpoint");
        String runJobUrl = (String) options.valueOf("run.job.url");
        String encryptorPath = (String) options.valueOf("encryptor.path");
        String encryptedAdminKubeconfig = (String) options.valueOf("admin.kubeconfig");

        // decrypt encrypted encryption key.
        String encryptionKey = KeyEncryptorProcessExecutor.doExec(encryptorPath, encryptedKey, "false");

        // decrypt properties.
        String s3AccessKey = EncryptionUtils.decodeBase64AndDecrypt(encryptionKey, encryptedS3AccessKey);
        String s3SecretKey = EncryptionUtils.decodeBase64AndDecrypt(encryptionKey, encryptedS33SecretKey);
        String s3Endpoint = EncryptionUtils.decodeBase64AndDecrypt(encryptionKey, encryptedS3Endpoint);
        String adminKubeconfig = EncryptionUtils.decodeBase64AndDecrypt(encryptionKey, encryptedAdminKubeconfig);

        // save kubeconfig file
        FileUtils.stringToFile(adminKubeconfig, "kubeconfig", false);
        LOG.info("kubeconfig file created...");

        // download run job shell from s3.
        int index = runJobUrl.indexOf("/");
        String bucket = runJobUrl.substring(0, index);
        LOG.info("bucket: [{}]", bucket);

        String keyPath = runJobUrl.substring(index + 1, runJobUrl.length());
        LOG.info("keyPath: [{}]", keyPath);

        String outputPath = "run-job.sh";
        AwsS3Utils.downloadObject(s3Endpoint, s3AccessKey, s3SecretKey, bucket, keyPath, outputPath);
        LOG.info("object [{}] downloaded", keyPath);

        // save aws config to run aws cli.
        ProcessExecutor.doExec("aws", "configure",  "--profile=minio", "set", "default.s3.signature_version", "s3v4");
        ProcessExecutor.doExec("aws", "configure",  "--profile=minio", "set", "aws_access_key_id", s3AccessKey);
        ProcessExecutor.doExec("aws", "configure",  "--profile=minio", "set", "aws_secret_access_key", s3SecretKey);
        ProcessExecutor.doExec("aws", "configure",  "--profile=minio", "set", "region", "us-west-1");

        // save s3 endpoint.
        FileUtils.stringToFile(s3Endpoint, "s3-endpoint.txt", false);
    }
}

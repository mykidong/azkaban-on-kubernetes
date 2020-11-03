package io.mykidong.util;

import com.amazonaws.services.s3.AmazonS3;
import io.mykidong.test.TestBase;
import org.junit.Test;

public class AwsS3UtilsTestRunner extends TestBase {

    @Test
    public void listObject() throws Exception {
        String endpoint = "https://mykidong-tenant.minio.cloudchef-labs.com";
        String accessKey = "bWluaW8=";
        String secretKey = "bWluaW8xMjM=";

        AmazonS3 s3client = AwsS3Utils.createS3Client(endpoint, accessKey, secretKey);

        String bucketName = "mykidong";

        if(s3client.doesBucketExistV2(bucketName)) {
            LOG.info("bucket exists.");

            return;
        }

        s3client.createBucket(bucketName);
    }

    @Test
    public void downloadObject() throws Exception {
        String endpoint = "https://mykidong-tenant.minio.cloudchef-labs.com";
        String accessKey = "bWluaW8=";
        String secretKey = "bWluaW8xMjM=";
        String bucketName = "mykidong";
        String keyPath = "hello/mykidong";
        String outputPath = "/home/pcp/ret-obj.sh";
        AwsS3Utils.downloadObject(endpoint, accessKey, secretKey, bucketName, keyPath, outputPath);
    }
}

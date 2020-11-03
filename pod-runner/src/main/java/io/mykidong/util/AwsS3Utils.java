package io.mykidong.util;

import com.amazonaws.auth.AWSCredentials;
import com.amazonaws.auth.AWSStaticCredentialsProvider;
import com.amazonaws.auth.BasicAWSCredentials;
import com.amazonaws.client.builder.AwsClientBuilder;
import com.amazonaws.regions.Regions;
import com.amazonaws.services.s3.AmazonS3;
import com.amazonaws.services.s3.AmazonS3ClientBuilder;
import com.amazonaws.services.s3.model.S3Object;
import com.amazonaws.services.s3.model.S3ObjectInputStream;

import java.io.File;

public class AwsS3Utils {

    private static AWSCredentials createCredentials(String accessKey, String secretKey) {
        return new BasicAWSCredentials(accessKey, secretKey);
    }


    public static AmazonS3 createS3Client(String endpoint, String accessKey, String secretKey) {
        return AmazonS3ClientBuilder.standard()
                .withCredentials(new AWSStaticCredentialsProvider(createCredentials(accessKey, secretKey)))
                .withEndpointConfiguration(new AwsClientBuilder.EndpointConfiguration(endpoint, Regions.US_EAST_2.getName()))
                .withPathStyleAccessEnabled(true)
                .build();
    }

    public static void downloadObject(AmazonS3 s3client, String bucketName, String keyPath, String outputPath) {
        S3Object s3object = s3client.getObject(bucketName, keyPath);
        S3ObjectInputStream inputStream = s3object.getObjectContent();
        try {
            org.apache.commons.io.FileUtils.copyInputStreamToFile(inputStream, new File(outputPath));
        } catch (Exception e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }

    public static void downloadObject(String endpoint,
                                      String accessKey,
                                      String secretKey,
                                      String bucketName,
                                      String keyPath,
                                      String outputPath) {
        AmazonS3 s3client = createS3Client(endpoint, accessKey, secretKey);
        downloadObject(s3client, bucketName, keyPath, outputPath);
    }
}

package io.mykidong.encrypt;

import io.mykidong.test.TestBase;
import io.mykidong.util.EncryptionUtils;
import io.mykidong.util.FileUtils;
import io.mykidong.util.PropertiesUtils;
import org.junit.Assert;
import org.junit.Test;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class EncryptionDecryptionTestSkip extends TestBase {

    private static Logger LOG = LoggerFactory.getLogger(EncryptionDecryptionTestSkip.class);

    @Test
    public void printSecrets() throws Exception {
        Properties prop = PropertiesUtils.readPropertiesFromClasspath("pod-runner.properties");
        String encryptedKey = prop.getProperty("encryption.key");
        String encryptionKey = KeyEncryptorProcessExecutor.doExec("c:/project/mykidong/encyptor/enc.exe", encryptedKey, "false");

        String S3_ACCESS_KEY = EncryptionUtils.encyptAndEncodeBase64(encryptionKey,"bWluaW8=");
        LOG.info("S3_ACCESS_KEY: [{}]", S3_ACCESS_KEY);
        Assert.assertTrue("bWluaW8=".equals(EncryptionUtils.decodeBase64AndDecrypt(encryptionKey, S3_ACCESS_KEY)));

        String S3_SECRET_KEY = EncryptionUtils.encyptAndEncodeBase64(encryptionKey,"bWluaW8xMjM=");
        LOG.info("S3_SECRET_KEY: [{}]", S3_SECRET_KEY);
        Assert.assertTrue("bWluaW8xMjM=".equals(EncryptionUtils.decodeBase64AndDecrypt(encryptionKey, S3_SECRET_KEY)));

        String S3_ENDPOINT = EncryptionUtils.encyptAndEncodeBase64(encryptionKey,"https://mykidong-tenant.minio.cloudchef-labs.com");
        LOG.info("S3_ENDPOINT: [{}]", S3_SECRET_KEY);
        Assert.assertTrue("https://mykidong-tenant.minio.cloudchef-labs.com".equals(EncryptionUtils.decodeBase64AndDecrypt(encryptionKey, S3_ENDPOINT)));

        String adminKubeconfig = FileUtils.fileToString("kubeconfig/kubeconfig.yaml", true);
        String ADMIN_KUBECONFIG = EncryptionUtils.encyptAndEncodeBase64(encryptionKey,adminKubeconfig);
        LOG.info("ADMIN_KUBECONFIG: [{}]", ADMIN_KUBECONFIG);

        String decryptedAdminKubeconfig = EncryptionUtils.decodeBase64AndDecrypt(encryptionKey, ADMIN_KUBECONFIG);
        LOG.info("decryptedAdminKubeconfig: \n{}", decryptedAdminKubeconfig);
        Assert.assertTrue(adminKubeconfig.equals(decryptedAdminKubeconfig));
    }
}

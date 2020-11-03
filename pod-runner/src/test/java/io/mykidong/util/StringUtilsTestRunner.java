package io.mykidong.util;

import io.mykidong.test.TestBase;
import org.junit.Test;

public class StringUtilsTestRunner extends TestBase {

    @Test
    public void parseBucketAndKeyPath() throws Exception {
        String runJobUrl = "my-bucket/anykey/path/run-job.sh";
        int index = runJobUrl.indexOf("/");
        String bucket = runJobUrl.substring(0, index);
        LOG.info("bucket: [{}]", bucket);

        String keyPath = runJobUrl.substring(index + 1, runJobUrl.length());
        LOG.info("keyPath: [{}]", keyPath);
    }
}

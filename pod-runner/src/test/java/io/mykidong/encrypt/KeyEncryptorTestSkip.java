package io.mykidong.encrypt;

import io.mykidong.test.TestBase;
import org.junit.Test;

public class KeyEncryptorTestSkip extends TestBase {

    @Test
    public void readDecryptedKey() throws Exception {
        String encryptedKey = "981c5dc8554f2d6b4701c23ecb1efd13a14d650b89704207e5de5e77f974afe7e4447f2b17d7df8a";
        String encryptionKey = KeyEncryptorProcessExecutor.doExec("/home/pcp/enc", encryptedKey, "false");
        LOG.info("encryptionKey: {}", encryptionKey);
    }
}

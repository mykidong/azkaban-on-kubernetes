package io.mykidong.encrypt;

import io.mykidong.util.EncryptionUtils;
import joptsimple.OptionParser;
import joptsimple.OptionSet;

public class Encryptor {

    public static void main(String[] args) {

        // parse arguments.
        OptionParser parser = new OptionParser();
        parser.accepts("text").withRequiredArg().ofType(String.class);
        parser.accepts("encrypt").withRequiredArg().ofType(String.class);
        parser.accepts("encrypted.key").withRequiredArg().ofType(String.class);
        parser.accepts("encryptor.path").withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse(args);

        String text = (String) options.valueOf("text");
        String toEncrypt = (String) options.valueOf("encrypt");
        String encryptedKey = (String) options.valueOf("encrypted.key");
        String encLibPath = (String) options.valueOf("encryptor.path");


        String encryptionKey = KeyEncryptorProcessExecutor.doExec(encLibPath, encryptedKey, "false");

        if(Boolean.valueOf(toEncrypt)) {
            String encryptedText = EncryptionUtils.encyptAndEncodeBase64(encryptionKey,text);
            System.out.println(encryptedText);
        } else {
            String decryptedText = EncryptionUtils.decodeBase64AndDecrypt(encryptionKey, text);
            System.out.println(decryptedText);
        }
    }
}

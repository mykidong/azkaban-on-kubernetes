package io.mykidong.kubernetes;

import io.mykidong.domain.Kubeconfig;
import io.mykidong.encrypt.KeyEncryptorProcessExecutor;
import io.mykidong.util.*;
import joptsimple.OptionParser;
import joptsimple.OptionSet;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Properties;
import java.util.UUID;

public class PodRunner {

    private static Logger LOG = LoggerFactory.getLogger(PodRunner.class);

    public static void main(String[] args) {

        // load log4j.
        Log4jConfigurer.loadLog4j(null);

        // parse arguments.
        OptionParser parser = new OptionParser();
        parser.accepts("conf").withRequiredArg().ofType(String.class);
        parser.accepts("image").withRequiredArg().ofType(String.class);
        parser.accepts("cmd").withRequiredArg().ofType(String.class);
        parser.accepts("args").withRequiredArg().ofType(String.class);
        parser.accepts("namespace").withRequiredArg().ofType(String.class);
        parser.accepts("run.job.url").withRequiredArg().ofType(String.class);
        parser.accepts("encryptor.path").withRequiredArg().ofType(String.class);

        OptionSet options = parser.parse(args);

        String conf = (String) options.valueOf("conf");
        String image = (String) options.valueOf("image");
        String cmd = (String) options.valueOf("cmd");
        String argsList = (String) options.valueOf("args");
        String namespace = (String) options.valueOf("namespace");
        String runJobUrl = (String) options.valueOf("run.job.url");
        String encryptorPath = (String) options.valueOf("encryptor.path");


        // read properties.
        Properties prop = PropertiesUtils.readPropertiesFromFileSystem(conf);
        String encryptedKey = prop.getProperty("encryption.key");
        String encryptedKubeconfig = prop.getProperty("admin.kubeconfig");
        String encryptedS3AccessKey = prop.getProperty("s3.access.key");
        String encryptedS3SecretKey = prop.getProperty("s3.secret.key");
        String encryptedS3Endpoint = prop.getProperty("s3.endpoint");


        // decrypt encrypted encryption key.
        String encryptionKey = KeyEncryptorProcessExecutor.doExec(encryptorPath, encryptedKey, "false");

        // read kubeconfig yaml from encrypted property.
        String adminKubeconfig = EncryptionUtils.decodeBase64AndDecrypt(encryptionKey, encryptedKubeconfig);

        // convert kubeconfig yaml to object.
        Kubeconfig kubeconfig = YamlUtils.readKubeconfigYaml(new ByteArrayInputStream(adminKubeconfig.getBytes()));

        // replace job with params.
        Map<String, String> kv = new HashMap<>();
        String suffix = RandomUtils.getRandomNumber(10000, 4);
        kv.put("suffix", suffix);
        kv.put("namespace", namespace);
        kv.put("image", image);

        cmd = "\"" + cmd + "\"";
        kv.put("cmd", cmd);

        String argsString = "";
        for(String arg : argsList.split("\\s+")) {
            argsString += ((argsString.equals("")) ? "" : ",") + "\"" + arg + "\"";
        }

        // encryption key param.
        argsString += "," + "\"--encryption.key=" + encryptedKey + "\"";

        // s3 access key param.
        argsString += "," + "\"--s3.access.key=" + encryptedS3AccessKey + "\"";

        // s3 secret key param.
        argsString += "," + "\"--s3.secret.key=" + encryptedS3SecretKey + "\"";

        // s3 endpoint param.
        argsString += "," + "\"--s3.endpoint=" + encryptedS3Endpoint + "\"";

        // run job url param.
        argsString += "," + "\"--run.job.url=" + runJobUrl + "\"";

        // encryptor path param.
        argsString += "," + "\"--encryptor.path=" + encryptorPath + "\"";

        // kubeconfig.
        argsString += "," + "\"--admin.kubeconfig=" + encryptedKubeconfig + "\"";


        kv.put("args", argsString);

        String resourceTemplate = FileUtils.fileToString("kubernetes/template/job.yaml", true);
        String jobResource = TemplateUtils.replace(resourceTemplate, kv);
        InputStream jobResourceInputStream = new ByteArrayInputStream(jobResource.getBytes());

        // create pod and watch.
        ResourceController.runPod(kubeconfig, namespace, jobResourceInputStream);
    }
}

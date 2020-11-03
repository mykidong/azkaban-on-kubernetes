package io.mykidong.util;

import io.mykidong.test.TestBase;
import org.junit.Test;

import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class TemplateUtilsTestRunner extends TestBase {

    @Test
    public void replace() throws Exception {
        String image = "mykidong/spark-job-runner:3.0.0";
        String namespace = "azkaban";
        String cmd = "/home/pcp/enc";
        String argsList = "1th 2th 3th 4th";

        // replace job with params.
        Map<String, String> kv = new HashMap<>();
        kv.put("name", image + "-" + UUID.randomUUID().toString());
        kv.put("namespace", namespace);
        kv.put("image", image);

        cmd = "\"" + cmd + "\"";
        kv.put("cmd", cmd);

        String argsString = "";
        for(String arg : argsList.split("\\s+")) {
            argsString += ((argsString.equals("")) ? "" : ",") + "\"" + arg + "\"";
        }
        kv.put("args", argsString);

        String resourceTemplate = FileUtils.fileToString("kubernetes/template/job.yaml", true);
        String jobResource = TemplateUtils.replace(resourceTemplate, kv);
        LOG.info(jobResource);
    }

}

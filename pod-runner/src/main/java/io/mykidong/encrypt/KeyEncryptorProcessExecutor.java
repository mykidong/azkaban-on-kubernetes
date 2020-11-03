package io.mykidong.encrypt;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

public class KeyEncryptorProcessExecutor {

    private static Logger LOG = LoggerFactory.getLogger(KeyEncryptorProcessExecutor.class);

    public static String doExec(String... cmd) {
        ProcessBuilder processBuilder = new ProcessBuilder();
        processBuilder.command(cmd);

        boolean exceptionThrown = false;

        String encyptionKey = null;
        try {
            LOG.info("cmd: [{}]", cmd);

            Process process = processBuilder.start();

            ExecutorService executor = Executors.newFixedThreadPool(1);
            Future<String> future = executor.submit(() -> {
                String key = null;
                try
                {
                    InputStreamReader isr = new InputStreamReader(process.getInputStream());
                    BufferedReader br = new BufferedReader(isr);
                    String line = null;
                    while((line = br.readLine()) != null) {
                        key = line;
                        break;
                    }
                } catch (IOException ioe)
                {
                    ioe.printStackTrace();
                }

                return key;
            });

            try {
                // set the value of encryption key.
                encyptionKey = future.get();
            } catch (InterruptedException | ExecutionException e) {
                LOG.error(e.getMessage());
            }
            executor.shutdown();

            int exitVal = process.waitFor();
            if (exitVal == 0) {
                LOG.info("cmd [{}], Success!", cmd);
            } else {
                LOG.info("abnormal exit, cmd [{}]", cmd);
            }
        } catch (Exception e) {
            e.printStackTrace();
            LOG.error("cmd: [{}], error: [{}]", cmd, e.getMessage());
            exceptionThrown = true;
        }

        if(!exceptionThrown) {
            LOG.info("cmd [{}] done...", cmd);
        }

        return encyptionKey;
    }
}

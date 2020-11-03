package io.mykidong.kubernetes;

import io.fabric8.kubernetes.api.model.HasMetadata;
import io.fabric8.kubernetes.api.model.Pod;
import io.fabric8.kubernetes.client.KubernetesClient;
import io.fabric8.kubernetes.client.KubernetesClientException;
import io.fabric8.kubernetes.client.dsl.LogWatch;
import io.mykidong.domain.Kubeconfig;
import io.mykidong.kubernetes.client.KubernetesClientUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.InputStream;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class ResourceController {

    private static Logger LOG = LoggerFactory.getLogger(ResourceController.class);

    public static void runPod(Kubeconfig kubeconfig, String namespace, InputStream jobResourceInputStream) {
        try  {
            KubernetesClient adminClient = KubernetesClientUtils.newClient(kubeconfig);

            List<HasMetadata> resources = adminClient.load(jobResourceInputStream).get();
            HasMetadata resource = resources.get(0);
            if (resource instanceof Pod){
                Pod pod = (Pod) resource;
                Pod result = adminClient.pods().inNamespace(namespace).create(pod);
                String podName = result.getMetadata().getName();
                LOG.info("podName: {}", podName);

                LogWatch watch = adminClient.pods().inNamespace(namespace).withName(podName).tailingLines(10).watchLog(System.out);

                long start = System.currentTimeMillis();
                while(true) {
                    String phase = adminClient.pods().inNamespace(namespace).withName(podName).get().getStatus().getPhase();
                    LOG.info("phase: {}", phase);
                    if(phase.equals("Failed")) {
                        throw new RuntimeException(podName + " job failed!");
                    } else if(phase.equals("Succeeded")) {
                        break;
                    }

                    Thread.sleep(5 * 1000);
                    long elapsed = (System.currentTimeMillis() - start) / 1000;
                    // more than 3 hours.
                    if(elapsed > (3 * 60 * 60)) {
                        throw new RuntimeException(podName + " job takes too long time!");
                    }
                }

            } else {
                System.err.println("Loaded resource is not a Pod! " + resource);
            }
        } catch (KubernetesClientException e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        } catch (Exception e) {
            LOG.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
}

package com.devops.automation.confversionmanage.apollo;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import lombok.extern.slf4j.Slf4j;

import java.io.File;
import java.net.URL;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;

@Slf4j
public class ApolloConfigExecutor {
    private List<ApolloConfigVO> configs;

    public ApolloConfigExecutor(List<ApolloConfigVO> configs) {
        this.configs = configs;
    }

    public void updateRemote() throws Exception{
        if (this.configs.isEmpty()) {
            log.warn("No Apollo config to be updated");
        } else {
            ExecutorService executorService = Executors.newFixedThreadPool(this.configs.size());
            ExecutorService executorItemService = Executors.newFixedThreadPool(20);
            Iterator iterator = this.configs.iterator();
            while (iterator.hasNext()) {
                ApolloConfigVO vo = (ApolloConfigVO) iterator.next();
                ApolloOpenApiClient apolloOpenApiClient = ApolloOpenApiClient.newBuilder().withPortalUrl(vo.getPortalURL())
                        .withToken(vo.getToken())
                        .build();
//                URL url = AutomationBoot.getResource("/"+vo.getFilePath());
                URL url = null;
                if (url == null) {
                    throw new Exception("获取资源无效：" + vo.getFilePath());
                }
                File path = new File(url.getFile());
                if (!path.isDirectory()) {
                    throw new Exception(path + "不是有效目录");
                }
                File[] tempList = path.listFiles();
                if (tempList !=null) {
                    for (int i =0;i<tempList.length;i++) {
                        if (tempList[i].isFile()) {
                            String filepath = tempList[i].getPath();
                            String filename = tempList[i].getName();
                            UpdateRemoteApolloConfig task= new UpdateRemoteApolloConfig(vo.getAppID(), filepath, vo.getEnv(), filename, vo.getDusID()
                            ,vo.isPublic(), apolloOpenApiClient, executorItemService);
                            executorService.submit(task);
                        }
                    }
                }
            }
            executorService.shutdown();
            while (!executorService.isTerminated()) {
                try {
                    TimeUnit.SECONDS.sleep(1L);
                } catch (InterruptedException e) {
                    e.printStackTrace();
                }
            }
            log.info("所有文件都执行完成");
        }
    }
}

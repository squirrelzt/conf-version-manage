package com.devops.automation.confversionmanage.apollo;

import com.ctrip.framework.apollo.openapi.client.ApolloOpenApiClient;
import com.ctrip.framework.apollo.openapi.dto.NamespaceReleaseDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenAppNamespaceDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenItemDTO;
import com.ctrip.framework.apollo.openapi.dto.OpenNamespaceDTO;
import lombok.extern.slf4j.Slf4j;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Properties;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Future;
import java.util.stream.Collectors;

@Slf4j
public class UpdateRemoteApolloConfig implements Callable<Boolean> {
    private static final String USER_ID = "apollo";
    private ApolloOpenApiClient apolloOpenApiClient;
    private String appId;
    private String appFilePath;
    private String env;
    private String fileName;
    private String cluster;
    private boolean isPublic;
    private ExecutorService executorItemService;
//    private List<OpenItemDTO> (List)oldlist.stream().filter(item -> {});

    public UpdateRemoteApolloConfig(String appId,String appFilePath,String env,String fileName,String cluster,boolean isPublic,ApolloOpenApiClient apolloOpenApiClient,
                                    ExecutorService executorItemService) {
        this.appId = appId;
        this.appFilePath = appFilePath;
        this.env = env;
        this.fileName = fileName;
        this.cluster = cluster;
        this.apolloOpenApiClient = apolloOpenApiClient;
        this.executorItemService = executorItemService;
    }

    private boolean updateRemoteConfig() {
        log.info("开始处理【{}】配置文件", this.fileName);
        String[] fileinfo = this.fileName.split("\\.");
        String fileType = fileinfo[1];
        String nameSpace = fileinfo[0];

        try (InputStream inputStream = new FileInputStream(this.appFilePath)) {
            Properties properties = new Properties();
            if (!"properties".equals(fileType)) {
                log.info("配置文件【{}】请使用properties文件类型", this.fileName);
                return false;
            }
            properties.load(inputStream);
            List<OpenItemDTO> list = new ArrayList<OpenItemDTO>();
            Iterator iterator = properties.stringPropertyNames().iterator();
            while (iterator.hasNext()) {
                String key = (String) iterator.next();
                log.debug(key + "=" + properties.getProperty(key));
                OpenItemDTO openItemDTO = new OpenItemDTO();
                openItemDTO.setKey(key);
                openItemDTO.setValue(properties.getProperty(key));
                openItemDTO.setDataChangeLastModifiedBy("apollo");
                openItemDTO.setDataChangeCreatedBy("apollo");
                list.add(openItemDTO);

            }
            return this.pushToApollo(list, nameSpace);
        } catch (IOException e) {
            log.error("配置文件【{}】处理失败", this.fileName);
            return false;
        }
//        return true;
    }

    public boolean pushToApollo(List<OpenItemDTO> list, final String nameSpace) {
        Long startTime = System.currentTimeMillis();
        final String appId = this.appId;
        final String env = this.env;
        final String cluster = this.cluster;
        OpenNamespaceDTO openNamespaceDTO = null;
        List<Future> futureList = new ArrayList<>();
        List oldList = null;
        try {
            openNamespaceDTO = this.apolloOpenApiClient.getNamespace(appId,this.env,cluster,nameSpace);
            if (openNamespaceDTO!=null) {
                oldList = openNamespaceDTO.getItems();
                List<OpenItemDTO> delList = this.getDelList(list,openNamespaceDTO.getItems());
                Iterator iterator = delList.iterator();
                while (iterator.hasNext()) {
//                    final OpenItemDTO itemDTO = (OpenItemDTO)iterator.next();
//                    Future result = this.executorItemService.submit(run()->{
//                        UpdateRemoteApolloConfig.log.debug("删除远程配置文件【{}】中的{}",UpdateRemoteApolloConfig.this.fileName,itemDTO.toString());
//                        UpdateRemoteApolloConfig.this.apolloOpenApiClient.removeItem(appId,env,cluster,nameSpace,itemDTO.getKey(),"apollo");
//                    });
//                    futureList.add(result);
                }
            }
        } catch (RuntimeException e) {
            log.error("查找namespace【{}】文件失败", nameSpace);
        }
        try {
            if (openNamespaceDTO == null) {
                OpenAppNamespaceDTO openAppNamespaceDTO = new OpenAppNamespaceDTO();
                openAppNamespaceDTO.setAppId(this.appId);
                openAppNamespaceDTO.setName(nameSpace);
                openAppNamespaceDTO.setPublic(this.isPublic);
//                openAppNamespaceDTO.setAppendNamespacePrefix("apollo");
                openAppNamespaceDTO.setAppendNamespacePrefix(true);
                this.apolloOpenApiClient.createAppNamespace(openAppNamespaceDTO);
            }
            if (oldList != null) {
                list = this.getProcessList(list, oldList);
            }
            Iterator iterator = list.iterator();
            while (iterator.hasNext()) {
                final OpenItemDTO itemDTO = (OpenItemDTO)iterator.next();
//                Future result = this.executorItemService.submit((run()->{
//                    UpdateRemoteApolloConfig.log.debug("删除远程配置文件【{}】中的{}",UpdateRemoteApolloConfig.this.fileName,itemDTO.toString());
//                    UpdateRemoteApolloConfig.this.apolloOpenApiClient.createOrUpdateItem(appId,env,cluster,nameSpace,itemDTO.getKey(),"apollo");
//                });
//                futureList.add(result);
            }
            iterator = futureList.iterator();
            while (iterator.hasNext()) {
                Future future = (Future) iterator.next();
                future.get();
            }
            NamespaceReleaseDTO releaseDTO = new NamespaceReleaseDTO();
//            releaseDTO.setReleaseDTO("apollo");
            releaseDTO.setReleaseComment("apollo");
            releaseDTO.setReleaseTitle("Auto Publish");
            this.apolloOpenApiClient.publishNamespace(this.appId,this.env,this.cluster,nameSpace,releaseDTO);
            log.info("配置文件【{}】处理成功",nameSpace);

        }catch (Exception e) {
            log.error("配置文件【{}】",nameSpace);
            return false;
        }
        return true;
    }

    @Override
    public Boolean call() throws Exception {
        return this.updateRemoteConfig();
    }

    private List<OpenItemDTO> getDelList(List<OpenItemDTO> newlist, List<OpenItemDTO> oldlist) {
        List<OpenItemDTO> distinctByUniquelist = (List)oldlist.stream().filter(item -> {
            return !((List)newlist.stream().map( e -> {
               return e.getKey();
            }).collect(Collectors.toList())).contains(item.getKey());
        }).collect(Collectors.toList());
        Iterator iterator = distinctByUniquelist.iterator();
        while (iterator.hasNext()) {
            OpenItemDTO openItemDTO = (OpenItemDTO)iterator.next();
            log.info("需要删除的item{}", openItemDTO.toString());
        }
        return distinctByUniquelist;
    }

    private List<OpenItemDTO> getProcessList(List<OpenItemDTO> newlist, List<OpenItemDTO> oldlist) {
        List<OpenItemDTO> distinctByUniquelist = (List)newlist.stream().filter(item -> {
            return !((List)oldlist.stream().map( e -> {
                return e.getKey();
            }).collect(Collectors.toList())).contains(item.getKey());
        }).collect(Collectors.toList());
        Iterator iterator = distinctByUniquelist.iterator();
        while (iterator.hasNext()) {
            OpenItemDTO openItemDTO = (OpenItemDTO)iterator.next();
            log.info("需要删除的item{}", openItemDTO.toString());
        }
        return distinctByUniquelist;
    }
}

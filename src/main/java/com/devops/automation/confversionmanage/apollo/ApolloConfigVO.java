package com.devops.automation.confversionmanage.apollo;

import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
public class ApolloConfigVO {
    private String env;
    private String appID;
    private String dusID;
    private String portalURL;
    private String token;
    private boolean isPublic;
    private String filePath;
}

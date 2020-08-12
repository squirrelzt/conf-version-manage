package com.devops.automation.confversionmanage.domain;

import lombok.Data;

@Data
public class GitLog {
    private String message;
    private String author;
    private String operationTime;
    private String branch;
}

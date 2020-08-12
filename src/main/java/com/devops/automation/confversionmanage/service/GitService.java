package com.devops.automation.confversionmanage.service;

import com.devops.automation.confversionmanage.domain.GitLog;
import org.eclipse.jgit.api.errors.GitAPIException;

import java.io.IOException;
import java.util.List;

public interface GitService {

    void gitClone() throws GitAPIException;

    List<GitLog> getGitLog() throws IOException, GitAPIException;
}

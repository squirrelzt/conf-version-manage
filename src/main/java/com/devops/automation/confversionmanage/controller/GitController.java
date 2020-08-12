package com.devops.automation.confversionmanage.controller;

import com.devops.automation.confversionmanage.domain.GitLog;
import com.devops.automation.confversionmanage.service.GitService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;
import java.util.List;

@Slf4j
@RestController
@RequestMapping("git")
public class GitController {

    @Autowired
    GitService gitService;

    @GetMapping("log")
    public List<GitLog> getLog() {
        try {
            return gitService.getGitLog();
        } catch (IOException e) {
            log.error(e.getMessage());
        } catch (GitAPIException e) {
            log.error(e.getMessage());
        }
        return null;
    }

    @GetMapping("clone")
    public void gitClone() {
        try {
            gitService.gitClone();
        } catch (GitAPIException e) {
            log.error(e.getMessage());
        }
    }
}

package com.devops.automation.confversionmanage.service.impl;

import com.devops.automation.confversionmanage.domain.GitLog;
import com.devops.automation.confversionmanage.git.CloneRemoteRepository;
import com.devops.automation.confversionmanage.git.SimpleProgressMonitor;
import com.devops.automation.confversionmanage.service.GitService;
import lombok.extern.slf4j.Slf4j;
import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class GitServiceImpl implements GitService {

    @Value("${git.url}")
    private String url;
    @Value("${git.username}")
    private String username;
    @Value("${git.password}")
    private String password;
    @Value("${git.local.clone-path}")
    private String clonePath;
    @Value("${git.local.repository-path}")
    private String repositoryPath;

    @Override
    public void gitClone() throws GitAPIException {
        UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider(username,password);
        Git.cloneRepository()
                .setCredentialsProvider(usernamePasswordCredentialsProvider)
                .setCloneAllBranches(true)
                .setURI(url)
                .setDirectory(new File(clonePath))
                .setProgressMonitor(new SimpleProgressMonitor())
                .call();
    }

    @Override
    public List<GitLog> getGitLog() throws IOException, GitAPIException {
        List<GitLog> logs = new ArrayList<>();
        Repository repo = new FileRepository(new File(repositoryPath));
        Git git = new Git(repo);
        Iterable<RevCommit> resuts = git.log().call();
        resuts.forEach(revCommit -> {
            String message = revCommit.getFullMessage();
            String author = revCommit.getAuthorIdent().getName();
            String operationTime = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(revCommit.getAuthorIdent().getWhen());

            log.info("{}  {}  {}", message, author, operationTime);
            GitLog log = new GitLog();
            log.setMessage(message);
            log.setAuthor(author);
            log.setOperationTime(operationTime);
            logs.add(log);
        });
        return logs;
    }
}

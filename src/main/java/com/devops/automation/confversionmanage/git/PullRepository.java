package com.devops.automation.confversionmanage.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.ListBranchCommand;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.Ref;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.transport.UsernamePasswordCredentialsProvider;

import java.io.File;
import java.io.IOException;
import java.util.List;

public class PullRepository {
    public static void main(String[] args) throws IOException, GitAPIException {
        UsernamePasswordCredentialsProvider usernamePasswordCredentialsProvider = new UsernamePasswordCredentialsProvider("","");
        Repository repo = new FileRepository(new File("D://gitProject/.git"));
        Git git = new Git(repo);
        git.pull().setCredentialsProvider(usernamePasswordCredentialsProvider).setRemote("origin")
                .setRemoteBranchName("master");
        List<Ref> list = git.branchList().call();
        for (int i = 0; i < list.size(); i++) {
            Ref ref = list.get(i);
            System.out.println(ref.getName());
        }
    }
}

package com.devops.automation.confversionmanage.git;

import org.eclipse.jgit.api.Git;
import org.eclipse.jgit.api.Status;
import org.eclipse.jgit.api.errors.GitAPIException;
import org.eclipse.jgit.internal.storage.file.FileRepository;
import org.eclipse.jgit.lib.ObjectId;
import org.eclipse.jgit.lib.PersonIdent;
import org.eclipse.jgit.lib.Repository;
import org.eclipse.jgit.revwalk.RevCommit;
import org.eclipse.jgit.revwalk.RevTree;

import java.io.File;
import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Iterator;

public class LogRepository {
    public static void main(String[] args) throws IOException, GitAPIException {
        Repository repo = new FileRepository(new File("D://gitProject/.git"));
        Git git = new Git(repo);
        Status status = git.status().call();
        System.out.println(status);

        Iterable<RevCommit> resuts = git.log().call();
        resuts.forEach(revCommit -> {
            System.out.println(revCommit.toString());
            RevTree revTree = revCommit.getTree();
            String revTreeName = revTree.getName();
            System.out.println("revTreeName: "+ revTreeName);
            PersonIdent personIdent = revCommit.getAuthorIdent();
            System.out.println("提交人： "+ personIdent.getName());
            System.out.println("邮箱: "+ personIdent.getEmailAddress());
            System.out.println("提交时间: "+new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(personIdent.getWhen()));
            String fullMessage = revCommit.getFullMessage();
            System.out.println("fullMessage: "+fullMessage);
            String shortMessage = revCommit.getShortMessage();
            System.out.println(shortMessage);
            ObjectId id = revCommit.getId();
            System.out.println("id: " + id.getName());
            String name = revCommit.getName();
            System.out.println("name: "+name);
        });
    }
}

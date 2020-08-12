package com.devops.automation.confversionmanage.git;

import org.eclipse.jgit.lib.ProgressMonitor;

public class SimpleProgressMonitor implements ProgressMonitor {
    @Override
    public void start(int totalTasks) {

    }

    @Override
    public void beginTask(String title, int totalWork) {

    }

    @Override
    public void update(int completed) {

    }

    @Override
    public void endTask() {

    }

    @Override
    public boolean isCancelled() {
        return false;
    }
}

package com.belosh.portal.server.entity;

public class ServerDefinition {
    private int serverPort;
    private int connectionTimeout;
    private int maxThreads;
    private int idleThreads;
    private int threadTimeout;
    private boolean cachedPool;
    private boolean unpackWARs;
    private boolean autoDeploy;

    public int getServerPort() {
        return serverPort;
    }

    public void setServerPort(int serverPort) {
        this.serverPort = serverPort;
    }

    public int getConnectionTimeout() {
        return connectionTimeout;
    }

    public void setConnectionTimeout(int connectionTimeout) {
        this.connectionTimeout = connectionTimeout;
    }

    public int getMaxThreads() {
        return maxThreads;
    }

    public void setMaxThreads(int maxThreads) {
        this.maxThreads = maxThreads;
    }

    public int getIdleThreads() {
        return idleThreads;
    }

    public void setIdleThreads(int idleThreads) {
        this.idleThreads = idleThreads;
    }

    public int getThreadTimeout() {
        return threadTimeout;
    }

    public void setThreadTimeout(int threadTimeout) {
        this.threadTimeout = threadTimeout;
    }

    public boolean isCachedPool() {
        return cachedPool;
    }

    public void setCachedPool(boolean cachedPool) {
        this.cachedPool = cachedPool;
    }

    public boolean isUnpackWARs() {
        return unpackWARs;
    }

    public void setUnpackWARs(boolean unpackWARs) {
        this.unpackWARs = unpackWARs;
    }

    public boolean isAutoDeploy() {
        return autoDeploy;
    }

    public void setAutoDeploy(boolean autoDeploy) {
        this.autoDeploy = autoDeploy;
    }

    @Override
    public String toString() {
        return "ServerDefinition{" +
                "serverPort=" + serverPort +
                ", connectionTimeout=" + connectionTimeout +
                ", maxThreads=" + maxThreads +
                ", idleThreads=" + idleThreads +
                ", threadTimeout=" + threadTimeout +
                ", cachedPool=" + cachedPool +
                ", unpackWARs=" + unpackWARs +
                ", autoDeploy=" + autoDeploy +
                '}';
    }
}

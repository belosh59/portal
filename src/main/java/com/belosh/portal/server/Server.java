package com.belosh.portal.server;

import com.belosh.portal.http.parser.RequestParser;
import com.belosh.portal.server.entity.ServerDefinition;
import com.belosh.portal.application.ApplicationManager;
import com.belosh.portal.application.ApplicationScanner;
import com.belosh.portal.server.handler.RequestHandler;
import com.belosh.portal.server.parser.ServerDefinitionParser;
import com.sun.istack.internal.NotNull;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

class Server {
    private final static Logger logger = LoggerFactory.getLogger(Server.class);
    private final static String DEFAULT_SERVER_CONFIG_PATH = "/server.yml";

    public static void main(String[] args) {
        // Starting Server
        ServerSocket serverSocket;
        ServerDefinition serverDefinition = ServerDefinitionParser.parseServerDefinition(DEFAULT_SERVER_CONFIG_PATH);
        try {
            serverSocket = new ServerSocket(serverDefinition.getServerPort());
            logger.info("Servlet container server started on port: {}", serverDefinition.getServerPort());
        } catch (IOException e) {
            throw new RuntimeException("Could not start server", e);
        }

        ApplicationManager applicationManager = new ApplicationManager();

        RequestParser requestParser = new RequestParser(applicationManager);

        // Configure Application Scanner
        ApplicationScanner applicationScanner = new ApplicationScanner(applicationManager);
        applicationScanner.setAutoDeploy(serverDefinition.isAutoDeploy());
        applicationScanner.setUnpackWARs(serverDefinition.isUnpackWARs());

        // Configure Thread Pool
        int maxThreads = serverDefinition.getMaxThreads();
        boolean isCached = serverDefinition.isCachedPool();
        int corePoolSize = isCached ? serverDefinition.getIdleThreads() : maxThreads;
        long keepAliveTime = isCached ? serverDefinition.getThreadTimeout() : 0L;
        BlockingQueue<Runnable> workQueue = isCached ? new SynchronousQueue<>() : new LinkedBlockingQueue<>();
        PortalThreadFactory portalThreadFactory = new PortalThreadFactory();

        ExecutorService executorService = new ThreadPoolExecutor(
                corePoolSize,
                maxThreads,
                keepAliveTime,
                TimeUnit.SECONDS,
                workQueue,
                portalThreadFactory);
        executorService.execute(applicationScanner);

        //noinspection InfiniteLoopStatement
        while (true) {
            try {
                Socket socket = serverSocket.accept();

                logger.info("Socket Remote Address: {}, Local Socket Address: {}",
                        socket.getRemoteSocketAddress(),
                        socket.getLocalSocketAddress());
                RequestHandler requestHandler = new RequestHandler(socket, requestParser);

                executorService.execute(requestHandler);
            } catch (IOException e) {
                logger.error("Unable to accept socket", e);
            }
        }
    }

    static class PortalThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix = "request-handlers-thread-";

        @Override
        public Thread newThread(@SuppressWarnings("NullableProblems") Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(namePrefix + threadNumber.getAndIncrement());
            thread.setUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception in thread: {}", t.getName(), e));
            return thread;
        }
    }
}

package com.belosh.portal.server;

import com.belosh.portal.http.parser.RequestParser;
import com.belosh.portal.server.entity.ServerDefinition;
import com.belosh.portal.application.ApplicationManager;
import com.belosh.portal.application.ApplicationScanner;
import com.belosh.portal.server.handler.RequestHandler;
import com.belosh.portal.server.parser.ServerDefinitionParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.*;
import java.util.concurrent.atomic.AtomicInteger;

public class Server {
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
        ExecutorService executorService;
        PortalThreadFactory portalThreadFactory = new PortalThreadFactory();

        if (serverDefinition.isCachedPool()) {
            executorService = new ThreadPoolExecutor(serverDefinition.getIdleThreads(),
                    serverDefinition.getMaxThreads(),
                    serverDefinition.getThreadTimeout(),
                    TimeUnit.SECONDS,
                    new SynchronousQueue<>(),
                    portalThreadFactory);
        } else {
            int maxThreads = serverDefinition.getMaxThreads();
            executorService = new ThreadPoolExecutor(maxThreads,
                    maxThreads,
                    0L,
                    TimeUnit.MILLISECONDS,
                    new LinkedBlockingQueue<>(),
                    portalThreadFactory);
        }
        executorService.submit(applicationScanner);

        while (true) {
            try {
                Socket socket = serverSocket.accept();

                logger.info("Socket Remote Address: {}, Local Socket Address: {}",
                        socket.getRemoteSocketAddress(),
                        socket.getLocalSocketAddress());
                RequestHandler requestHandler = new RequestHandler(socket, requestParser);

                executorService.submit(requestHandler);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("Unable to accept socket", e);
            }
        }
    }

    // TODO: Looks like not working
    static class PortalThreadFactory implements ThreadFactory {
        private final AtomicInteger threadNumber = new AtomicInteger(1);
        private final String namePrefix = "request-handlers-thread-";

        @Override
        public Thread newThread(Runnable r) {
            Thread thread = new Thread(r);
            thread.setName(namePrefix + threadNumber.getAndIncrement());
            thread.setUncaughtExceptionHandler((t, e) -> logger.error("Uncaught exception in thread: {}", t.getName(), e));
            return thread;
        }
    }
}

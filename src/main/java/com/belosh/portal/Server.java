package com.belosh.portal;

import com.belosh.portal.entity.ServerDefinition;
import com.belosh.portal.parser.ServerDefinitionParser;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.net.ServerSocket;
import java.net.Socket;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

public class Server {
    private final Logger logger = LoggerFactory.getLogger(getClass());
    private final String DEFAULT_SERVER_CONFIG_PATH = "/server.yml";
    private ExecutorService executorService;
    private ApplicationScanner applicationScanner = new ApplicationScanner();
    private ApplicationManager applicationManager = new ApplicationManager();

    public void start() {
        ServerDefinition serverDefinition = ServerDefinitionParser.parseServerDefinition(DEFAULT_SERVER_CONFIG_PATH);

        //Starting server
        ServerSocket serverSocket;
        try {
            serverSocket = new ServerSocket(serverDefinition.getServerPort());
            logger.info("Servlet container server started on port: {}", serverDefinition.getServerPort());
        } catch (IOException e) {
            throw new RuntimeException("Could not start server", e);
        }

        // Configure threadpool and ApplicationScanner
        // TODO: Figureout how to implement max/min/idle threads count
        if (serverDefinition.isCachedPool()) {
            executorService = Executors.newCachedThreadPool();
        } else {
            int maxThreads = serverDefinition.getMaxThreads();
            executorService = Executors.newFixedThreadPool(maxThreads);
        }
        applicationScanner.setApplicationManager(applicationManager);
        applicationScanner.setApplicationScannerInterval(serverDefinition.getApplicationScannerInterval());
        executorService.submit(applicationScanner);

        while (true) {
            try {
                Socket socket = serverSocket.accept();
                RequestHandler requestHandler = new RequestHandler();
                requestHandler.setSocket(socket);
                requestHandler.setApplicationManager(applicationManager);

                executorService.submit(requestHandler);
            } catch (IOException e) {
                e.printStackTrace();
                logger.error("Unable to accept socket");
            }
        }
    }
}

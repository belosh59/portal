## Portal WebServer

OpenSource Servlet Container:

TODO:
* [x] Change File class usege to Path class for pathes
* [x] ThreadPool configuration should be added: Give a name to threads in pool / Set UncaughtExceptionHandler
* [x] Implements NIO File System Events
* [x] UnpackWars and AutoDeploy parameters should be processed
* [x] Deploy online-shop App based on ServiceLocator and IOC. Spring classloader integration required
* [x] Servlet lifecycle init -> serve -> destroy should be fully covered
* [x] start.sh / stop.sh
* [x] PortalServlet Streams should be closed in try-with-resources clause in Request Handler
* [x] Package structure refactored
* [x] Reuse Socket input/output streams. Implement content length. Do not close connection with clients
* [x] Chain of responsibility. Filters processing in web.xml
* [x] Implement Cookie processing
* [x] POST Request body should be processed
* [x] Principal should be processed as well
* Thymeleaf is not working with PortalServletContext during processing of template variables
* Ensure that logger process cause of exception
* Move all constants to separate class
* Assets failing to be processing from time to time, need deeper investigation

Advanced TODO:
* Welcome files in web.xml

Refactored:
1. ApplicationScannerInterval parameter removed as not needed after implementation #4 from TODO list

Notes:
* Google Guava used to copy input/output streams
* Downcasting objects: security problem - http://www.onjava.com/pub/a/onjava/2003/05/14/java_webserver.html?page=4# Army
* Application entity should be considered as ServletContext
* Chunked HTTP 1.1 response:
    - https://stackoverflow.com/questions/14381825/java-servlet-httpresponse-contentlenght-header/
    - https://docs.oracle.com/javaee/6/api/javax/servlet/http/HttpServlet.html#doGet%28javax.servlet.http.HttpServletRequest,%20javax.servlet.http.HttpServletResponse%29
    - https://zoompf.com/blog/2012/05/too-chunky/
* Servlet specification: https://www.tutorialspoint.com/servlets/servlets-quick-guide.htm
* Shutting down the tomcat via 8005 port - http://www.avajava.com/tutorials/lessons/how-do-i-prevent-people-from-shutting-down-my-tomcat.html?page=1
* Servlet URL matching strategy: https://stackoverflow.com/questions/24538007/can-i-do-this-complex-url-mapping-in-web-container-using-servlet

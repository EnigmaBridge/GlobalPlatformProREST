#REST interface for GlobalPlatformPro
[GlobalPlatformPro](https://github.com/martinpaljak/GlobalPlatformPro)
is a nice tool for JavaCard management (e.g., applet install, info). 

This project builds REST interface on top of it so it is possible to 
install java card applets remotely.


## Info

* Provides simple REST binding
* Uses Spring-boot, Spring 4, Gradle

## ToDo

* HTTPS (Letsencrypt)
* Authentication / API key
* Specify allowed commands / cards to use

## Running
There are several ways for testing this REST server. 

### Embedded container - Gradle

Start embedded Tomcat container:

```
./gradlew bootRun
```

Server is reachable on [`http://127.0.0.1:8081`](http://127.0.0.1:8081)

### Embedded container - JAR

To build JAR with embedded servlet container use

```
./gradlew bootRepackage
```

Then run the server

```
java -jar rest/build/libs/gppro-rest-0.1.0.jar
```

### WAR

You can build WAR archive which can be used in the servlet containers such as Tomcat.
To build war:

```
./gradlew war
```

### Example

```
http://127.0.0.1:8081/raw/?request=--debug%20--list
```


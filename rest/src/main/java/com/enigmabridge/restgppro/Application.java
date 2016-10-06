package com.enigmabridge.restgppro;

import com.enigmabridge.restgppro.utils.NamedThreadFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.context.annotation.Bean;
import org.springframework.scheduling.annotation.EnableAsync;
import org.springframework.scheduling.annotation.EnableScheduling;

import java.util.concurrent.Executor;
import java.util.concurrent.Executors;

@org.springframework.context.annotation.Configuration
@EnableAsync
@EnableScheduling
@SpringBootApplication
public class Application implements CommandLineRunner {
    private static final String ROUTER_RELOAD_EXECUTOR = "reloadExecutor";
    private static final String SERVER_RESYNC_EXECUTOR = "resyncExecutor";
    private static final Logger LOG = LoggerFactory.getLogger(Application.class);

    @Autowired
    private ErrorAttributes errorAttributes;

    public static void main(String[] args) {
        SpringApplication.run(Application.class, args);
    }

    @Bean
    public AppErrorController appErrorController(){return new AppErrorController(errorAttributes);}

    /*
    @Bean
    public EmbeddedServletContainerFactory servletContainer() {
        TomcatEmbeddedServletContainerFactory factory = new TomcatEmbeddedServletContainerFactory();
        factory.setPort(8081);
        factory.setSessionTimeout(50, TimeUnit.MINUTES);
        return factory;
    }*/

    @Override
    public void run(String... args) throws Exception {

        //update/create configuration file
        LOG.info("Started...");
    }

    @Bean(name = ROUTER_RELOAD_EXECUTOR)
    public Executor reloadExecutor() {
        return Executors.newSingleThreadExecutor(new NamedThreadFactory("router-reload-exec"));
    }

    @Bean(name = SERVER_RESYNC_EXECUTOR)
    public Executor resyncExecutor() {
        return Executors.newSingleThreadExecutor(new NamedThreadFactory("server-resync-exec"));
    }
}

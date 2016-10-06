/*
 * Copyright (c) 2016 Enigma Bridge Ltd.
 *
 * This file is part of the GlobalPlatformProREST project.
 *
 *     GlobalPlatformProREST is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     GlobalPlatformProREST is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with GlobalPlatformProREST.  If not, see <http://www.gnu.org/licenses/>.
 *
 *     If you have any support question, use the GitHub facilities. Visit http://enigmabridge.com
 *     if you want to speak to us directly.
 */

package com.enigmabridge.restgppro;

import org.apache.coyote.http11.Http11NioProtocol;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.embedded.EmbeddedServletContainerCustomizer;
import org.springframework.boot.context.embedded.tomcat.TomcatEmbeddedServletContainerFactory;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;

import java.io.File;

@Configuration
@DependsOn(value = ApiConfig.YAML_CONFIG)
public class ContainerConfiguration {
    private final static Logger LOG = LoggerFactory.getLogger(ContainerConfiguration.class);

    @Bean
    @DependsOn(value = ApiConfig.YAML_CONFIG)
    EmbeddedServletContainerCustomizer containerCustomizer(
            @Value("${keystore.file}") String keystoreFile,
            @Value("${server.port}") final String serverPort,
            @Value("${keystore.pass}") final String keystorePass,
            @Value("${server.protocol}") final String scheme)
            throws Exception {

        // This is boiler plate code to setup https on embedded Tomcat
        // with Spring Boot:

        final String absoluteKeystoreFile = new File(keystoreFile)
                .getAbsolutePath();

        if (scheme.equalsIgnoreCase("https")) {
            LOG.info("Starting HTTPS endpoint on port: {}", serverPort);
        } else {
            LOG.info("Starting HTTP endpoint on port: {}", serverPort);
        }
        return container -> {
            TomcatEmbeddedServletContainerFactory tomcat = (TomcatEmbeddedServletContainerFactory) container;
            tomcat.addConnectorCustomizers(connector -> {
                connector.setPort(Integer.parseInt(serverPort));
                if (scheme.equalsIgnoreCase("https")) {
                    connector.setSecure(true);
                    connector.setScheme("https");
                    Http11NioProtocol proto = (Http11NioProtocol) connector
                            .getProtocolHandler();
                    proto.setSSLEnabled(true);
                    proto.setKeystoreFile(absoluteKeystoreFile);
                    proto.setKeystorePass(keystorePass);
                    proto.setKeystoreType("JKS");
                    proto.setKeyAlias("tomcat");
                } else {
                    connector.setScheme("http");
                    connector.setProtocol("http");
                }
            });
        };
    }
}

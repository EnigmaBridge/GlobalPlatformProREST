/*
 * Enigma Bridge Ltd ("COMPANY") CONFIDENTIAL Unpublished Copyright (c) 2016-2016.
 * Enigma Bridge Ltd, All Rights Reserved.
 *
 *  NOTICE: All information contained herein is, and remains the property of COMPANY. The intellectual and technical
 *  concepts contained herein are proprietary to COMPANY and may be covered by U.K, U.S., and Foreign Patents, patents
 *  in process, and are protected by trade secret or copyright law. Dissemination of this information or reproduction
 *  of this material is strictly forbidden unless prior written permission is obtained from COMPANY. Access to the
 *  source code contained herein is hereby forbidden to anyone except current COMPANY employees, managers or
 *  contractors who have executed Confidentiality and Non-disclosure agreements explicitly covering such access.
 *
 *  The copyright notice above does not evidence any actual or intended publication or disclosure of this source code,
 *  which includes information that is confidential and/or proprietary, and is a trade secret, of COMPANY. ANY
 *  REPRODUCTION, MODIFICATION,  DISTRIBUTION, PUBLIC PERFORMANCE, OR PUBLIC DISPLAY OF OR THROUGH USE
 *  OF THIS SOURCE CODE WITHOUT THE EXPRESS WRITTEN CONSENT OF COMPANY IS STRICTLY PROHIBITED, AND IN
 *  VIOLATION OF APPLICABLE LAWS AND INTERNATIONAL TREATIES. THE RECEIPT OR POSSESSION OF THIS SOURCE CODE
 *  AND/OR RELATED INFORMATION DOES NOT CONVEY OR IMPLY ANY RIGHTS TO REPRODUCE, DISCLOSE OR DISTRIBUTE
 *  ITS CONTENTS, OR TO MANUFACTURE, USE, OR SELL ANYTHING THAT IT MAY DESCRIBE, IN WHOLE OR IN PART.
 *
 *  @author: Enigma Bridge
 *  @credits:
 *  @version: 1.0
 *  @email: info@enigmabridge.com
 *  @status: Production
 *
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

        LOG.info("Starting HTTPS endpoint on port: {}", serverPort);
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

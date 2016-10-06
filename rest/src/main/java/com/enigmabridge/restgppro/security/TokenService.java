package com.enigmabridge.restgppro.security;


import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import java.util.Collections;
import java.util.List;

@EnableConfigurationProperties
public class TokenService {
    private static final Logger logger = LoggerFactory.getLogger(TokenService.class);

    @Autowired
    protected AuthConfiguration authConfiguration;

    /**
 * Created by Enigma Bridge Ltd (dan) on 06/10/2016.
     * Checks if the given token for management requests is valid.
     * @param token token
     * @return true if valid
     */
    public boolean containsManagementToken(String token) {
        List<String> tokenList = authConfiguration != null ?
                authConfiguration.getManagementTokens() :
                Collections.emptyList();

        return tokenList.contains(token);
    }

    /**
     * Checks if the given token for business requests from EB is valid.
     * @param token token
     * @return true if valid
     */
    public boolean containsBusinessToken(String token) {
        List<String> tokenList = authConfiguration != null ?
                authConfiguration.getBusinessTokens() :
                Collections.emptyList();

        return tokenList.contains(token);
    }
}

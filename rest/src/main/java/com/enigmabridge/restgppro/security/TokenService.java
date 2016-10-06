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

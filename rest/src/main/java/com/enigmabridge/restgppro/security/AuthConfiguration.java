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

import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.Collections;
import java.util.List;

/**
 * Created by Enigma Bridge Ltd (dan) on 06/10/2016.
 */
@ConfigurationProperties(prefix = "auth")
@Configuration
public class AuthConfiguration {
    protected Management management;
    protected Business business;

    public AuthConfiguration() {
    }

    public AuthConfiguration(Management management, Business business) {
        this.management = management;
        this.business = business;
    }

    public Management getManagement() {
        return management;
    }

    public Business getBusiness() {
        return business;
    }

    public void setManagement(Management management) {
        this.management = management;
    }

    public void setBusiness(Business business) {
        this.business = business;
    }

    public List<String> getManagementTokens(){
        return management != null && management.getTokens() != null ?
                Collections.unmodifiableList(management.getTokens()) :
                Collections.emptyList();
    }

    public List<String> getBusinessTokens(){
        return business != null && business.getTokens() != null ?
                Collections.unmodifiableList(business.getTokens()) :
                Collections.emptyList();
    }

    public static class Management {
        private List<String> tokens;

        public Management() {
        }

        public Management(List<String> tokens) {
            this.tokens = tokens;
        }

        public List<String> getTokens() {
            return tokens;
        }

        public void setTokens(List<String> tokens) {
            this.tokens = tokens;
        }
    }

    public static class Business {
        private List<String> tokens;

        public Business() {
        }

        public Business(List<String> tokens) {
            this.tokens = tokens;
        }

        public List<String> getTokens() {
            return tokens;
        }

        public void setTokens(List<String> tokens) {
            this.tokens = tokens;
        }
    }
}

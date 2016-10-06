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

/**
 * Service configuration strings.
 * <p>
 * Created by dusanklinec on 01.08.16.
 */
public class ApiConfig {
    public static final int UMG_CURRENT_API_VERSION = 1;
    public static final String API_PATH = "/api/v" + UMG_CURRENT_API_VERSION;

    public static final String AUTHENTICATE_URL = API_PATH + "/authenticate";
    public static final String UO_EVENT_URL = API_PATH + "/evt/uo";

    // Spring Boot Actuator services
    public static final String AUTOCONFIG_ENDPOINT = "/autoconfig";
    public static final String BEANS_ENDPOINT = "/beans";
    public static final String CONFIGPROPS_ENDPOINT = "/configprops";
    public static final String ENV_ENDPOINT = "/env";
    public static final String MAPPINGS_ENDPOINT = "/mappings";
    public static final String METRICS_ENDPOINT = "/metrics";
    public static final String SHUTDOWN_ENDPOINT = "/shutdown";
    public static final String CLIENT_ENDPOINT = "/client";
    public static final String APIKEY_ENDPOINT = "/apikey";

    public static final String MANAGEMENT_ROLE_SUFFIX = "BACKEND_ADMIN";
    public static final String MANAGEMENT_ROLE = "ROLE_" + MANAGEMENT_ROLE_SUFFIX;

    public static final String BUSINESS_ROLE_SUFIX = "BUSINESS_ADMIN";
    public static final String BUSINESS_ROLE = "ROLE_" + BUSINESS_ROLE_SUFIX;

    public static final String YAML_CONFIG = "yaml-config";
    public static final String ROUTER = "router";
}

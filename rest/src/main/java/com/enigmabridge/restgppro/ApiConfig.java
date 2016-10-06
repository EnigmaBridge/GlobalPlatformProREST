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

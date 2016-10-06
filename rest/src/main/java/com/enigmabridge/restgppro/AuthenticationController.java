package com.enigmabridge.restgppro;

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

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class AuthenticationController {

    @RequestMapping(value = ApiConfig.AUTHENTICATE_URL, method = RequestMethod.POST)
    public String authenticate() {
        return "This is just for in-code-documentation purposes and Rest API reference documentation." +
                "Servlet will never get to this point as Http requests are processed by AuthenticationFilter." +
                "Nonetheless to authenticate Domain User POST request with X-Auth-Username and X-Auth-Password headers " +
                "is mandatory to this URL. If username and password are correct valid token will be returned (just json string in response) " +
                "This token must be present in X-Auth-Token header in all requests for all other URLs, including logout." +
                "Authentication can be issued multiple times and each call results in new ticket.";
    }
}

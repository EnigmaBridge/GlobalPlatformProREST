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

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.web.ErrorAttributes;
import org.springframework.boot.autoconfigure.web.ErrorController;
import org.springframework.util.Assert;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;
import org.springframework.web.context.request.RequestAttributes;
import org.springframework.web.context.request.ServletRequestAttributes;

import javax.servlet.http.HttpServletRequest;
import java.util.Map;


@RestController
@RequestMapping("/error")
public class AppErrorController implements ErrorController {

    private final ErrorAttributes errorAttributes;

    @Autowired
    public AppErrorController(ErrorAttributes errorAttributes) {
        Assert.notNull(errorAttributes, "ErrorAttributes must not be null");
        this.errorAttributes = errorAttributes;
    }

    @Override
    public String getErrorPath() {
        return "/error";
    }

    @RequestMapping
    public Map<String, Object> error(HttpServletRequest aRequest) {
        Map<String, Object> body = getErrorAttributes(aRequest, getTraceParameter(aRequest));
        String trace = (String) body.get("trace");
        if (trace != null) {
            String[] lines = trace.split("\n\t");
            body.put("trace", lines);
        }
        return body;
    }

    private boolean getTraceParameter(HttpServletRequest request) {
        String parameter = request.getParameter("trace");
        if (parameter == null) {
            return false;
        }
        return !"false".equals(parameter.toLowerCase());
    }

    private Map<String, Object> getErrorAttributes(HttpServletRequest aRequest, boolean includeStackTrace) {
        RequestAttributes requestAttributes = new ServletRequestAttributes(aRequest);
        return errorAttributes.getErrorAttributes(requestAttributes, includeStackTrace);
    }
}
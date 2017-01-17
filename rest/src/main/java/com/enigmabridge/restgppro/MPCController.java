/*
 * Copyright (c) 2017 Enigma Bridge Ltd.
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

import com.enigmabridge.restgppro.response.GeneralResponse;
import com.enigmabridge.restgppro.rest.JsonEnvelope;
import org.json.JSONObject;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;
import java.util.concurrent.atomic.AtomicLong;

/**
 * Created by dusanklinec on 20.07.16.
 */
@RestController
@PreAuthorize("hasAuthority('" + ApiConfig.BUSINESS_ROLE + "')")
public class MPCController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();
    private static final String MPC_PATH = ApiConfig.API_PATH+"/mpc";

    @RequestMapping(MPC_PATH+"/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                String.format(template, name));
    }

    @RequestMapping(value = MPC_PATH + "/setup", method = RequestMethod.POST)
    public GeneralResponse apiUpdated(@RequestBody String jsonStr, HttpServletRequest request) {
        long timeStart = System.currentTimeMillis();
        JsonEnvelope message = null;
        String remoteIPAddress = request.getRemoteAddr();
        GeneralResponse msgBack = null;

        JSONObject parsedContent = new JSONObject(jsonStr);




        return msgBack;
    }
}

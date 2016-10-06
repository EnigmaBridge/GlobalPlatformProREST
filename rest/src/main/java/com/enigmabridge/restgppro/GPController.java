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

import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.javacard.gp.GPArgumentTokenizer;
import pro.javacard.gp.GPTool;

import java.io.IOException;
import java.io.OutputStream;
import java.io.PrintStream;
import java.security.NoSuchAlgorithmException;
import java.util.List;
import java.util.concurrent.atomic.AtomicLong;

import static com.enigmabridge.restgppro.ApiConfig.API_PATH;

/**
 * Created by dusanklinec on 20.07.16.
 */
@RestController
@PreAuthorize("hasAuthority('" + ApiConfig.BUSINESS_ROLE + "')")
public class GPController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping(API_PATH+"/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                String.format(template, name));
    }

    @RequestMapping(API_PATH+"/raw")
    public void rawRequest(@RequestParam(value = "request") String request, OutputStream output) throws IOException, NoSuchAlgorithmException {
        final PrintStream ps = new PrintStream(output, true);
        final GPTool tool = new GPTool(ps, ps);
        final List<String> inputArgs = GPArgumentTokenizer.tokenize(request);
        final int code = tool.work(inputArgs.toArray(new String[inputArgs.size()]));
        ps.println("Done: " + code);
    }
}

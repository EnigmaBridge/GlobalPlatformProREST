package com.enigmabridge.restgppro;

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

/**
 * Created by dusanklinec on 20.07.16.
 */
@RestController
public class GPController {
    private static final String template = "Hello, %s!";
    private final AtomicLong counter = new AtomicLong();

    @RequestMapping("/greeting")
    public Greeting greeting(@RequestParam(value="name", defaultValue="World") String name) {
        return new Greeting(counter.incrementAndGet(),
                String.format(template, name));
    }

    @RequestMapping("/raw")
    public void rawRequest(@RequestParam(value = "request") String request, OutputStream output) throws IOException, NoSuchAlgorithmException {
        final PrintStream ps = new PrintStream(output, true);
        final GPTool tool = new GPTool(ps, ps);
        final List<String> inputArgs = GPArgumentTokenizer.tokenize(request);
        final int code = tool.work(inputArgs.toArray(new String[inputArgs.size()]));
        ps.println("Done: " + code);
    }
}

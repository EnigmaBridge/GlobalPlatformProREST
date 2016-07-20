package com.enigmabridge.restgppro;

import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;
import pro.javacard.gp.GPTool;

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
        GPTool tool;


        return new Greeting(counter.incrementAndGet(),
                String.format(template, name));
    }
}

package com.example.workshops_evidence.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/")
public class Test {

    @GetMapping("{get}")
    public String greeting(@PathVariable int get) {
        return "Hello World" + get;
    }
}

package com.example.clouduser.controller;

import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

/**
 * @author : poet_Dai
 * @create:
 * @Description:
 */

@RestController()
@RequestMapping("/v1")
public class TestController {

    @PostMapping("/get")
    public String get(){return "order";}
}

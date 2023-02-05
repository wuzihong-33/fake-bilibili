package com.bilibili.api;

import com.bilibili.service.DemoService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RequestMapping("/demo")
@RestController
public class DemoApi {

    @Autowired
    private DemoService demoService;

    @GetMapping("/test")
    public String test() {
        return "test api access success";
    }

//    @GetMapping("/query")
//    public String query(Long id) {
//        return demoService.query(id);
//    }
}

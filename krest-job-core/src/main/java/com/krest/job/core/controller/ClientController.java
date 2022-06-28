package com.krest.job.core.controller;

import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping
public class ClientController {

    /**
     * 检测服务是否存活
     *
     * @return
     */
    @GetMapping("detect/service")
    public boolean detectService() {
        return true;
    }
}

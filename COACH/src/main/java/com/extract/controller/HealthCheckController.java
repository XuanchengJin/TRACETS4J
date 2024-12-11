package com.extract.controller;

import com.alibaba.fastjson.JSONObject;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping(path = "/", produces = "application/json")
public class HealthCheckController {
    @RequestMapping(value = "/health", method = RequestMethod.GET)
    public JSONObject healthCheck() {
        JSONObject jsonObject = new JSONObject();
        jsonObject.put("code", 0);
        return jsonObject;
    }
}

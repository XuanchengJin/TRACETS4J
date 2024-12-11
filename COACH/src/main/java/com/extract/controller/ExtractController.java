package com.extract.controller;

import jakarta.validation.Valid;
import jakarta.validation.constraints.NotNull;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import com.extract.exception.CException;
import com.extract.request.ExtractRequest;
import com.extract.response.ApiResponse;
import com.extract.service.ExtractService;

@RestController
@RequestMapping("/v1/iCode")
@Slf4j
public class ExtractController {
    @Autowired
    private ExtractService extractService;

    @RequestMapping(value = "/extract", method = RequestMethod.POST)
    public ResponseEntity<Object> generateCode(@RequestBody @Valid @NotNull ExtractRequest request) throws CException {
        log.info("start handle code generation request.");
        extractService.dataSetExtract(request);
        log.info("end handle code generation request.");
        return ApiResponse.newOKResponse(null);
    }
}

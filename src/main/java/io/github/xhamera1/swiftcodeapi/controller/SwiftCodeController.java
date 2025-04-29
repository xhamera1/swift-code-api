package io.github.xhamera1.swiftcodeapi.controller;

import io.github.xhamera1.swiftcodeapi.dto.SwiftCodeRequest;
import io.github.xhamera1.swiftcodeapi.dto.SwiftCodeResponse;
import io.github.xhamera1.swiftcodeapi.service.SwiftCodeApiService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.lang.model.element.Name;

@RestController()
@RequestMapping("/v1/swift-codes")
public class SwiftCodeController {

    public final Logger log = LoggerFactory.getLogger(SwiftCodeController.class);
    public final SwiftCodeApiService swiftCodeApiService;

    @Autowired
    public SwiftCodeController(SwiftCodeApiService swiftCodeApiService) {
        this.swiftCodeApiService = swiftCodeApiService;
    }

    @GetMapping("{swift-code}")
    public ResponseEntity<SwiftCodeResponse> getDetailsFromSwiftCode(@PathVariable(name = "swift-code") String swiftCode) {
        log.info("Received request to get details for SWIFT code: {}", swiftCode);
        SwiftCodeResponse swiftCodeResponse = swiftCodeApiService.getSwiftCodeDetails(swiftCode);
        return ResponseEntity.ok(swiftCodeResponse);
    }

    @GetMapping("/country")
}

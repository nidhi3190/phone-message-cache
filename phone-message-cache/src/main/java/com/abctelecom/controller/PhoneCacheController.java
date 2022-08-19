/*
 * *
 *  * The MIT License (MIT)
 *  * <p>
 *  * Copyright (c) 2022
 *  * <p>
 *  * Permission is hereby granted, free of charge, to any person obtaining a copy
 *  * of this software and associated documentation files (the "Software"), to deal
 *  * in the Software without restriction, including without limitation the rights
 *  * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 *  * copies of the Software, and to permit persons to whom the Software is
 *  * furnished to do so, subject to the following conditions:
 *  * <p>
 *  * The above copyright notice and this permission notice shall be included in all
 *  * copies or substantial portions of the Software.
 *  * <p>
 *  * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 *  * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 *  * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 *  * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 *  * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 *  * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 *  * SOFTWARE.
 *
 */

package com.abctelecom.controller;

import com.abctelecom.aspects.CollectLog;
import com.abctelecom.data.dto.ServiceResponse;
import com.abctelecom.data.entity.Account;
import com.abctelecom.data.entity.PhoneNumber;
import com.abctelecom.service.bussiness.MessagingService;
import com.abctelecom.service.security.AuthenticationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.media.Content;
import io.swagger.v3.oas.annotations.media.Schema;
import io.swagger.v3.oas.annotations.responses.ApiResponse;
import io.swagger.v3.oas.annotations.responses.ApiResponses;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestHeader;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

/**
 * Rest Controller class. Exposes all the public APIs to the outside world.
 */
@RestController
public class PhoneCacheController {

    @Autowired
    private AuthenticationService authenticationService;

    @Autowired
    private MessagingService messagingService;

    /**
     * This controller method will fetch the denomination details from ATM. <br>
     * This is more of a audit feature. No authentication is added here.
     *
     * @return {@link PhoneNumber} A list of Denomination Details.
     */
    @CollectLog
    @Operation(summary = "Put the values in phone cache")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Will respond with denomination details", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceResponse.class))})})
    @PostMapping("/inbound/sms")
    public ServiceResponse putToCache(@RequestHeader(HttpHeaders.AUTHORIZATION) String basicAuthToken, @RequestParam String from, @RequestParam String to, @RequestParam String text) {
        Account account = null;
        try {
            account = authenticationService.authenticateAccount(basicAuthToken);
        } catch (Exception exp) {
            return new ServiceResponse("Authentication failed", "Username or password is invalid");
        }
        return messagingService.processInboundMessage(account, from, to, text);
    }

    @CollectLog
    @Operation(summary = "Outbound API")
    @ApiResponses(value = {@ApiResponse(responseCode = "200", description = "Will respond with denomination details", content = {@Content(mediaType = "application/json", schema = @Schema(implementation = ServiceResponse.class))})})
    @PostMapping("/outbound/sms")
    public ServiceResponse outBound(@RequestHeader(HttpHeaders.AUTHORIZATION) String basicAuthToken, @RequestParam String from, @RequestParam String to, @RequestParam String text) {
        Account account = null;
        try {
            account = authenticationService.authenticateAccount(basicAuthToken);
        } catch (Exception exp) {
            return new ServiceResponse("Authentication failed", "Username or password is invalid");
        }
        return messagingService.processOutBoundMessage(account, from, to, text);
    }

}

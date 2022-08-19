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

package com.abctelecom.service.bussiness;

import com.abctelecom.cache.CacheManager;
import com.abctelecom.cache.entries.FromMessage;
import com.abctelecom.cache.entries.StopMessage;
import com.abctelecom.data.dto.ServiceResponse;
import com.abctelecom.data.entity.Account;
import com.abctelecom.data.entity.PhoneNumber;
import com.abctelecom.data.repository.PhoneNumberRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashSet;
import java.util.Objects;
import java.util.Optional;
import java.util.Set;

@Service
public class MessagingService {

    @Autowired
    private PhoneNumberRepository phoneNumberRepository;

    @Autowired
    private CacheManager cacheManager;

    private static Set<String> stopMessages = new HashSet<>();

    static {
        stopMessages.add("STOP");
        stopMessages.add("STOP\n");
        stopMessages.add("STOP\r\n");
        stopMessages.add("STOP\r");
    }

    public ServiceResponse processInboundMessage(Account account, String from, String to, String text) {
        try {
            if (Objects.isNull(text) || (text.length() < 1 && to.length() > 120)) {
                return new ServiceResponse("", "text Parameter is invalid");
            }
            if (Objects.isNull(to) || (to.length() < 1 && to.length() > 16)) {
                return new ServiceResponse("", "to Parameter is invalid");
            }
            Optional<PhoneNumber> optionalPhone = this.phoneNumberRepository.findByAccountIdAndNumber(account.getId(), to);
            if (optionalPhone.isEmpty()) {
                return new ServiceResponse("", "to Parameter not found");
            }
            if (stopMessages.contains(text)) {
                StopMessage stopMessage = new StopMessage(from, to);
                this.cacheManager.addToCache(stopMessage);
            }
            return new ServiceResponse("inbound SMS OK", "");
        } catch (Exception exp) {
            exp.printStackTrace();
            return new ServiceResponse("", "UnKnown Failure");
        }
    }

    public ServiceResponse processOutBoundMessage(Account account, String from, String to, String text) {
        try {
            if (Objects.isNull(text) || (text.length() < 1 && to.length() > 120)) {
                return new ServiceResponse("", "text Parameter is invalid");
            }
            if (Objects.isNull(from) || (to.length() < 1 && from.length() > 16)) {
                return new ServiceResponse("", "from Parameter is invalid");
            }
            Optional<PhoneNumber> optionalPhone = this.phoneNumberRepository.findByAccountIdAndNumber(account.getId(), from);
            if (optionalPhone.isEmpty()) {
                return new ServiceResponse("", "from Parameter not found");
            }

            FromMessage fromMessage = new FromMessage(from);
            FromMessage fromMessageFromCache = (FromMessage) this.cacheManager.get(fromMessage);
            if (Objects.nonNull(fromMessageFromCache)) {
                if (fromMessageFromCache.isThresholdCrossed()) {
                    return new ServiceResponse("", "limit reached for from [" + from + "]");
                }
                fromMessageFromCache.incrementRequestCount();
            } else {
                this.cacheManager.addToCache(fromMessage);
            }

            StopMessage stopMessage = new StopMessage(from, to);
            if (this.cacheManager.isPresent(stopMessage)) {
                return new ServiceResponse("", "SMS from [" + from + "] to [" + to + "] blocked by STOP Request");
            }
            return new ServiceResponse("outbound SMS OK", "");
        } catch (Exception exp) {
            exp.printStackTrace();
            return new ServiceResponse("", "UnKnown Failure");
        }
    }
}

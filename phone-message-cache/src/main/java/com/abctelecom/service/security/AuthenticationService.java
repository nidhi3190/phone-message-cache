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

package com.abctelecom.service.security;

import com.abctelecom.data.entity.Account;
import com.abctelecom.data.repository.AccountRepository;
import com.abctelecom.exception.custom_exceptions.AuthenticationException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.Base64;
import java.util.Optional;

@Service
public class AuthenticationService {

    @Autowired
    AccountRepository accountRepository;

    public Account authenticateAccount(String authorizationHeaderValue) {
        authorizationHeaderValue = authorizationHeaderValue.split(" ")[1];
        String decodedValue = new String(Base64.getDecoder().decode(authorizationHeaderValue));
        String userName = decodedValue.split(":")[0];
        String authToken = decodedValue.split(":")[1];
        Optional<Account> accountOptional = this.accountRepository.findByUsername(userName);
        if (accountOptional.isPresent() && accountOptional.get().getAuth_id().equals(authToken)) {
            return accountOptional.get();
        }
        throw new AuthenticationException("Username or password not valid");
    }
}

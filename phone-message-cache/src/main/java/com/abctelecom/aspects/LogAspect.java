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

package com.abctelecom.aspects;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.stereotype.Component;

import java.time.Instant;
import java.util.Objects;

@Component
@Aspect
public class LogAspect {
    private final Logger logger = LogManager.getLogger(this.getClass());

    @Around("@annotation(CollectLog)")
    public Object miscUtils(ProceedingJoinPoint joinPoint) throws Throwable {
        logger.info(this.prepareLogMessage(joinPoint, true, false));
        try {
            Object proceed = joinPoint.proceed();
            logger.info(this.prepareLogMessage(joinPoint, true, false));
            return proceed;
        } catch (Exception exp) {
            logger.error(this.prepareLogMessage(joinPoint, true, true));
            throw exp;
        }
    }

    /**
     * Prepares the log based on entry and exit conditions.
     *
     * @param joinPoint
     * @param isEntry
     * @return
     */
    private String prepareLogMessage(ProceedingJoinPoint joinPoint, Boolean isEntry, Boolean isException) {
        StringBuilder message = new StringBuilder();
        message.append("Time:[").append(Instant.now().toEpochMilli()).append("]");
        if (isEntry) {
            message.append(", Calling : [");
        } else {
            if (isException) {
                message.append(", Found Exception : [");
            } else {
                message.append(", Completed : [");
            }
        }
        message.append(joinPoint.getTarget().getClass().getSimpleName());
        message.append(", Method: ").append(joinPoint.getSignature().getName());
        if (isEntry) {
            message.append(", with Arguments : [").append(this.getFieldLog(joinPoint, message)).append("]");
        }
        message.append("]");
        return message.toString();
    }

    private StringBuilder getFieldLog(ProceedingJoinPoint joinPoint, StringBuilder sb) {
        for (Object args : joinPoint.getArgs()) {
            if (Objects.nonNull(args)) {
                sb.append(args.toString());
            } else {
                sb.append("null");
            }
        }
        return sb;
    }
}

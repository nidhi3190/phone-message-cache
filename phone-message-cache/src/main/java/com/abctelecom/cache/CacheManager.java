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

package com.abctelecom.cache;

import com.abctelecom.cache.entries.Evictable;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.time.Duration;
import java.time.Instant;
import java.util.Map;
import java.util.Objects;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope("singleton")
public class CacheManager {
    private Map<Evictable, CacheEntry> cache = new ConcurrentHashMap<>();

    public void addToCache(Evictable k) {
        this.evict(k);
        if (!this.isPresent(k)) {
            CacheEntry cacheEntry = new CacheEntry(Instant.now(),
                    Instant.now().plus(Duration.ofHours(k.getEvictionTimeInHours())), k);
            this.cache.put(k, cacheEntry);
        }
    }

    public void evict(Evictable evictable) {
        CacheEntry cacheEntry = this.cache.get(evictable);
        if (Objects.nonNull(cacheEntry) && cacheEntry.shouldEvict()) {
            this.cache.remove(evictable);
        }
    }

    public Boolean isPresent(Evictable evictable) {
        return this.cache.containsKey(evictable);
    }

    public Evictable get(Evictable evictable) {
        CacheEntry ce = this.cache.get(evictable);
        if (Objects.isNull(ce)) {
            return null;
        }
        return ce.getCachedValue();
    }
}

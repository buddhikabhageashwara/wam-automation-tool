/*
 * MIT License
 *
 * Copyright (c) 2024 buddhika bhageashwara alwis
 *
 * Permission is hereby granted, free of charge, to any person obtaining a copy
 * of this software and associated documentation files (the "Software"), to deal
 * in the Software without restriction, including without limitation the rights
 * to use, copy, modify, merge, publish, distribute, sublicense, and/or sell
 * copies of the Software, and to permit persons to whom the Software is
 * furnished to do so, subject to the following conditions:
 *
 * The above copyright notice and this permission notice shall be included in all
 * copies or substantial portions of the Software.
 *
 * THE SOFTWARE IS PROVIDED "AS IS", WITHOUT WARRANTY OF ANY KIND, EXPRESS OR
 * IMPLIED, INCLUDING BUT NOT LIMITED TO THE WARRANTIES OF MERCHANTABILITY,
 * FITNESS FOR A PARTICULAR PURPOSE AND NONINFRINGEMENT. IN NO EVENT SHALL THE
 * AUTHORS OR COPYRIGHT HOLDERS BE LIABLE FOR ANY CLAIM, DAMAGES OR OTHER
 * LIABILITY, WHETHER IN AN ACTION OF CONTRACT, TORT OR OTHERWISE, ARISING FROM,
 * OUT OF OR IN CONNECTION WITH THE SOFTWARE OR THE USE OR OTHER DEALINGS IN THE
 * SOFTWARE.
 */

package wam.automationtool.application.util;


import java.util.Objects;
import org.ehcache.Cache;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import wam.automationtool.application.dto.cache.CacheDataDto;

@Component
public class WAMCacheManager {

  @Autowired private Cache<String, CacheDataDto> wamCache;

  public CacheDataDto getCacheDataDto(final String key) {
    CacheDataDto cacheDataDto = null;
    if (Objects.nonNull(wamCache)) {
      cacheDataDto = wamCache.get(key);
    }
    return cacheDataDto;
  }

  public void addToCache(final String key, final CacheDataDto cacheDataDto) {
      // Retrieve the item before adding it to avoid overwriting the existing cache entry.
      if (Objects.nonNull(wamCache)) {
      wamCache.put(key, cacheDataDto);
    }
  }

  public void removeItemFromCache(final String key) {
    // This needs to be called once after the execution is completed.
    // Also, this request should be sent to remote agents as well.
    if (Objects.nonNull(wamCache)) {
      wamCache.remove(key);
    }
  }

  public void clearAllCacheItems() {
    if (Objects.nonNull(wamCache)) {
      wamCache.clear();
    }
  }
}

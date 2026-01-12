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

package wam.automationtool.application.config;

import static wam.automationtool.application.config.AppConstant.WAM_CACHE_MANAGER;

import jakarta.annotation.PostConstruct;
import java.util.Objects;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import wam.automationtool.application.dto.cache.CacheDataDto;

@Configuration
public class WAMCacheConfig {

  private CacheManager cacheManager;

  private Cache<String, CacheDataDto> wamCache;

  @PostConstruct
  public void init() {
    this.cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    this.wamCache = cacheManager.getCache(WAM_CACHE_MANAGER, String.class, CacheDataDto.class);
    if (Objects.isNull(wamCache)) {
      this.wamCache =
          cacheManager.createCache(
              WAM_CACHE_MANAGER,
              CacheConfigurationBuilder.newCacheConfigurationBuilder(
                      String.class,
                      CacheDataDto.class,
                      ResourcePoolsBuilder.heap(Long.MAX_VALUE).offheap(500, MemoryUnit.MB))
                  .withExpiry(ExpiryPolicyBuilder.noExpiration()));
    }
  }

  @Bean
  public CacheManager getWAMCacheManager() {
    return cacheManager;
  }

  @Bean
  public Cache<String, CacheDataDto> wamCache() {
    return wamCache;
  }

  public void destroyCache() {
    if (Objects.nonNull(wamCache)) {
      cacheManager.removeCache(WAM_CACHE_MANAGER);
    }
    cacheManager.close();
  }
}

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

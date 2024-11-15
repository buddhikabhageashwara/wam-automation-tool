package wam.automationtool.application.util;

import static wam.automationtool.application.config.AppConstant.WAM_CACHE_MANAGER;

import java.util.Objects;
import org.ehcache.Cache;
import org.ehcache.CacheManager;
import org.ehcache.config.builders.CacheConfigurationBuilder;
import org.ehcache.config.builders.CacheManagerBuilder;
import org.ehcache.config.builders.ExpiryPolicyBuilder;
import org.ehcache.config.builders.ResourcePoolsBuilder;
import org.ehcache.config.units.MemoryUnit;
import wam.automationtool.application.dto.cache.CacheDataDto;

public class WAMCacheManager {

  public static Cache<String, CacheDataDto> initiateCache() {
    final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    Cache<String, CacheDataDto> wamCache =
        cacheManager.getCache(WAM_CACHE_MANAGER, String.class, CacheDataDto.class);
    if (Objects.isNull(wamCache)) {
      wamCache =
          cacheManager.createCache(
              WAM_CACHE_MANAGER,
              CacheConfigurationBuilder.newCacheConfigurationBuilder(
                      String.class,
                      CacheDataDto.class,
                      ResourcePoolsBuilder.heap(Long.MAX_VALUE).offheap(500, MemoryUnit.MB))
                  .withExpiry(ExpiryPolicyBuilder.noExpiration()));
    }
    return wamCache;
  }

  public static CacheDataDto getCacheDataDto(final String key) {
    CacheDataDto cacheDataDto = null;
    final Cache<String, CacheDataDto> wamCache = getCache();
    if(Objects.nonNull(wamCache)) {
      cacheDataDto = wamCache.get(key);
    }
    return cacheDataDto;
  }

  private static Cache<String, CacheDataDto> getCache() {
    final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    final Cache<String, CacheDataDto> wamCache =
        cacheManager.getCache(WAM_CACHE_MANAGER, String.class, CacheDataDto.class);
    return wamCache;
  }

  private static void addToCache(final String key, final CacheDataDto cacheDataDto) {
    final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    final Cache<String, CacheDataDto> wamCache =
        cacheManager.getCache(WAM_CACHE_MANAGER, String.class, CacheDataDto.class);
    if (Objects.nonNull(wamCache)) {
      wamCache.put(key, cacheDataDto);
    }
  }

  public static void removeFromCache(final String key) {
    final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    final Cache<String, CacheDataDto> wamCache =
            cacheManager.getCache(WAM_CACHE_MANAGER, String.class, CacheDataDto.class);
    if (Objects.nonNull(wamCache)) {
      wamCache.remove(key);
    }
  }

  public static void destroyCache() {
    final CacheManager cacheManager = CacheManagerBuilder.newCacheManagerBuilder().build(true);
    final Cache<String, CacheDataDto> wamCache =
        cacheManager.getCache(WAM_CACHE_MANAGER, String.class, CacheDataDto.class);
    if (Objects.nonNull(wamCache)) {
      cacheManager.removeCache(WAM_CACHE_MANAGER);
    }
    cacheManager.close();
  }
}

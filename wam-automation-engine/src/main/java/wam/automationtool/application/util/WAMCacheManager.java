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

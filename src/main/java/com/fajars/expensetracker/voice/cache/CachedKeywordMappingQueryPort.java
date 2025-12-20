
package com.fajars.expensetracker.voice.cache;

import com.fajars.expensetracker.voice.dto.*;
import com.fajars.expensetracker.voice.JpaKeywordMappingAdapter;
import com.fajars.expensetracker.voice.KeywordMappingQueryPort;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.cache.*;
import org.springframework.context.annotation.*;
import org.springframework.stereotype.*;
import java.util.*;

@Component
@Primary
public class CachedKeywordMappingQueryPort implements KeywordMappingQueryPort {

    private final KeywordMappingQueryPort delegate;
    private final CacheManager cm;

    public CachedKeywordMappingQueryPort(
        JpaKeywordMappingAdapter d, @Qualifier("keywordCacheManager") CacheManager cm) {
        this.delegate = d;
        this.cm = cm;
    }

    @Override
    public List<KeywordMapping> findByUserIdAndType(UUID u, KeywordType t) {
        String cacheName =
            t == KeywordType.CATEGORY ? "keyword-category-mapping" : "keyword-wallet-mapping";
        Cache c = cm.getCache(cacheName);
        String key = u.toString();

        if (c != null) {
            var v = c.get(key, List.class);
            if (v != null) {
                return v;
            }
        }

        var res = delegate.findByUserIdAndType(u, t);
        if (c != null) {
            c.put(key, res);
        }

        return res;
    }
}

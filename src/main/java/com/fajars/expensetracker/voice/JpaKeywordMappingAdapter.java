
package com.fajars.expensetracker.voice;

import com.fajars.expensetracker.voice.dto.KeywordMapping;
import com.fajars.expensetracker.voice.dto.KeywordType;
import com.fajars.expensetracker.voice.repository.KeywordMappingRepository;
import java.util.List;
import java.util.UUID;
import org.springframework.stereotype.Component;

@Component
public class JpaKeywordMappingAdapter implements KeywordMappingQueryPort,
    KeywordMappingCommandPort {

    private final KeywordMappingRepository repo;

    public JpaKeywordMappingAdapter(KeywordMappingRepository r) {
        this.repo = r;
    }

    public List<KeywordMapping> findByUserIdAndType(UUID u, KeywordType t) {
        return repo.findByUserIdAndType(u, t).stream()
            .map(e -> new KeywordMapping(e.getId(), e.getKeyword(), e.getTargetId(), e.getType()))
            .toList();
    }

    public void save(UUID u, KeywordMapping m) {
        repo.save(KeywordMappingEntity.from(u, m.keyword(), m.type(), m.targetId()));
    }

    public void delete(UUID u, UUID id) {
        repo.deleteById(id);
    }
}

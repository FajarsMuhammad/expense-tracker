
package com.fajars.expensetracker.voice.repository;

import com.fajars.expensetracker.voice.KeywordMappingEntity;
import com.fajars.expensetracker.voice.dto.*;
import org.springframework.data.jpa.repository.*;
import java.util.*;

public interface KeywordMappingRepository extends JpaRepository<KeywordMappingEntity, UUID> {

    List<KeywordMappingEntity> findByUserIdAndType(UUID userId, KeywordType type);

    boolean existsByUserIdAndKeywordAndType(UUID userId, String keyword, KeywordType type);
}

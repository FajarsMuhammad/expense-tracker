
package com.fajars.expensetracker.voice;

import com.fajars.expensetracker.voice.dto.*;
import jakarta.persistence.*;
import java.util.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
@Entity
@Table(name = "keyword_mappings")
public class KeywordMappingEntity {

    @Id
    @GeneratedValue
    private UUID id;

    private UUID userId;

    private String keyword;

    @Enumerated(EnumType.STRING)
    private KeywordType type;

    private UUID targetId;

    public static KeywordMappingEntity from(UUID u, String k, KeywordType t, UUID tid) {
        var e = new KeywordMappingEntity();
        e.userId = u;
        e.keyword = k;
        e.type = t;
        e.targetId = tid;
        return e;
    }
}

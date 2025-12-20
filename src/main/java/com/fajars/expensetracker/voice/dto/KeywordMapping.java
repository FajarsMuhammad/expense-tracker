
package com.fajars.expensetracker.voice.dto;

import java.util.UUID;

public record KeywordMapping(
    UUID id,
    String keyword,
    UUID targetId,
    KeywordType type
) {}

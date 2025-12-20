
package com.fajars.expensetracker.voice;

import com.fajars.expensetracker.voice.dto.*;
import java.util.*;

public interface KeywordMappingQueryPort {

    List<KeywordMapping> findByUserIdAndType(UUID userId, KeywordType type);
}

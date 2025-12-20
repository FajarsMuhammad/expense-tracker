
package com.fajars.expensetracker.voice;

import com.fajars.expensetracker.voice.dto.*;
import java.util.*;

public interface KeywordMappingCommandPort {

    void save(UUID userId, KeywordMapping mapping);

    void delete(UUID userId, UUID id);
}

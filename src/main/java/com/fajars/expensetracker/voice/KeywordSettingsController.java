
package com.fajars.expensetracker.voice;

import com.fajars.expensetracker.voice.dto.*;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;
import java.util.*;

@RestController
@RequestMapping("/settings/keywords")
@RequiredArgsConstructor
public class KeywordSettingsController {

    private final KeywordMappingCommandPort cmd;

    private final KeywordMappingQueryPort qry;

    @PostMapping
    public void create(@RequestBody KeywordMapping m) {
        cmd.save(UUID.randomUUID(), m);
    }

    @GetMapping
    public List<KeywordMapping> list(@RequestParam KeywordType type) {
        return qry.findByUserIdAndType(UUID.randomUUID(), type);
    }
}

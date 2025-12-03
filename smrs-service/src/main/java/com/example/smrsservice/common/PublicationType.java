package com.example.smrsservice.common;

import lombok.AllArgsConstructor;
import lombok.Getter;
@AllArgsConstructor
@Getter
public enum PublicationType {
    JOURNAL("Journal", "Tạp chí"),
    CONFERENCE("Conference", "Hội thảo");

    private final String jsonName;
    private final String displayName;
}

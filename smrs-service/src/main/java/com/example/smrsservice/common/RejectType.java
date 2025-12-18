package com.example.smrsservice.common;

import com.fasterxml.jackson.annotation.JsonCreator;
import com.fasterxml.jackson.annotation.JsonValue;

public enum RejectType {
    REVISION("Revision"),      // Cho cơ hội sửa lại
    PERMANENT("Permanent");    // Trả về kho cho nhóm khác

    private final String jsonName;

    RejectType(String jsonName) {
        this.jsonName = jsonName;
    }

    @JsonValue
    public String getJsonName() {
        return jsonName;
    }

    @JsonCreator
    public static RejectType from(String value) {
        if (value == null) {
            throw new IllegalArgumentException("RejectType is required");
        }
        for (RejectType type : values()) {
            if (type.jsonName.equalsIgnoreCase(value) || type.name().equalsIgnoreCase(value)) {
                return type;
            }
        }
        throw new IllegalArgumentException("Invalid reject type: " + value);
    }
}

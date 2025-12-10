package com.example.smrsservice.dto.account;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class ImportDeanResult {

    private Integer totalRows;
    private Integer successCount;
    private Integer failedCount;

    private List<String> successEmails;
    private List<String> failedEmails;
    private List<String> errors;
}

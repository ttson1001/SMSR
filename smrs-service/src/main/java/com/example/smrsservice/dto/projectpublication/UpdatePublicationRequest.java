package com.example.smrsservice.dto.projectpublication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UpdatePublicationRequest {
    private String publicationName;  // Optional
    private String publicationType;  // Optional: JOURNAL, CONFERENCE
    private String publicationLink;  // Optional
    private String status;  // Optional: REGISTERED, PUBLISHED, CANCELLED
    private String notes;  // Optional
    private String doi;  // Optional
    private String isbnIssn;  // Optio
}

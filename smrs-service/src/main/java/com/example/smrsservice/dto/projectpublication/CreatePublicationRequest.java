package com.example.smrsservice.dto.projectpublication;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CreatePublicationRequest {

    private Integer projectId;  // Required
    private String publicationName;  // Required
    private String publicationType;  // JOURNAL, CONFERENCE
    private String publicationLink;  // Optional
    private String notes;  // Optional
    private String doi;  // Optional
    private String isbnIssn;  // Optional
}

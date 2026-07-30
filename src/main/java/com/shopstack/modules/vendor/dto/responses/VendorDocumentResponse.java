package com.shopstack.modules.vendor.dto.responses;

import com.shopstack.common.enums.DocumentType;
import lombok.*;

import java.time.LocalDateTime;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorDocumentResponse {

    private Long documentId;
    private DocumentType documentType;
    private String documentUrl;
    private Boolean verified;
    private LocalDateTime uploadedAt;
}
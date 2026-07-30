package com.shopstack.modules.vendor.dto.requests;

import com.shopstack.common.enums.DocumentType;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import lombok.*;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorDocumentUploadRequest {

    @NotNull(message = "Vendor ID is required")
    private Long vendorId;

    @NotNull(message = "Document type is required")
    private DocumentType documentType;

    @NotBlank(message = "Document URL is required")
    private String documentUrl;
}

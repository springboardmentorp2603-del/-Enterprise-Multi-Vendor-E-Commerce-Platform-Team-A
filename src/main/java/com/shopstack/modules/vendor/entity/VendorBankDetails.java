package com.shopstack.modules.vendor.entity;

import jakarta.persistence.*;
import lombok.*;

@Entity
@Table(name = "vendor_bank_details")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class VendorBankDetails {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "bank_detail_id")
    private Long bankDetailId;

    @ManyToOne(fetch = FetchType.LAZY)
    @JoinColumn(name = "vendor_id", nullable = false)
    private Vendor vendor;

    @Column(name = "account_holder_name", nullable = false, length = 150)
    private String accountHolderName;

    @Column(name = "account_number", nullable = false, length = 50)
    private String accountNumber;

    @Column(name = "bank_name", nullable = false, length = 150)
    private String bankName;

    @Column(name = "ifsc_or_swift_code", nullable = false, length = 20)
    private String ifscOrSwiftCode;

    @Column(name = "is_verified")
    @Builder.Default
    private Boolean isVerified = false;
}
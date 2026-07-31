package com.shopstack.modules.systemlog.entity;

import com.shopstack.common.audit.BaseEntity;
import jakarta.persistence.Column;
import jakarta.persistence.Entity;
import jakarta.persistence.Table;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;

import java.util.UUID;

@Entity
@Table(name = "system_logs")
@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class SystemLog extends BaseEntity {

    @Column(name = "module", length = 50)
    private String module;

    @Column(name = "action", length = 100)
    private String action;

    @Column(name = "performed_by")
    private UUID performedBy;

    @Column(name = "performed_by_name", length = 150)
    private String performedByName;

    @Column(name = "level", length = 10)
    @Builder.Default
    private String level = "INFO";

    @Column(name = "details", length = 500)
    private String details;
}
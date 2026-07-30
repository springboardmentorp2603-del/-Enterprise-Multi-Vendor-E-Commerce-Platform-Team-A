package com.shopstack.modules.admin.dto.requests;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AdminReviewRequest {

    /** Optional note explaining the decision (especially useful on rejection). */
    private String remarks;
}

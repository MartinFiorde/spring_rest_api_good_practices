package com.maf.cashcard;

import org.springframework.data.annotation.Id;

record AuditTrail(
        @Id Long id,
        String objectType,
        Long objectId
) {
}

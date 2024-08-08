package com.maf.cashcard;

import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
interface AuditTrailRepository extends CrudRepository<AuditTrail, Long>, PagingAndSortingRepository<AuditTrail, Long> {
    AuditTrail findByObjectTypeAndObjectId(String objectType, Long objectId);
}

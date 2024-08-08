package com.maf.cashcard;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.PagingAndSortingRepository;
import org.springframework.stereotype.Repository;

@Repository
interface CashCardRepository extends CrudRepository<CashCard, Long>, PagingAndSortingRepository<CashCard, Long> {
    CashCard findByIdAndOwnerAndIsActive(Long id, String owner, Boolean isActive);
    Page<CashCard> findByOwnerAndIsActive(String owner, Boolean isActive, PageRequest pageRequest);
    boolean existsByIdAndOwnerAndIsActive(Long id, String owner, Boolean isActive);
}
package com.maf.cashcard;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.util.UriComponentsBuilder;

import java.net.URI;
import java.security.Principal;
import java.util.List;

@RestController
@RequestMapping("/cashcards")
class CashCardController {

    private final CashCardRepository cashCardRepository;
    private final AuditTrailRepository auditTrailRepository;

    @Autowired
    private CashCardController(CashCardRepository cashCardRepository, AuditTrailRepository auditTrailRepository) {
        this.cashCardRepository = cashCardRepository;
        this.auditTrailRepository = auditTrailRepository;
    }

    @GetMapping
    public ResponseEntity<List<CashCard>> findAll(Pageable pageable, Principal principal) {
        Page<CashCard> page = cashCardRepository.findByOwnerAndIsActive(
                principal.getName(),
                true,
                PageRequest.of(
                        pageable.getPageNumber(),
                        pageable.getPageSize(),
                        pageable.getSortOr(Sort.by(Sort.Direction.ASC, "amount"))
                ));
        return ResponseEntity.ok(page.getContent());
    }

    @GetMapping("/{requestedId}")
    public ResponseEntity<CashCard> findById(@PathVariable Long requestedId, Principal principal) {
        CashCard cashCard = cashCardRepository.findByIdAndOwnerAndIsActive(requestedId, principal.getName(), true);
        return cashCard != null ? ResponseEntity.ok(cashCard) : ResponseEntity.notFound().build();
    }

    @PostMapping
    public ResponseEntity<Void> createCashCard(@RequestBody CashCard newCashCardRequest, UriComponentsBuilder ucb, Principal principal) {
        CashCard cashCardToSave = new CashCard(null, newCashCardRequest.amount(), principal.getName(), true);
        CashCard savedCashCard = cashCardRepository.save(cashCardToSave);
        URI locationOfNewCashCard = ucb
                .path("cashcards/{id}")
                .buildAndExpand(savedCashCard.id())
                .toUri();
        return ResponseEntity.created(locationOfNewCashCard).build();
    }

    @PutMapping("/{requestedId}")
    public ResponseEntity<Void> putCashCard(@PathVariable Long requestedId, @RequestBody CashCard cashCardChanged, Principal principal) {
        CashCard cashCardSearched = cashCardRepository.findByIdAndOwnerAndIsActive(requestedId, principal.getName(), true);
        if (cashCardSearched == null) return ResponseEntity.notFound().build();
        CashCard cashCardUpdated = new CashCard(cashCardSearched.id(), cashCardChanged.amount(), cashCardSearched.owner(), true);
        cashCardRepository.save(cashCardUpdated);
        return ResponseEntity.noContent().build();
    }

    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteCashCard(@PathVariable Long id, Principal principal) {
        CashCard cashCardSearched = cashCardRepository.findByIdAndOwnerAndIsActive(id, principal.getName(), true);
        if (cashCardSearched == null) return ResponseEntity.notFound().build();
        auditTrailRepository.findByObjectTypeAndObjectId(CashCard.class.toString(), id);
        CashCard cashCardDeleted = new CashCard(cashCardSearched.id(), cashCardSearched.amount(), cashCardSearched.owner(), false);
        cashCardRepository.save(cashCardDeleted); // SOFT DELETE
        //cashCardRepository.deleteById(id); // HARD DELETE
        auditTrailRepository.save(new AuditTrail(null, CashCard.class.toString(), id));
        return ResponseEntity.noContent().build();
    }

    @GetMapping("/audittrail/{id}")
    public ResponseEntity<AuditTrail> findAuditTrail(@PathVariable Long id) {
        AuditTrail auditTrail = auditTrailRepository.findByObjectTypeAndObjectId(CashCard.class.toString(), id);
        System.out.println("MAF: "+auditTrail);
        return auditTrail != null ? ResponseEntity.ok(auditTrail) : ResponseEntity.notFound().build();
    }
}
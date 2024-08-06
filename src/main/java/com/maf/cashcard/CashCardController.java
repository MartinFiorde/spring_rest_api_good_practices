package com.maf.cashcard;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/cashcards")
class CashCardController {

    @GetMapping("/{requestedId}")
    public ResponseEntity<CashCard> findById(@PathVariable Long requestedId) {
        if (requestedId == 1000) return ResponseEntity.notFound().build();// idem new ResponseEntity<>(HttpStatus.NOT_FOUND)
        CashCard cashCard = new CashCard(99L, 123.45);
        return ResponseEntity.ok(cashCard);
    }
}
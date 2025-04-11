package com.example.financeapp.controller;

import com.example.financeapp.model.Quote;
import com.example.financeapp.repository.QuoteRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Optional;

@RestController
public class QuoteController {
    @Autowired
    private QuoteRepository quoteRepository;

    @GetMapping("/quotes")
    public List<Quote> getQuotes(@RequestParam("search") Optional<String> searchParam){
        return searchParam.map( param->quoteRepository.getContainingQuote(param) )
                .orElse(quoteRepository.findAll());
    }

    @GetMapping("/quotes/{quoteId}" )
    public ResponseEntity<String> readQuote(@PathVariable("quoteId") Long id) {
        return ResponseEntity.of(quoteRepository.findById(id).map(Quote::getQuote));
    }

    @PostMapping("/quotes")
    public Quote addQuote(@RequestBody String quote) {
        Quote q = new Quote();
        q.setQuote(quote);
        q.setAuthor("Hemingway");
        return quoteRepository.save(q);
    }

    @RequestMapping(value="/quotes/{quoteId}", method=RequestMethod.DELETE)
    public void deleteQuote(@PathVariable(value = "quoteId") Long id) {
        quoteRepository.deleteById(id);
    }
}
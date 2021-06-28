package com.yapily.marvel.controller;

import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yapily.marvel.model.CustomCharacter;
import com.yapily.marvel.service.IMarvelApiCharacterService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.ArrayList;
import java.util.List;


@RestController
@RequestMapping(value = "/characters")
public class MarvelApICharacterController {
    private IMarvelApiCharacterService marvelApiCharacterService;

    @Autowired
    MarvelApICharacterController(IMarvelApiCharacterService marvelApiCharacterService) {
        this.marvelApiCharacterService = marvelApiCharacterService;
    }

    @GetMapping("")
    ResponseEntity<Mono<List<String>>> getCharacterIds(
            @RequestParam(required = false) Integer limit, @RequestParam(required = false) Integer offset) {
        Flux<String> result = this.marvelApiCharacterService.getCharacterIds(limit, offset);

        if (result != null) {
            return new ResponseEntity<>(result.collectList(), HttpStatus.OK);
        }

        return new ResponseEntity<>(Mono.just(new ArrayList()), HttpStatus.INTERNAL_SERVER_ERROR);
    }

    @GetMapping("/{id}")
    public ResponseEntity<Mono<CustomCharacter>> getCharacterById(@PathVariable Integer id) throws Exception {
        Mono<JsonNode> characterMono = this.marvelApiCharacterService.getCharacterById(id);

        ObjectMapper objMapper = new ObjectMapper();
        objMapper.disable(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES);

        Mono<CustomCharacter> result = characterMono.map(
                a -> objMapper.convertValue(a, CustomCharacter.class));

        return new ResponseEntity<>(result, HttpStatus.OK);
    }
}
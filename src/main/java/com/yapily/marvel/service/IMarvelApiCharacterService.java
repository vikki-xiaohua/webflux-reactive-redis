package com.yapily.marvel.service;

import com.fasterxml.jackson.databind.JsonNode;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

public interface IMarvelApiCharacterService {
     Flux<String> getCharacterIds(Integer limit, Integer offset);
     Mono<JsonNode> getCharacterById(Integer characterId) ;
}

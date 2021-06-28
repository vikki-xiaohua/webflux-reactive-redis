package com.yapily.marvel.service;

import com.fasterxml.jackson.databind.JsonNode;
import com.yapily.marvel.util.CommonUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.time.Duration;
import java.time.temporal.ChronoUnit;

@Service
public class MarvelApiCharacterServiceImpl implements IMarvelApiCharacterService {
    public static final String MARVEL_API_GET_CHARACTER_BY_ID_URL = "/characters/{characterId}";
    private Logger logger = LoggerFactory.getLogger(MarvelApiCharacterServiceImpl.class);
    private WebClient webClient = WebClient.builder()
            .baseUrl(CommonUtil.MARVEL_API_CHARACTER_BASE_URL)
            .build();
    ;
    private ReactiveStringRedisTemplate redisTemplate;
    private ReactiveListOperations<String, String> reactiveListOps;

    @Value("${marvel-publicKey}")
    private String marvelPublicKey;

    @Value("${marvel-privateKey}")
    private String marvelPrivateKey;


    @Autowired
    public MarvelApiCharacterServiceImpl(ReactiveStringRedisTemplate redisTemplate) {
        this.redisTemplate = redisTemplate;
        redisTemplate.expire(CommonUtil.MARVEL_API_CHARACTER_KEY, Duration.of(24 * 60 * 60, ChronoUnit.SECONDS));

        this.reactiveListOps = redisTemplate.opsForList();
    }

    @Override
    public Flux<String> getCharacterIds(Integer limit, Integer offset) {
        logger.info("limit: {}, offset: {}", limit, offset);

        Flux<String> flux = reactiveListOps.range(CommonUtil.MARVEL_API_CHARACTER_KEY, offset, limit);
        //flux.subscribe(a-> logger.debug(a));

        return flux;
    }

    @Override
    public Mono<JsonNode> getCharacterById(Integer characterId) {
        logger.info("characterId: {}", characterId);

        long timeStamp = System.currentTimeMillis();
        String stringToHash = timeStamp + marvelPrivateKey + marvelPublicKey;
        String hash = DigestUtils.md5Hex(stringToHash);

        Mono<JsonNode>
                response =
                webClient.get()
                        .uri(uriBuilder -> uriBuilder.path(MARVEL_API_GET_CHARACTER_BY_ID_URL)
                                .queryParam("ts", timeStamp)
                                .queryParam("apikey", marvelPublicKey)
                                .queryParam("hash", hash)
                                .build(characterId))
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve().bodyToMono(JsonNode.class);

        Mono<JsonNode> result = response.map(a -> a.get("data").get("results").get(0));

        logger.info("getCharacter result: {}", result);

        return result;
    }

}
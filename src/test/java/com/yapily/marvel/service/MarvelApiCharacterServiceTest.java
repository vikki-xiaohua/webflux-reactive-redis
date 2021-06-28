package com.yapily.marvel.service;


import com.fasterxml.jackson.databind.JsonNode;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@ActiveProfiles("test")
public class MarvelApiCharacterServiceTest {
    @Autowired
    private IMarvelApiCharacterService marvelApiCharacterService;

    @Test
    public void testGetCharacterById() throws Exception {
        int mock_id = 1011334;
        Mono<JsonNode> result = marvelApiCharacterService.getCharacterById(mock_id);

        Assertions.assertNotNull(result.block());
        Assertions.assertEquals(result.block().get("id").asInt(), mock_id);
    }
}

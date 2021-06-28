package com.yapily.marvel.controller;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.yapily.marvel.service.IMarvelApiCharacterService;
import com.yapily.marvel.service.MarvelApiCharacterServiceImpl;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.reactive.WebFluxTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.context.annotation.Import;
import org.springframework.http.MediaType;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import org.springframework.test.web.reactive.server.WebTestClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;

import java.util.List;

import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.times;

@ExtendWith(SpringExtension.class)
@WebFluxTest(controllers = MarvelApICharacterController.class)
@Import(MarvelApiCharacterServiceImpl.class)
public class MarvelApiCharacterServiceControllerTest {
    @Autowired
    WebTestClient webTestClient;

    @MockBean
    private IMarvelApiCharacterService marvelApiCharacterService;

    @Test
    @Disabled
    void testGetCharacterIds() throws Exception {
        Flux<String> flux = Flux.just("1011334", "1017100", "1009144");

        Mockito.when(marvelApiCharacterService.getCharacterIds(10, 1)).thenReturn(flux);

        webTestClient.get().uri("/characters")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().isOk()
                .expectBody(List.class);

        Mockito.verify(marvelApiCharacterService, times(1)).getCharacterIds(10, 1);
    }

    @Test
    public void testGetCharacterIds_is5xxServerError() throws Exception {
        Mockito.when(marvelApiCharacterService.getCharacterIds(anyInt(), anyInt())).thenReturn(null);

        webTestClient.get().uri("/characters")
                .accept(MediaType.APPLICATION_JSON)
                .exchange()
                .expectStatus().is5xxServerError()
                .expectBody(List.class);
    }

    @Test
    public void testGetCharacterById() throws Exception {
        int mock_id = 1011334;
        String mock_name = "3-D Man";
        String mock_descriptione = "Rick Jones has been Hulk's best bud since day one";

        String jsonString = "{\n" +
                "        \"id\": 1011334,\n" +
                "        \"name\": \"3-D Man\",\n" +
                "        \"description\": \"Rick Jones has been Hulk's best bud since day one\",\n" +
                "        \"modified\": \"2014-04-29T14:18:17-0400\",\n" +
                "        \"thumbnail\": {\n" +
                "          \"path\": \"http://i.annihil.us/u/prod/marvel/i/mg/c/e0/535fecbbb9784\",\n" +
                "          \"extension\": \"jpg\"\n" +
                "        }}";

        ObjectMapper mapper = new ObjectMapper();
        JsonNode actualObj = mapper.readTree(jsonString);
        //System.out.println("actualObj=> " + actualObj);

        Mockito
                .when(marvelApiCharacterService.getCharacterById(mock_id))
                .thenReturn(Mono.just(actualObj));

        webTestClient.get().uri("/characters/{id}", mock_id)
                .exchange()
                .expectStatus().isOk()
                .expectBody()
                .jsonPath("$.name").isNotEmpty()
                .jsonPath("$.id").isEqualTo(mock_id)
                .jsonPath("$.name").isEqualTo(mock_name)
                .jsonPath("$.description").isEqualTo(mock_descriptione);

        Mockito.verify(marvelApiCharacterService, times(1)).getCharacterById(mock_id);
    }

}

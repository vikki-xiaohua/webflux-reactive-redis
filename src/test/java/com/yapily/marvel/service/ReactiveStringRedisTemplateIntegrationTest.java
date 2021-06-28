package com.yapily.marvel.service;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.test.annotation.DirtiesContext;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.junit.jupiter.SpringExtension;
import reactor.core.publisher.Mono;
import reactor.test.StepVerifier;

@ExtendWith(SpringExtension.class)
@SpringBootTest(webEnvironment = SpringBootTest.WebEnvironment.RANDOM_PORT)
@DirtiesContext(classMode = DirtiesContext.ClassMode.AFTER_CLASS)
@ActiveProfiles("test")
@Disabled("Temporarily disabled because of embedded redis issues, works when external Redis exists")
public class ReactiveStringRedisTemplateIntegrationTest {
    private static final String LIST_NAME = "demo_list";

    @Autowired
    private ReactiveStringRedisTemplate redisTemplate;

    private ReactiveListOperations<String, String> reactiveListOps;

    @BeforeEach
    public void setup() {
        reactiveListOps = redisTemplate.opsForList();
    }

    @Test
    public void givenListAndValues_whenLeftPushAndLeftPop_thenLeftPushAndLeftPop() {
        Mono<Long> lPush = reactiveListOps.rightPushAll(LIST_NAME, "first", "second")
                .log("Pushed");

        StepVerifier.create(lPush)
                .expectNext(2L)
                .verifyComplete();

        Mono<String> lPop_1 = reactiveListOps.leftPop(LIST_NAME)
                .log("Popped");

        StepVerifier.create(lPop_1)
                .expectNext("first")
                .verifyComplete();

        Mono<String> lPop_2 = reactiveListOps.leftPop(LIST_NAME)
                .log("Popped");

        StepVerifier.create(lPop_2)
                .expectNext("second")
                .verifyComplete();

        reactiveListOps.delete(LIST_NAME);
    }

}
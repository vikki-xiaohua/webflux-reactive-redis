package com.yapily.marvel.config;

import com.arnaudpiroelle.marvel.api.MarvelApi;
import com.arnaudpiroelle.marvel.api.exceptions.AuthorizationException;
import com.arnaudpiroelle.marvel.api.exceptions.QueryException;
import com.arnaudpiroelle.marvel.api.objects.Character;
import com.arnaudpiroelle.marvel.api.objects.ref.DataWrapper;
import com.arnaudpiroelle.marvel.api.params.name.character.ListCharacterParamName;
import com.arnaudpiroelle.marvel.api.rest.client.RetrofitHttpClient;
import com.arnaudpiroelle.marvel.api.rest.handlers.RestServiceErrorHandler;
import com.arnaudpiroelle.marvel.api.services.sync.CharactersService;
import com.fasterxml.jackson.databind.JsonNode;
import com.google.gson.Gson;
import com.yapily.marvel.util.CommonUtil;
import org.apache.commons.codec.digest.DigestUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.context.annotation.Profile;
import org.springframework.core.env.Environment;
import org.springframework.data.redis.core.ReactiveListOperations;
import org.springframework.data.redis.core.ReactiveStringRedisTemplate;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Component;
import org.springframework.web.reactive.function.client.ExchangeStrategies;
import org.springframework.web.reactive.function.client.WebClient;
import reactor.core.publisher.Flux;
import reactor.core.publisher.Mono;
import retrofit.converter.GsonConverter;

import java.time.Duration;
import java.time.temporal.ChronoUnit;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.atomic.AtomicInteger;

import static retrofit.RestAdapter.LogLevel.NONE;

@Component
@Profile("!test")
public class AppStartupRunner implements ApplicationRunner {
    public static final String MARVEL_API_GET_ALL_CHARACTER_IDS = "/characters";
    private static String marvelPublicKey;
    private static String marvelPrivateKey;
    private final int limit = 100;

    private final AtomicInteger offset = new AtomicInteger(0);
    ScheduledExecutorService executor = Executors.newScheduledThreadPool(5);

    @Autowired
    private Environment env;

    private Logger logger = LoggerFactory.getLogger(AppStartupRunner.class);

    private ReactiveStringRedisTemplate redisTemplate;
    private ReactiveListOperations<String, String> reactiveListOps;

    private WebClient webClient = WebClient.builder()
            .baseUrl(CommonUtil.MARVEL_API_CHARACTER_BASE_URL)
            .exchangeStrategies(ExchangeStrategies.builder()
                    .codecs(configure -> configure
                            .defaultCodecs()
                            .maxInMemorySize(20 * 1024 * 1024))
                    .build())
            .build();

    @Autowired
    public AppStartupRunner(Environment env,
                            ReactiveStringRedisTemplate redisTemplate) {

        marvelPublicKey = env.getProperty("marvel-publicKey");
        marvelPrivateKey = env.getProperty("marvel-privateKey");

        this.redisTemplate = redisTemplate;
        redisTemplate.expire(CommonUtil.MARVEL_API_CHARACTER_KEY, Duration.of(24 * 60 * 60, ChronoUnit.SECONDS));

        this.reactiveListOps = redisTemplate.opsForList();
    }

    @Override
    public void run(ApplicationArguments args) throws Exception {
        reactiveListOps.delete(CommonUtil.MARVEL_API_CHARACTER_KEY);

        int total = getTotal();

        executor.scheduleAtFixedRate(() -> {
            try {
                loadToRedis(total);
            } catch (QueryException | AuthorizationException e) {
                e.printStackTrace();
            }
        }, 0, 2000, TimeUnit.MILLISECONDS);

    }

    private int getTotal() throws AuthorizationException, QueryException {
        MarvelApi
                .configure()
                .withLogLevel(NONE)
                .withConvertor(new GsonConverter(new Gson()))
                .withErrorHandler(new RestServiceErrorHandler())
                .withEndpoint(CommonUtil.MARVEL_API_CHARACTER_GATEWAY)
                .withApiKeys(marvelPublicKey, marvelPrivateKey)
                .withClient(new RetrofitHttpClient())
                .init();

        Map<ListCharacterParamName, String> var1 = new HashMap<>();
        var1.put(ListCharacterParamName.LIMIT, String.valueOf(1));
        var1.put(ListCharacterParamName.OFFSET, String.valueOf(0));

        CharactersService charactersService = MarvelApi.getService(CharactersService.class);
        DataWrapper<Character> temp = charactersService.listCharacter(var1);

        int total = temp.getData().getTotal();

        logger.info("total:{}", total);

        return total;
    }

    private void loadToRedis(int total) throws QueryException, AuthorizationException {
        long timeStamp = System.currentTimeMillis();
        String stringToHash = timeStamp + marvelPrivateKey + marvelPublicKey;
        String hash = DigestUtils.md5Hex(stringToHash);

        Mono<JsonNode> response =
                webClient.get()
                        .uri(uriBuilder -> uriBuilder.path(MARVEL_API_GET_ALL_CHARACTER_IDS)
                                .queryParam("offset", offset.get() * limit)
                                .queryParam("limit", limit)
                                .queryParam("ts", timeStamp)
                                .queryParam("apikey", marvelPublicKey)
                                .queryParam("hash", hash)
                                .build())
                        .accept(MediaType.APPLICATION_JSON)
                        .retrieve().bodyToMono(JsonNode.class);

        if (total - (offset.get()) * limit <= 0) {
            executor.shutdown();
        } else {
            offset.getAndIncrement();

            logger.info("offset:{}, limit:{}", offset.get(), limit);

            Flux<JsonNode> flux1 = response.map(a -> a.get("data").get("results")).flatMapMany(Flux::fromIterable);

            flux1.filter(a -> !a.isNull() && !a.isEmpty() && a.hasNonNull("id"))
                    .subscribe(b -> {
                        Mono<Long> lPush = reactiveListOps.rightPushAll(CommonUtil.MARVEL_API_CHARACTER_KEY, String.valueOf(b.get("id").toString()));
                        lPush.subscribe(s -> logger.debug("rPush=> " + s));
                    });
        }
    }
}
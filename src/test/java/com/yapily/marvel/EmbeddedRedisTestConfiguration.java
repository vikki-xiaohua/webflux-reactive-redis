package com.yapily.marvel;

import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.boot.test.context.TestConfiguration;
import redis.embedded.RedisServer;

import javax.annotation.PostConstruct;
import javax.annotation.PreDestroy;

@TestConfiguration
@ConfigurationProperties(prefix = "spring.redis")
public class EmbeddedRedisTestConfiguration {
    private final redis.embedded.RedisServer redisServer;

    @Value("${spring.redis.host}")
    private String redisHostName;

    @Value("${spring.redis.port}")
    private int redisPort;

    public EmbeddedRedisTestConfiguration(@Value("${spring.redis.port}") final int redisPort) {
        //this.redisServer = new redis.embedded.RedisServer(6379);

        this.redisServer = RedisServer
                .builder()
                .setting("bind " + redisHostName)
                .port(redisPort)
                .setting("maxmemory 128mb")
                .build();
    }

    @PostConstruct
    public void startRedis() {
        this.redisServer.start();
    }

    @PreDestroy
    public void stopRedis() {
        this.redisServer.stop();
    }

}
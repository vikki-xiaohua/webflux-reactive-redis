## Environment Requirements

> Java 11

> Maven

> Redis

## How to Run the Application
```
Run the **run.sh** script in marvel folder
```

or

```
./mvnw  spring-boot:run -Dspring-boot.run.jvmArguments="\
 -Dmarvel-publicKey=5495505f65bb5841a868600d95ab0f22 -Dmarvel-privateKey=67518060e6ebe0da2ff3ab5817f9b4fa98327e39"
```

### Run Maven Test

mvn clean test -Dspring.profiles.active=test

## Test APIs

### getCharacterIds

```
GET  
http://localhost:8080/characters?limit=5&offset=0

[
    "1011334",
    "1017100",
    "1009144",
    "1010699",
    "1009146"
]

```

### getCharacterById

```
 GET
 http://localhost:8080/characters/1011334

{
    "id": 1011334,
    "name": "3-D Man",
    "description": "",
    "thumbnail": {
        "path": "http://i.annihil.us/u/prod/marvel/i/mg/c/e0/535fecbbb9784",
        "extension": "jpg"
    }
}
```

## Docker (Not Done)

### Use build-image plugin from Spring Boot maven

```
%mvn install -Dspring.profiles.active=test spring-boot:build-image

[INFO]     [creator]     *** Images (7268b8c9f680):
[INFO]     [creator]           docker.io/library/marvel:0.0.1-SNAPSHOT
[INFO] 
[INFO] Successfully built image 'docker.io/library/marvel:0.0.1-SNAPSHOT'
[INFO] 
[INFO] ------------------------------------------------------------------------
[INFO] BUILD SUCCESS
[INFO] ------------------------------------------------------------------------
[INFO] Total time:  04:05 min
[INFO] Finished at: 2021-06-18T17:15:44+08:00
[INFO] ------------------------------------------------------------------------

% docker images
marvel                     0.0.1-SNAPSHOT              7268b8c9f680   41 years ago        276MB

```






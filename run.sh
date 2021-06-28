
export marvel_publicKey=5495505f65bb5841a868600d95ab0f22
export marvel_privateKey=67518060e6ebe0da2ff3ab5817f9b4fa98327e39

mvn clean install
sleep 3
java -jar  target/marvel-0.0.1-SNAPSHOT.jar

unset marvel_publicKey,marvel_privateKey

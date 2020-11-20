hello world 😁

This project cant be private in git.
Travis-ci don't like it 😢

For å lage en Docker Container av Spring Boot applikasjonen din må du lage en Dockerfile

    FROM openjdk:8-jdk-alpine
    VOLUME /tmp
    ARG JAR_FILE
    COPY ${JAR_FILE} app.jar
    ENTRYPOINT ["java","-jar","app.jar"]
    For å bruke Docker til å lage et Container Image kjører dere;

docker build . --tag pgr301 --build-arg JAR_FILE=./build/libs/<artifactname>
Artifactname er filnavnet på JAR filen. Merk at dere må bygge med Maven eller Gradle før dere kjører kommandoen. Hvis dere bygger med Maven er ikke JAR_FILE argumentet build/libs men target/xyz...

For å starte en Container, kan du kjøre

    docker run -p 8080:8080 pgr301:latest
    
Du skal nå kunne kjøre nå applikasjonen din fra nettleser.



(Kilde https://blog.laputa.io/try-influxdb-and-grafana-by-docker-6b4d50c6a446)

Vi kan få ut en influxdb konfigurasjonsfil fra container ved å kjøre kommandoen

docker run --rm influxdb:1.0 influxd config > influxdb.conf
Dette kan være greit dersom vi ønsker å endre noe. Vi vil sende filen tilbake til containeren og overskrive med våre verdier når vi kjører influx

Vi kan starte influx med følgende docker kommando. Legg merke til av vi overstyrer konfigurasjonsfilen

docker run --name influxdb \
  -p 8083:8083 -p 8086:8086 -p 25826:25826/udp \
  -v $PWD/influxdb:/var/lib/influxdb \
  -v $PWD/influxdb.conf:/etc/influxdb/influxdb.conf:ro \
  -v $PWD/types.db:/usr/share/collectd/types.db:ro \
  influxdb:1.0
hvis dere går til http://localhost:8083/ får dere opp et enkelt brukergrensesnitt.

Visualisering av Metrics
Start Grafana med docker

docker run -d -p 3000:3000 --name grafana grafana/grafana:6.5.0
hvis dere går til http://localhost:3000/ får dere opp et enkelt brukergrensesnitt. - I grafana, Konfigurer en datasource og bruk følgende verdi som URL

http://host.docker.internal:8086
Velg database "mydb". Resten av verdiene kan være uendret.

Instrumenter Spring Boot applikasjonen din med MicroMeter
Det er nå på tide å få noe metrics inn i InfluxDB og visualisere med Grafana.
 
I grove trekk kan dette gjøres ved å legge til de riktige avhengighetene til prosjeketet, og la Spring Boot plukke disse opp med autokonfigurasjon. Micrometer rammeverket kommer som en transitiv avhengighet med Spring Boot Actuator. Så, disse to linjene i build.gradle er det som skal til

    <dependency>
      <groupId>io.micrometer</groupId>
       <artifactId>micrometer-registry-influx</artifactId>
       <version>1.5.5</version>
    </dependency>
    
Vi kan etter det legge til Metrics i koden vår;

    @PostMapping(path = "/tx", consumes = "application/json", produces = "application/json")
        public void addMember(@RequestBody Transaction tx) {
            meterRegistry.counter("txcount", "currency", tx.getCurrency()).increment();
        }
    }
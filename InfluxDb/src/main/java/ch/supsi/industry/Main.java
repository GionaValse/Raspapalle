package ch.supsi.industry;

import com.influxdb.client.InfluxDBClient;
import com.influxdb.client.InfluxDBClientFactory;
import com.influxdb.client.WriteApiBlocking;
import com.influxdb.client.domain.WritePrecision;
import com.influxdb.client.write.Point;

import java.time.Instant;
import java.util.Random;

//TIP To <b>Run</b> code, press <shortcut actionId="Run"/> or
// click the <icon src="AllIcons.Actions.Execute"/> icon in the gutter.
public class Main {

    public static void main(String[] args) throws InterruptedException {
    // You can generate an API token from the "API Tokens Tab" in the UI
        String token = "H0otoTWobMDnOwTVYF9tAaqofciwwjlpejUkbCCK1wU6DQUJbj76MhckXitA5xM-krOdJxPwRjFkO76n85QP4A==";
        String bucket = "supsi";
        String org = "Supisi";

        InfluxDBClient client = InfluxDBClientFactory.create("http://localhost:8086", token.toCharArray());

        for (int i = 0; i < 100; i++) {
            Point point = Point
                    .measurement("piscina")
                    .addTag("tipo_rilevazione", new Random().nextBoolean() ? "Manuale" : "Automatica")
                    .addField("temperatura_in_vasca", new Random().nextFloat(18, 28))
                    .addField("percentuale_cloro", new Random().nextInt(5, 15))
                    .time(Instant.now(), WritePrecision.NS);

            WriteApiBlocking writeApi = client.getWriteApiBlocking();
            writeApi.writePoint(bucket, org, point);
            Thread.sleep(200);
        }
    }
}

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

/*
* Query mean:
* from(bucket: "supsi")
*  |> range(start: -7d)
*  |> filter(fn: (r) => r["_measurement"] == "piscina")
*  |> filter(fn: (r) => r["_field"] == "percentuale_cloro")
*  |> filter(fn: (r) => r["tipo_rilevazione"] == "Automatica" or r["tipo_rilevazione"] == "Manuale")
*  |> aggregateWindow(every: v.windowPeriod, fn: mean, createEmpty: false)
*  |> yield(name: "mean")
*
* Pivioted:
* from(bucket: "supsi")
*  |> range(start: -7d)
*  |> filter(fn: (r) => r["_measurement"] == "piscina")
*  |> filter(fn: (r) => r["_field"] == "percentuale_cloro")
*  |> filter(fn: (r) => r["tipo_rilevazione"] == "Automatica" or r["tipo_rilevazione"] == "Manuale")
*  |> pivot(rowKey: ["_time"], columnKey: ["_field"], valueColumn: "_value")
*  |> yield(name: "pivoted")
* 
* Sum:
* from(bucket: "supsi")
* |> range(start: -70y)
* |> filter(fn: (r) => r["_measurement"] == "piscina")
* |> filter(fn: (r) => r["_field"] == "percentuale_cloro")
* |> yield(name: "sum")
* 
* Standard deviation:
* from(bucket: "supsi")
*  |> range(start: -7d)
*  |> filter(fn: (r) => r["_measurement"] == "piscina")
*  |> filter(fn: (r) => r["_field"] == "percentuale_cloro")
*  |> filter(fn: (r) => r["tipo_rilevazione"] == "Automatica" or r["tipo_rilevazione"] == "Manuale")
*  |> aggregateWindow(every: 5s, fn: stddev, createEmpty: false)
*  |> yield(name: "stddev")
* */


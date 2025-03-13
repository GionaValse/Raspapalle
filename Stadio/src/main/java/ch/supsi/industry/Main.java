package ch.supsi.industry;

import org.iot.raspberry.grovepi.GrovePi;
import org.iot.raspberry.grovepi.pi4j.GrovePi4J;
import org.iot.raspberry.grovepi.sensors.analog.GroveRotarySensor;
import org.iot.raspberry.grovepi.sensors.digital.GroveUltrasonicRanger;
import org.iot.raspberry.grovepi.sensors.i2c.GroveRgbLcd;

import java.util.logging.Level;
import java.util.logging.Logger;

public class Main {

    private static final double EXIT_OFFSET = 50;
    private static final double ENTER_OFFSET = 250;
    private static final double ULTRASONIC_OFFSET = 20;

    public static void main(String[] args) throws Exception {
        Logger.getLogger("GrovePi").setLevel(Level.WARNING);
        Logger.getLogger("RaspberryPi").setLevel(Level.WARNING);

        GrovePi grovePi = new GrovePi4J();
        Status status = Status.INITIAL;

        GroveRotarySensor rotarySensor = new GroveRotarySensor(grovePi, 1);
        GroveUltrasonicRanger ultrasonicRanger = new GroveUltrasonicRanger(grovePi, 6);
        GroveRgbLcd rgbLcd = grovePi.getLCD();

        int people = 0;

        while (true) {
            double rotationValue = rotarySensor.get().getDegrees();

            switch (status) {
                case INITIAL:
                    rgbLcd.setRGB(0, 0, 255);

                    if (rotationValue >= ENTER_OFFSET) {
                        status = Status.WAITING_ENTER;
                    } else if (rotationValue <= EXIT_OFFSET) {
                        status = Status.WAITING_EXIT;
                    }
                    break;

                case WAITING_ENTER:
                    double enterDistance = ultrasonicRanger.get();
                    rgbLcd.setRGB(0, 100, 0);

                    if (rotationValue < ENTER_OFFSET) {
                        status = Status.INITIAL;
                        continue;
                    }

                    if (enterDistance <= ULTRASONIC_OFFSET) {
                        people++;
                        rgbLcd.setRGB(0, 255, 0);
                        rgbLcd.setText("Persona entrata\nP. dentro: " + people);
                        status = Status.COMPLETED;
                    }
                    break;

                case WAITING_EXIT:
                    double exitDistance = ultrasonicRanger.get();
                    rgbLcd.setRGB(100, 0, 0);

                    if (rotationValue > EXIT_OFFSET) {
                        status = Status.INITIAL;
                        continue;
                    }

                    if (exitDistance <= ULTRASONIC_OFFSET) {
                        people--;

                        if (people < 0) {
                            people = 0;
                        }

                        rgbLcd.setRGB(255, 0, 0);
                        rgbLcd.setText("Persona uscita\nP. dentro: " + people);
                        status = Status.COMPLETED;
                    }
                    break;

                case COMPLETED:
                    if (rotationValue < ENTER_OFFSET && rotationValue > EXIT_OFFSET) {
                        rgbLcd.setRGB(0, 0, 255);
                        rgbLcd.setText("\nP. dentro: " + people);
                        System.out.println("Persone dentro: "  + people);
                        status = Status.INITIAL;
                    }
                    break;
            }

            Thread.sleep(500);
        }
    }
}
package org.kendar.mqtt;

import io.netty.handler.codec.mqtt.MqttQoS;
import org.eclipse.paho.client.mqttv3.MqttClient;
import org.eclipse.paho.client.mqttv3.MqttConnectOptions;
import org.eclipse.paho.client.mqttv3.MqttException;
import org.eclipse.paho.client.mqttv3.MqttMessage;
import org.junit.jupiter.api.*;
import org.kendar.utils.Sleeper;

import java.io.IOException;
import java.util.UUID;

import static java.nio.charset.StandardCharsets.UTF_8;
import static org.junit.Assert.assertEquals;

public class SimpleTest extends BasicTest{

    public static final String MESSAGE_CONTENT = "Hello World!!";
    public static final String TOPIC_NAME = "/exit/";

    @BeforeAll
    public static void beforeClass() throws IOException {
        beforeClassBase();

    }

    @AfterAll
    public static void afterClass() throws Exception {
        try {
            afterClassBase();
        } catch (Exception ex) {

        }
    }

    @BeforeEach
    public void beforeEach(TestInfo testInfo) {
        beforeEachBase(testInfo);
    }

    @AfterEach
    public void afterEach() {
        afterEachBase();
    }

    @Test
    void qos2Test() throws MqttException {
        String publisherId = UUID.randomUUID().toString();
        var publisher = new MqttClient("tcp://localhost:1884",publisherId);

        var options = new MqttConnectOptions();
        options.setAutomaticReconnect(true);
        options.setCleanSession(true);
        options.setConnectionTimeout(10);
        publisher.connect(options);

        var message = new MqttMessage(MESSAGE_CONTENT.getBytes(UTF_8));
        //message.setQos(2);
        message.setQos(2);
        message.setRetained(true);
        publisher.publish(TOPIC_NAME,message);
        Sleeper.sleep(1000);
        publisher.disconnect();
        assertEquals(1,moquetteMessages.size());
        var founded = moquetteMessages.get(0);
        assertEquals(MqttQoS.EXACTLY_ONCE,founded.getQos());
        assertEquals(MESSAGE_CONTENT,founded.getPayload().toString(UTF_8));
    }
}

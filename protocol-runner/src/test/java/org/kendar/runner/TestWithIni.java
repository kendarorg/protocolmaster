package org.kendar.runner;

import org.eclipse.paho.client.mqttv3.*;
import org.kendar.Main;
import org.kendar.jpa.HibernateSessionFactory;
import org.kendar.utils.Sleeper;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.concurrent.atomic.AtomicReference;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertTrue;


public class TestWithIni {
    public static final String MESSAGE_CONTENT = "Hello World!!";
    public static final String TOPIC_NAME = "/subscribe/";
    private static final List<MqttMessage> messages = new ArrayList<>();
    //@Test
    void testLoadingFromIniFile() throws Exception {
        var args = new String[]{
                "-cfg", "test.ini"
        };
        startAndHandleUnexpectedErrors(args);
        Sleeper.sleep(6000);

        qos2Test();
        jpaTest();
        Sleeper.sleep(6000);
    }



    private void startAndHandleUnexpectedErrors(String[] args) {
        AtomicReference exception =new AtomicReference(null);
        var serverThread = new Thread(() -> {
            Main.execute(args, () -> {
                try {
                    Sleeper.sleep(100);
                    return true;
                }catch (Exception e){
                    exception.set(e);
                    return false;
                }
            });
            exception.set(new Exception("Terminated abruptly"));
        });
        serverThread.start();
        while (!Main.isRunning()) {
            if(exception.get()!=null){
                throw new RuntimeException((Throwable) exception.get());
            }
            Sleeper.sleep(100);
        }
    }

    private static void setupCallBack(MqttClient client) {
        messages.clear();
        client.setCallback(new MqttCallback() {
            public void messageArrived(String topic, MqttMessage message) throws Exception {
                System.out.println("topic: " + topic);
                System.out.println("qos: " + message.getQos());
                System.out.println("message content: " + new String(message.getPayload()));
                messages.add(message);
            }

            public void connectionLost(Throwable cause) {
                System.out.println("connectionLost: " + cause.getMessage());
            }

            public void deliveryComplete(IMqttDeliveryToken token) {
                System.out.println("deliveryComplete: " + token.isComplete());
            }
        });
    }

    protected static final int FAKE_PORT = 5432;

    void jpaTest() throws Exception {
        HibernateSessionFactory.initialize("org.postgresql.Driver",
                //postgresContainer.getJdbcUrl(),
                String.format("jdbc:postgresql://localhost:%d/test?ssl=false", FAKE_PORT),
                "user","login",
                "org.hibernate.dialect.PostgreSQLDialect",
                CompanyJpa.class);


        HibernateSessionFactory.transactional(em -> {
            var lt = new CompanyJpa();
            lt.setDenomination("Test Ltd");
            lt.setAddress("TEST RD");
            lt.setAge(22);
            lt.setSalary(500.22);
            em.persist(lt);
        });
        var atomicBoolean = new AtomicBoolean(false);
        HibernateSessionFactory.query(em -> {
            var resultset = em.createQuery("SELECT denomination FROM CompanyJpa").getResultList();
            for (var rss : resultset) {
                assertEquals("Test Ltd", rss);
                atomicBoolean.set(true);
            }
        });

        assertTrue(atomicBoolean.get());
    }

    void qos2Test() throws MqttException {
        messages.clear();

        //var protocolServer = new TcpServer(baseProtocol);

        try {
            //protocolServer.start();

            //Sleeper.sleep(5000, protocolServer::isRunning);

            String publisherId = UUID.randomUUID().toString();
            var client = new MqttClient("tcp://localhost:1885", publisherId);

            MqttConnectOptions options = new MqttConnectOptions();
            client.connect(options);

            if (client.isConnected()) {
                setupCallBack(client);

                client.subscribe(TOPIC_NAME, 2);

                MqttMessage message = new MqttMessage(MESSAGE_CONTENT.getBytes());
                message.setQos(2);
                client.publish(TOPIC_NAME, message);
            }
            Sleeper.sleep(2000, () -> !messages.isEmpty());
            client.disconnect();
            client.close();
            assertEquals(1, messages.size());
            var mesg = messages.get(0);
            assertEquals(MESSAGE_CONTENT, new String(mesg.getPayload()));
            assertEquals(2, mesg.getQos());
        } finally {
            //protocolServer.stop();
        }

    }
}

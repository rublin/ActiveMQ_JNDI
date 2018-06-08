
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Properties;

public class Main {
    private static final Logger LOG = LoggerFactory.getLogger(Main.class);

    /**
     * @param args the destination name to send to and optionally, the number of * messages to send
     */
    public static void main(String[] args) {

        Context jndiContext = null;
        ConnectionFactory connectionFactory = null;
        Connection connection = null;
        Destination destination = null;
        Session session;
        MessageProducer producer;
        final int numMsgs = 1;

        if ((args.length != 2)) {
            LOG.info("Usage: java SimpleProducer <url> [<queue name>]");
            System.exit(1);
        }

        LOG.info("ActiveMQ URL is " + args[0]);
        LOG.info("Queue name is " + args[1]);

        Properties props = new Properties();
        props.setProperty(Context.INITIAL_CONTEXT_FACTORY,"org.apache.activemq.jndi.ActiveMQInitialContextFactory");
        props.setProperty(Context.PROVIDER_URL, args[0]);
        props.setProperty("queue.queue/simple", args[1]);

        /* * Create a JNDI API InitialContext object */
        try {
            jndiContext = new InitialContext(props);
        } catch (NamingException e) {
            LOG.error("Could not create JNDI API context: ", e);
            System.exit(1);
        }

        /* * Look up connection factory and destination. */
        try {
            connectionFactory = (ConnectionFactory) jndiContext.lookup("ConnectionFactory");
            destination = (Destination) jndiContext.lookup("queue/simple");
        } catch (NamingException e) {
            LOG.error("JNDI API lookup failed: ", e);
            System.exit(1);
        }

        /* * Create connection. Create session from connection; false means * session is not transacted. Create sender and text message. Send * messages, varying text slightly. Send end-of-messages message. * Finally, close the connection. */
        try {
            connection = connectionFactory.createConnection();
            session = connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
            producer = session.createProducer(destination);
            TextMessage message = session.createTextMessage();
            for (int i = 0; i < numMsgs; i++) {
                message.setText("This is message " + (i + 1));
                LOG.info("Sending message: {}", message.getText());
                producer.send(message);
            }

            /* * Send a non-text control message indicating end of messages. */
            producer.send(session.createMessage());
        } catch (JMSException e) {
            LOG.info("Exception occurred: ", e);
        } finally {
            if (connection != null) {
                try {
                    connection.close();
                } catch (JMSException ignored) {
                }
            }
        }
    }
}

package com.matrix.common.mq;

import java.net.URL;
import java.util.Hashtable;

import javax.jms.BytesMessage;
import javax.jms.Connection;
import javax.jms.ConnectionFactory;
import javax.jms.JMSException;
import javax.jms.Message;
import javax.jms.MessageConsumer;
import javax.jms.MessageProducer;
import javax.jms.Queue;
import javax.jms.Session;
import javax.jms.TextMessage;
import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NamingException;

import org.apache.log4j.Logger;

import rapture.common.impl.jackson.JacksonUtil;
import rapture.kernel.ContextFactory;
import rapture.kernel.Kernel;

/**
 * Provides JNDI access to the defined Websphere JMS objects in the .bindings file
 * 
 * @author dukenguyen
 *
 */
public enum MqManager {

    INSTANCE;

    private static final Logger log = Logger.getLogger(MqManager.class);

    private static InitialContext ctx;

    public static final String ENV_KEY = "environment";
    public static final String CF_KEY = "connectionFactory";
    public static final String OUTPUT_Q_KEY = "outputQueue";
    public static final String WRITE_ENABLED_KEY = "writeEnabled";

    static {
        String mqcfg = Kernel.getDoc().getDoc(ContextFactory.getKernelUser(), "document://configs/matrix/tradeweb/mqSeriesConfig");
        INSTANCE.init((String) JacksonUtil.getMapFromJson(mqcfg).get(ENV_KEY));
    }

    public void init(String env) {
        Hashtable<String, String> ctxmap = new Hashtable<>();
        ctxmap.put(Context.INITIAL_CONTEXT_FACTORY, "com.sun.jndi.fscontext.RefFSContextFactory");
        ctxmap.put(Context.PROVIDER_URL, findPathToBindingsFile(env));
        try {
            ctx = new InitialContext(ctxmap);
        } catch (NamingException e) {
            log.error("Error initializing InitialContext from .bindings file", e);
        }
    }

    public static MqManager getInstance() {
        return INSTANCE;
    }

    public ConnectionFactory getConnectionFactory(String connectionFactory) {
        try {
            return (ConnectionFactory) ctx.lookup(connectionFactory);
        } catch (NamingException e) {
            log.error(String.format("Error looking up connectionFactory [%s]", connectionFactory), e);
        }
        return null;
    }

    public Queue getQueue(String queue) {
        try {
            return (Queue) ctx.lookup(queue);
        } catch (NamingException e) {
            log.error(String.format("Error looking up queue [%s]", queue), e);
        }
        return null;
    }

    public String readFromQueue(String connectionFactory, String queue) throws MqManagerException {
        try {
            Connection c = getConnectionFactory(connectionFactory).createConnection();
            Session session = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageConsumer consumer = session.createConsumer(getQueue(queue));
            Message msg = consumer.receiveNoWait();
            if (msg instanceof TextMessage) {
                TextMessage tm = (TextMessage) msg;
                return tm.getText();
            } else if (msg instanceof BytesMessage) {
                BytesMessage bm = (BytesMessage) msg;
                return bm.readUTF();
            }
        } catch (JMSException e) {
            log.error(String.format("Could not read from queue [%s]", queue), e);
            throw new MqManagerException(e);
        }
        return null;
    }

    public void writeToQueue(String connectionFactory, String queue, String msg) throws MqManagerException {
        try {
            Connection c = getConnectionFactory(connectionFactory).createConnection();
            Session session = c.createSession(false, Session.AUTO_ACKNOWLEDGE);
            MessageProducer producer = session.createProducer(getQueue(queue));
            Message m = session.createTextMessage(msg);
            producer.send(m);
        } catch (JMSException e) {
            log.error(String.format("Could not write to queue [%s]", queue), e);
            throw new MqManagerException(e);
        }
    }

    /**
     * Find the .bindings IBM MQ specific JNDI bindings file in the CLASSPATH and return the path up to the file
     * 
     * @param env
     *            - Folder to use as part of the classpath for different versions of the bindings file based on environment
     * @return
     */
    private String findPathToBindingsFile(String env) {
        URL fileUrl = this.getClass().getResource(String.format("/mq/%s/.bindings", env));
        if (fileUrl != null) {
            String path = fileUrl.toString();
            path = path.substring(0, path.lastIndexOf('/'));
            log.info("Using the following path for the JNDI .bindings file: " + path);
            return path;
        }
        throw new RuntimeException(String.format("Could not find [.bindings] file for environment [%s] anywhere in the CLASSPATH", env));
    }

}

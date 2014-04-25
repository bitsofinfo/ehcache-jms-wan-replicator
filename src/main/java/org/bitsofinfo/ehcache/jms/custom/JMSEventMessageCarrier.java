package org.bitsofinfo.ehcache.jms.custom;

import java.io.Serializable;
import java.util.Enumeration;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.ObjectMessage;

import net.sf.ehcache.distribution.jms.JMSEventMessage;

/**
 * Dummy carrier object for the BatchingJMSCachePeer onMessage()
 * implementation
 * 
 * @author bitsofinfo.g[at]g mail com
 *
 */
public class JMSEventMessageCarrier implements ObjectMessage {

	private JMSEventMessage jmsEventMessage = null;
	
	public JMSEventMessageCarrier(JMSEventMessage msg) {
		this.jmsEventMessage = msg;
	}

	public Serializable getObject() throws JMSException {
		return jmsEventMessage;
	}
	
	
	
	
	
	
	public void acknowledge() throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void clearBody() throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void clearProperties() throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public boolean getBooleanProperty(String arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public byte getByteProperty(String arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public double getDoubleProperty(String arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public float getFloatProperty(String arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public int getIntProperty(String arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public String getJMSCorrelationID() throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public byte[] getJMSCorrelationIDAsBytes() throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public int getJMSDeliveryMode() throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public Destination getJMSDestination() throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public long getJMSExpiration() throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public String getJMSMessageID() throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public int getJMSPriority() throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public boolean getJMSRedelivered() throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public Destination getJMSReplyTo() throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public long getJMSTimestamp() throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public String getJMSType() throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public long getLongProperty(String arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public Object getObjectProperty(String arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public Enumeration getPropertyNames() throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public short getShortProperty(String arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public String getStringProperty(String arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public boolean propertyExists(String arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");
	}

	public void setBooleanProperty(String arg0, boolean arg1)
			throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setByteProperty(String arg0, byte arg1) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setDoubleProperty(String arg0, double arg1) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setFloatProperty(String arg0, float arg1) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setIntProperty(String arg0, int arg1) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setJMSCorrelationID(String arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setJMSCorrelationIDAsBytes(byte[] arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setJMSDeliveryMode(int arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setJMSDestination(Destination arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setJMSExpiration(long arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setJMSMessageID(String arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setJMSPriority(int arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setJMSRedelivered(boolean arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setJMSReplyTo(Destination arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setJMSTimestamp(long arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setJMSType(String arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setLongProperty(String arg0, long arg1) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setObjectProperty(String arg0, Object arg1) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setShortProperty(String arg0, short arg1) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setStringProperty(String arg0, String arg1) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

	public void setObject(Serializable arg0) throws JMSException {
		throw new UnsupportedOperationException("If this was thrown, something changed in the onMessage() impl of JMSCachePeer in the Ehcache JMS Replication project");

	}

}

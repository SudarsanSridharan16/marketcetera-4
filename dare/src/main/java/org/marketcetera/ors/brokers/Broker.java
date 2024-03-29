package org.marketcetera.ors.brokers;

import java.util.HashMap;
import java.util.Map;

import org.marketcetera.client.brokers.BrokerStatus;
import org.marketcetera.ors.filters.MessageModifierManager;
import org.marketcetera.ors.filters.MessageRouteManager;
import org.marketcetera.quickfix.FIXDataDictionary;
import org.marketcetera.quickfix.FIXMessageFactory;
import org.marketcetera.quickfix.FIXMessageUtil;
import org.marketcetera.quickfix.FIXVersion;
import org.marketcetera.quickfix.messagefactory.FIXMessageAugmentor;
import org.marketcetera.trade.BrokerID;
import org.marketcetera.util.log.SLF4JLoggerProxy;
import org.marketcetera.util.misc.ClassVersion;
import org.marketcetera.util.quickfix.AnalyzedMessage;

import quickfix.DataDictionary;
import quickfix.Message;
import quickfix.Session;
import quickfix.SessionID;


/**
 * The in-memory representation of a single broker.
 *
 * @author tlerios@marketcetera.com
 * @since 1.0.0
 * @version $Id: Broker.java 17266 2017-04-28 14:58:00Z colin $
 */

/* $License$ */

@ClassVersion("$Id: Broker.java 17266 2017-04-28 14:58:00Z colin $")
public class Broker
{

    // CLASS DATA

    private static final String HEARTBEAT_CATEGORY=
        Broker.class.getName()+".HEARTBEATS"; //$NON-NLS-1$


    // INSTANCE DATA.

    private final SpringBroker mSpringBroker;
    private final BrokerID mBrokerID;
    private final BrokerID mappedBrokerId;
    private FIXDataDictionary mDataDictionary;
    private boolean mLoggedOn;


    // CONSTRUCTORS.

    /**
     * Creates a new broker based on the given configuration. Its
     * message modifiers are configured to rely on the given report
     * history services provider for persistence operations.
     *
     * @param springBroker The configuration.
     * @param historyServices The report history services provider.
     */

    public Broker(SpringBroker springBroker)
    {
        mSpringBroker=springBroker;
        mBrokerID=new BrokerID(getSpringBroker().getId());
        mappedBrokerId = springBroker.getMappedBrokerId()==null?null:new BrokerID(springBroker.getMappedBrokerId());
    }


    // INSTANCE METHODS.

    /**
     * Returns the receiver's configuration.
     *
     * @return The configuration.
     */

    public SpringBroker getSpringBroker()
    {
        return mSpringBroker;
    }
    /**
     * Gets the broker status value.
     *
     * @return a <code>BrokerStatus</code> value
     */
    public BrokerStatus getStatus()
    {
        // hostname
        Map<String,String> settings = new HashMap<>();
        if(getSpringBroker() != null && getSpringBroker().getDescriptor() != null && getSpringBroker().getDescriptor().getSettings() != null && 
           getSpringBroker().getDescriptor().getSettings().getDefaults() != null) {
            for(Map.Entry<Object,Object> defaultEntry : getSpringBroker().getDescriptor().getSettings().getDefaults().entrySet()) {
                settings.put(String.valueOf(defaultEntry.getKey()),
                             String.valueOf(defaultEntry.getValue()));
            }
        }
        if(getSpringBroker() != null && getSpringBroker().getDescriptor() != null && getSpringBroker().getDescriptor().getDictionary() != null) {
            settings.putAll(getSpringBroker().getDescriptor().getDictionary());
        }
        return new BrokerStatus(getName(),
                                getBrokerID(),
                                getLoggedOn(),
                                settings,
                                mSpringBroker.getBrokerAlgoSpecs());
    }
    /**
     * Returns the receiver's name.
     *
     * @return The name.
     */

    public String getName()
    {
        return getSpringBroker().getName();
    }

    /**
     * Returns the receiver's broker ID.
     *
     * @return The ID.
     */

    public BrokerID getBrokerID()
    {
        return mBrokerID;
    }
    /**
     * Get the mappedBrokerId value.
     *
     * @return a <code>BrokerID</code> value or <code>null</code>
     */
    public BrokerID getMappedBrokerId()
    {
        return mappedBrokerId;
    }
    /**
     * Returns the receiver's QuickFIX/J session ID.
     *
     * @return The ID.
     */

    public SessionID getSessionID()
    {
        return getSpringBroker().getDescriptor().getQSessionID();
    }

    /**
     * Returns the receiver's QuickFIX/J session.
     *
     * @return The session.
     */

    public Session getSession()
    {
        return Session.lookupSession(getSessionID());
    }

    /**
     * Returns the receiver's QuickFIX/J data dictionary.
     *
     * @return The dictionary.
     */

    public DataDictionary getDataDictionary()
    {
        return FIXMessageUtil.getDataDictionary(getFIXVersion());
    }

    /**
     * Returns the receiver's message modifier manager.
     *
     * @return The manager. It may be null.
     */

    public MessageModifierManager getModifiers()
    {
        return getSpringBroker().getModifiers();
    }

    /**
     * Returns the receiver's route manager.
     *
     * @return The manager. It may be null.
     */

    public MessageRouteManager getRoutes()
    {
        return getSpringBroker().getRoutes();
    }

    /**
     * Returns the receiver's pre-sending message modifier manager.
     *
     * @return The manager. It may be null.
     */

    public MessageModifierManager getPreSendModifiers()
    {
        return getSpringBroker().getPreSendModifiers();
    }

    /**
     * Returns the receiver's response message modifier manager.
     *
     * @return The manager. It may be null.
     */

    public MessageModifierManager getResponseModifiers()
    {
        return getSpringBroker().getResponseModifiers();
    }

    /**
     * Returns the receiver's FIX version.
     *
     * @return The version.
     */

    public FIXVersion getFIXVersion()
    {
        return FIXVersion.getFIXVersion(getSessionID());
    }

    /**
     * Returns the receiver's FIX message factory.
     *
     * @return The factory.
     */

    public FIXMessageFactory getFIXMessageFactory()
    {
        return getFIXVersion().getMessageFactory();
    }

    /**
     * Returns the receiver's FIX data dictionary.
     *
     * @return The dictionary.
     */

    public synchronized FIXDataDictionary getFIXDataDictionary()
    {
        if (mDataDictionary==null) {
            mDataDictionary=new FIXDataDictionary(getDataDictionary());
        }
        return mDataDictionary;
    }

    /**
     * Returns the receiver's FIX message augmentor.
     *
     * @return The augmentor.
     */

    public FIXMessageAugmentor getFIXMessageAugmentor()
    {
        return getFIXMessageFactory().getMsgAugmentor();
    }

    /**
     * Sets the receiver's logon flag to the given value. This method
     * is synchronized to ensure that all threads will see the most
     * up-to-date value for the flag.
     *
     * @param loggedOn The flag.
     */

    public synchronized void setLoggedOn
        (boolean loggedOn)
    {
        mLoggedOn=loggedOn;
    }

    /**
     * Returns the receiver's logon flag. This method is synchronized
     * to ensure that all threads will see the most up-to-date value
     * for the flag.
     *
     * @return The flag.
     */

    public synchronized boolean getLoggedOn()
    {
        return mLoggedOn;
    }

    /**
     * Logs the given message, analyzed using the receiver's data
     * dictionary, at the debugging level.
     *
     * @param msg The message.
     */

    public void logMessage
        (Message msg)
    {
        Object category=(FIXMessageUtil.isHeartbeat(msg)?
                         HEARTBEAT_CATEGORY:this);
        if (SLF4JLoggerProxy.isDebugEnabled(category)) {
            Messages.ANALYZED_MESSAGE.debug
                (category,
                 new AnalyzedMessage(getDataDictionary(),msg).toString());
        }        
    }


    // Object.

    public String toString()
    {
        return Messages.BROKER_STRING.getText
            (getBrokerID().getValue(),getName(),getSessionID());
    }
}

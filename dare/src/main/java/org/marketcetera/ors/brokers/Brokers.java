package org.marketcetera.ors.brokers;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;

import org.marketcetera.client.brokers.BrokerStatus;
import org.marketcetera.client.brokers.BrokersStatus;
import org.marketcetera.trade.BrokerID;
import org.marketcetera.util.misc.ClassVersion;
import org.marketcetera.util.quickfix.SpringSessionSettings;

import quickfix.SessionID;

/**
 * The collective in-memory representation of all brokers.
 *
 * @author tlerios@marketcetera.com
 * @since 1.0.0
 * @version $Id: Brokers.java 17266 2017-04-28 14:58:00Z colin $
 */

/* $License$ */

@ClassVersion("$Id: Brokers.java 17266 2017-04-28 14:58:00Z colin $")
public class Brokers
        implements Iterable<Broker>
{

    // INSTANCE DATA.

    private final SpringBrokers mSpringBrokers;
    private final List<Broker> mBrokers;
    private final Map<BrokerID,Broker> mBrokerIDMap;
    private final Map<SessionID,Broker> mSessionIDMap;


    // CONSTRUCTORS.

    /**
     * Creates a new collective representation based on the given
     * broker configurations. Any message modifiers are configured to
     * rely on the given report history services provider for
     * persistence operations.
     *
     * @param springBrokers The configurations.
     * @param historyServices The report history services provider.
     */

    public Brokers(SpringBrokers springBrokers)
    {
        mSpringBrokers=springBrokers;
        int capacity=getSpringBrokers().getBrokers().size();
        mBrokers=new ArrayList<Broker>(capacity);
        mBrokerIDMap=new HashMap<BrokerID,Broker>(capacity);
        mSessionIDMap=new HashMap<SessionID,Broker>(capacity);
        for(SpringBroker sb:getSpringBrokers().getBrokers()) {
            Broker b=new Broker(sb);
            mBrokers.add(b);
            mBrokerIDMap.put(b.getBrokerID(),b);
            if(b.getMappedBrokerId() == null) {
                mSessionIDMap.put(b.getSessionID(),b);
            }
        }
    }
    /* (non-Javadoc)
     * @see java.lang.Object#toString()
     */
    @Override
    public String toString()
    {
        return String.valueOf(mBrokers);
    }
    /* (non-Javadoc)
     * @see java.lang.Iterable#iterator()
     */
    @Override
    public Iterator<Broker> iterator()
    {
        return mBrokers.iterator();
    }
    /**
     * Returns the receiver's broker configurations.
     *
     * @return The configurations.
     */

    public SpringBrokers getSpringBrokers()
    {
        return mSpringBrokers;
    }

    /**
     * Returns the receiver's brokers.
     *
     * @return The brokers.
     */

    public List<Broker> getBrokers()
    {
        return mBrokers;
    }

    /**
     * Returns the status of the receiver's brokers.
     *
     * @return The status.
     */

    public BrokersStatus getStatus(String inUsername)
    {
        List<BrokerStatus> list = new ArrayList<BrokerStatus>();
        for(Broker b : getBrokers()) {
            if(b.getSpringBroker().isUserAllowed(inUsername)) {
                list.add(b.getStatus());
            }
        }
        return new BrokersStatus(list);
    }

    /**
     * Returns the configuration of the receiver's QuickFIX/J session
     * settings.
     *
     * @return The configuration.
     */

    public SpringSessionSettings getSettings()
    {
        return getSpringBrokers().getSettings();
    }

    /**
     * Returns the receiver's broker for the given QuickFIX/J session
     * ID. It logs an error and returns null if there is no broker for
     * the given ID.
     *
     * @param sessionID The ID.
     *
     * @return The broker. It may be null.
     */

    public Broker getBroker
        (SessionID sessionID)
    {
        Broker b=mSessionIDMap.get(sessionID);
        if (b==null) {
            Messages.INVALID_SESSION_ID.debug(this,sessionID);
        }
        return b;
    }

    /**
     * Returns the receiver's broker for the given broker ID. It logs
     * an error and returns null if there is no broker for the given
     * ID.
     *
     * @param brokerID The ID.
     *
     * @return The broker. It may be null.
     */

    public Broker getBroker
        (BrokerID brokerID)
    {
        Broker b=mBrokerIDMap.get(brokerID);
        return b;
    }
}

package org.marketcetera.ors.history;

import java.math.BigDecimal;
import java.util.Date;
import java.util.List;
import java.util.Map;

import org.marketcetera.client.jms.JmsManager;
import org.marketcetera.core.IDFactory;
import org.marketcetera.core.position.PositionKey;
import org.marketcetera.ors.security.SimpleUser;
import org.marketcetera.persist.PersistenceException;
import org.marketcetera.trade.Currency;
import org.marketcetera.trade.Equity;
import org.marketcetera.trade.Future;
import org.marketcetera.trade.Option;
import org.marketcetera.trade.OrderID;
import org.marketcetera.trade.ReportBase;
import org.marketcetera.trade.ReportBaseImpl;
import org.marketcetera.util.misc.ClassVersion;


/* $License$ */
/**
 * Provides services to save and query reports.
 *
 * @author anshul@marketcetera.com
 * @version $Id: ReportHistoryServices.java 17266 2017-04-28 14:58:00Z colin $
 * @since 1.0.0
 */
@ClassVersion("$Id: ReportHistoryServices.java 17266 2017-04-28 14:58:00Z colin $")
public interface ReportHistoryServices {

    /**
     * Initializes the receiver with the given system resources.
     *
     * @param idFactory The ID factory to be used for report ID
     * generation.
     * @param jmsManager The JMS manager used for asychronous
     * persistence of reports. Null may be acceptable to certain
     * implementations.
     * @param reportSavedListener The listener notified after a report
     * has been saved (successfully or not). It may be null if no
     * notifications are needed.
     */
    public void init(IDFactory idFactory,
                     JmsManager jmsManager,
                     ReportSavedListener reportSavedListener);

    /**
     * Returns all the reports received after the supplied date-time
     * value, and which are visible to the given user.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the date-time value. Cannot be null.
     *
     * @return the reports that were received after the date-time
     * value, and which are visible to the given user.
     *
     * @throws PersistenceException if there were persistence errors
     * fetching the reports.
     * @throws ReportPersistenceException if the data retrieved had
     * unexpected errors.
     */
    public ReportBaseImpl[] getReportsSince
        (SimpleUser inUser,
         Date inDate)
        throws PersistenceException, ReportPersistenceException;

    /**
     * Returns the position of the equity based on all reports
     * received for it before or on the supplied date, and which are visible
     * to the given user.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the date to compare with all the reports. Only the reports
     * that were received prior to or on this date will be used in this calculation.
     * Cannot be null.
     *
     * @param inEquity the equity whose position is desired. Cannot be null.
     *
     * @return the equity position.
     *
     * @throws PersistenceException if there were errors retrieving the equity
     * position
     */
    public BigDecimal getEquityPositionAsOf
        (SimpleUser inUser,
         Date inDate,
         Equity inEquity)
        throws PersistenceException;
    
    /**
     * Returns the position of the currency based on all reports
     * received for it before or on the supplied date, and which are visible
     * to the given user.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the date to compare with all the reports. Only the reports
     * that were received prior to or on this date will be used in this calculation.
     * Cannot be null.
     *
     * @param inCurrency the currency whose position is desired. Cannot be null.
     *
     * @return the currency position.
     *
     * @throws PersistenceException if there were errors retrieving the currency
     * position
     */
    public BigDecimal getCurrencyPositionAsOf
        (SimpleUser inUser,
         Date inDate,
         Currency inCurrency)
        throws PersistenceException;

    /**
     * Returns the aggregate position of each (equity,account,actor)
     * tuple based on all reports received for each tuple on or before
     * the supplied date, and which are visible to the given user.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the date to compare with all the reports. Only
     * the reports that were received on or prior to this date will be
     * used in this calculation.  Cannot be null.
     *
     * @return the position map.
     *
     * @throws PersistenceException if there were errors retrieving the
     * position map.
     */
    public Map<PositionKey<Equity>, BigDecimal> getAllEquityPositionsAsOf
        (SimpleUser inUser,
         Date inDate)
        throws PersistenceException;
    
    
    /**
     * Returns the aggregate position of each (currency,account,actor)
     * tuple based on all reports received for each tuple on or before
     * the supplied date, and which are visible to the given user.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the date to compare with all the reports. Only
     * the reports that were received on or prior to this date will be
     * used in this calculation.  Cannot be null.
     *
     * @return the position map.
     *
     * @throws PersistenceException if there were errors retrieving the
     * position map.
     */
    public Map<PositionKey<Currency>, BigDecimal> getAllCurrencyPositionsAsOf
        (SimpleUser inUser,
         Date inDate)
        throws PersistenceException;

    /**
     * Gets the current aggregate position for the option instrument based on
     * execution reports received before or on the supplied date, and which
     * are visible to the given user.
     *
     * <p>
     * Buy trades result in positive positions. All other kinds of trades
     * result in negative positions.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the time. execution reports with sending time values less
     * than or equal to this time are included in this calculation.
     * @param inOption The option instrument
     *
     * @return the aggregate position for the symbol.
     *
     * @throws PersistenceException if there were errors retrieving the
     * position.
     */
    public BigDecimal getOptionPositionAsOf
        (final SimpleUser inUser,
         final Date inDate,
         final Option inOption)
        throws PersistenceException;

    /**
     * Returns the aggregate position of each option
     * (option,account,actor)
     * tuple based on all reports received for each option instrument on or before
     * the supplied date, and which are visible to the given user.
     *
     * <p> Buy trades result in positive positions. All other kinds of
     * trades result in negative positions.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the date to compare with all the reports. Only
     * the reports that were received on or prior to this date will be
     * used in this calculation.  Cannot be null.
     *
     * @return the position map.
     *
     * @throws PersistenceException if there were errors retrieving the
     * position map.
     */
    public Map<PositionKey<Option>, BigDecimal> getAllOptionPositionsAsOf
        (final SimpleUser inUser,
         final Date inDate)
        throws PersistenceException;
    /**
     * Gets the current aggregate position for the future instrument based on
     * execution reports received before or on the supplied date, and which
     * are visible to the given user.
     *
     * <p>
     * Buy trades result in positive positions. All other kinds of trades
     * result in negative positions.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the time. execution reports with sending time values less
     * than or equal to this time are included in this calculation.
     * @param inFuture The future instrument
     *
     * @return the aggregate position for the symbol.
     *
     * @throws PersistenceException if there were errors retrieving the
     * position.
     */
    public BigDecimal getFuturePositionAsOf(final SimpleUser inUser,
                                            final Date inDate,
                                            final Future inFuture)
        throws PersistenceException;
    /**
     * Returns the aggregate position of each future
     * (future,account,actor)
     * tuple based on all reports received for each future instrument on or before
     * the supplied date, and which are visible to the given user.
     *
     * <p> Buy trades result in positive positions. All other kinds of
     * trades result in negative positions.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the date to compare with all the reports. Only
     * the reports that were received on or prior to this date will be
     * used in this calculation.  Cannot be null.
     *
     * @return the position map.
     *
     * @throws PersistenceException if there were errors retrieving the
     * position map.
     */
    public Map<PositionKey<Future>,BigDecimal> getAllFuturePositionsAsOf(final SimpleUser inUser,
                                                                         final Date inDate)
            throws PersistenceException;
    /**
     * Returns the aggregate position of each option
     * (option,account,actor)
     * tuple based on all reports received for each option instrument on or before
     * the supplied date, and which are visible to the given user.
     *
     * <p> Buy trades result in positive positions. All other kinds of
     * trades result in negative positions.
     *
     * @param inUser the user making the query. Cannot be null.
     * @param inDate the date to compare with all the reports. Only
     * the reports that were received on or prior to this date will be
     * used in this calculation.  Cannot be null.
     * @param inSymbols the list of option roots.
     *
     * @return the position map.
     *
     * @throws PersistenceException if there were errors retrieving the
     * position map.
     */
    public Map<PositionKey<Option>, BigDecimal> getOptionPositionsAsOf
        (final SimpleUser inUser,
         final Date inDate,
         final String... inSymbols)
        throws PersistenceException;

    /**
     * Saves the supplied report to the database. Saving may be
     * immediate or delayed; in either case the report ID is set
     * before this method returns.
     *
     * @param report the report to be saved. Cannot be null.
     *
     * @throws org.marketcetera.persist.PersistenceException if there
     * were errors saving the report.
     */
    public void save(ReportBase report)
        throws PersistenceException;
    /**
     * Deletes the supplied report from the database.
     *
     * @param inReport a <code>ReportBase</code> value
     * @throws PersistenceException if there were errors deleting the report
     */
    public void delete(ReportBase inReport)
            throws PersistenceException;
    /**
     * Returns the open orders visible to the given user.
     *
     * @param inUser a <code>SimpleUser</code> value
     * @return a <code>List&lt;ReportBaseImpl</code> value
     * @throws PersistenceException if there were errors retrieving the data
     */
    public List<ReportBaseImpl> getOpenOrders(SimpleUser inUser)
            throws PersistenceException;
    /**
     * Gets the order ID of the root of this order chain.
     *
     * @param inOrderID an <code>OrderID</code> value
     * @return an <code>OrderID</code> value or <code>null</code>
     */
    OrderID getRootOrderIdFor(OrderID inOrderID);
}

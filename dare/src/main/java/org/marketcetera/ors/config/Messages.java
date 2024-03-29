package org.marketcetera.ors.config;

import org.marketcetera.util.log.I18NLoggerProxy;
import org.marketcetera.util.log.I18NMessage0P;
import org.marketcetera.util.log.I18NMessageProvider;
import org.marketcetera.util.misc.ClassVersion;

/**
 * The internationalization constants used by this package.
 *
 * @author tlerios@marketcetera.com
 * @since 1.0.0
 * @version $Id: Messages.java 17266 2017-04-28 14:58:00Z colin $
 */

/* $License$ */

@ClassVersion("$Id: Messages.java 17266 2017-04-28 14:58:00Z colin $")
public interface Messages
{

    /**
     * The message provider.
     */

    static final I18NMessageProvider PROVIDER=
        new I18NMessageProvider("ors_config"); //$NON-NLS-1$

    /**
     * The logger.
     */

    static final I18NLoggerProxy LOGGER=
        new I18NLoggerProxy(PROVIDER);

    /*
     * The messages.
     */

    static final I18NMessage0P NO_BROKERS=
        new I18NMessage0P(LOGGER,"no_brokers"); //$NON-NLS-1$
    static final I18NMessage0P NO_INCOMING_CONNECTION_FACTORY=
        new I18NMessage0P
        (LOGGER,"no_incoming_connection_factory"); //$NON-NLS-1$
    static final I18NMessage0P NO_OUTGOING_CONNECTION_FACTORY=
        new I18NMessage0P
        (LOGGER,"no_outgoing_connection_factory"); //$NON-NLS-1$
    static final I18NMessage0P NO_ID_FACTORY=
        new I18NMessage0P(LOGGER,"no_id_factory"); //$NON-NLS-1$
    static final I18NMessage0P NO_REPORT_HISTORY_SERVICES=
        new I18NMessage0P(LOGGER,"no_report_history_services"); //$NON-NLS-1$
    static final I18NMessage0P NO_ORDER_INFO_CACHE=
        new I18NMessage0P(LOGGER,"no_order_info_cache"); //$NON-NLS-1$
}

package org.marketcetera.ors.info;

import org.marketcetera.util.except.I18NException;
import org.marketcetera.util.log.I18NBoundMessage;
import org.marketcetera.util.misc.ClassVersion;

/**
 * An exception representing failures during information management.
 * 
 * @author tlerios@marketcetera.com
 * @since 2.0.0
 * @version $Id: InfoException.java 16468 2014-05-12 00:36:56Z colin $
 */

/* $License$ */

@ClassVersion("$Id: InfoException.java 16468 2014-05-12 00:36:56Z colin $")
public class InfoException
    extends I18NException
{

    // CLASS DATA.

    private static final long serialVersionUID=1L;


    // CONSTRUCTORS.

    /**
     * Constructs a new exception with the given message, but without
     * an underlying cause.
     *
     * @param message The message.
     */

    public InfoException
        (I18NBoundMessage message)
    {
        super(message);
    }

    /**
     * Constructs a new exception with the given message and
     * underlying cause.
     *
     * @param cause The cause.
     * @param message The message.
     */

    public InfoException
        (Throwable cause,
         I18NBoundMessage message)
    {
        super(cause,message);
    }
}

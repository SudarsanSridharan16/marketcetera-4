package org.marketcetera.admin;

/* $License$ */

/**
 * Provides a mutable {@link Permission} implementation.
 *
 * @author <a href="mailto:colin@marketcetera.com">Colin DuPlantis</a>
 * @version $Id$
 * @since $Release$
 */
public interface MutablePermission
        extends Permission
{
    /**
     * Set the name value.
     *
     * @param inName a <code>String</code> value
     */
    void setName(String inName);
    /**
     * Set the description value.
     *
     * @param inDescription a <code>String</code> value
     */
    void setDescription(String inDescription);
}

package org.everit.osgi.audit.ri;

public class UnknownAuditApplicationException extends RuntimeException {

    /**
     * Serial version UID.
     */
    private static final long serialVersionUID = 6784626196905796600L;

    private final String applicationName;

    public UnknownAuditApplicationException(final String applicationName) {
        super("audit application [" + applicationName + "] does not exist");
        this.applicationName = applicationName;
    }

    public String getApplicationName() {
        return applicationName;
    }

}

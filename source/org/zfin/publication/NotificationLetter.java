package org.zfin.publication;

import java.util.Collection;

public class NotificationLetter {

    private Collection<String> recipients;
    private String message;

    public Collection<String> getRecipients() {
        return recipients;
    }

    public void setRecipients(Collection<String> recipients) {
        this.recipients = recipients;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }
}

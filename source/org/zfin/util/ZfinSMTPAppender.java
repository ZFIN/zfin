package org.zfin.util;

import org.apache.log4j.Layout;
import org.apache.log4j.helpers.CyclicBuffer;
import org.apache.log4j.helpers.LogLog;
import org.apache.log4j.net.SMTPAppender;
import org.apache.log4j.spi.LoggingEvent;
import org.apache.log4j.spi.ThrowableInformation;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.util.log4j.ZfinHtmlLayout;
import org.zfin.util.servlet.RequestBean;

import javax.mail.*;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import java.util.Date;
import java.util.Properties;

/**
 * ToDo: ADD DOCUMENTATION!
 */
public class ZfinSMTPAppender extends SMTPAppender {

    private static ThreadLocal<CyclicBuffer> buffer;

    /**
     * Perform SMTPAppender specific appending actions, mainly adding
     * the event to a cyclic buffer and checking if the event triggers
     * an e-mail to be sent.
     */
    public void append(LoggingEvent event) {

        if (!checkEntryConditions()) {
            return;
        }

        if (getLocationInfo()) {
            event.getLocationInformation();
        }
        if (evaluator.isTriggeringEvent(event)) {
            CyclicBuffer threadBuffer = null;
            if (buffer == null) {
                buffer = new ThreadLocal<CyclicBuffer>();
            }
            threadBuffer = buffer.get();
            if (threadBuffer == null)
                threadBuffer = new CyclicBuffer(20);
            threadBuffer.add(event);
            buffer.set(threadBuffer);
        }
    }

    /**
     * Send out email with all events in an single email.
     *
     * @param bean request bean
     */
    public void sendEmailOfEvents(RequestBean bean) {
        // if the buffer is empty do not send anything.
        if (buffer == null)
            return;

        try {
            MimeBodyPart part = new MimeBodyPart();
            StringBuffer messageBuffer = new StringBuffer();
            String t = layout.getHeader();
            if (layout instanceof ZfinHtmlLayout) {
                t = ((ZfinHtmlLayout) layout).getHeader(bean);
            }
            if (t != null)
                messageBuffer.append(t);
            CyclicBuffer localBuffer = buffer.get();
            if (localBuffer == null || localBuffer.length() == 0)
                return;

            String rootCause = null;
            int len = localBuffer.length();
            for (int i = 0; i < len; i++) {
                LoggingEvent event = localBuffer.get();
                messageBuffer.append(layout.format(event));
                if (i == 0)
                    rootCause = getRootCause(event.getThrowableInformation());
                if (layout.ignoresThrowable()) {
                    String[] s = event.getThrowableStrRep();
                    if (s != null) {
                        for (String value : s) {
                            messageBuffer.append(value);
                            messageBuffer.append(Layout.LINE_SEP);
                        }
                    }
                }
            }
            t = layout.getFooter();
            if (t != null)
                messageBuffer.append(t);
            part.setContent(messageBuffer.toString(), layout.getContentType());

            Multipart mp = new MimeMultipart();
            mp.addBodyPart(part);
            msg.setContent(mp);
            msg.setSentDate(new Date());
            String subject = createSubjectLine(bean, rootCause);
            msg.setSubject(subject);
            Transport.send(msg);
            LogLog.debug("Error Email sent out");
        } catch (Exception e) {
            LogLog.error("Error occurred while sending e-mail notification.", e);
        }
    }

    private String createSubjectLine(RequestBean bean, String rootCause) {
        StringBuffer subject = new StringBuffer();
        subject.append("[");
        subject.append(ZfinPropertiesEnum.DOMAIN_NAME);
        subject.append("]");
        subject.append(getSubject());
        subject.append("[");
        if (bean.getPerson() != null)
            subject.append(bean.getPerson().getFullName());
        else
            subject.append("Unknown");
        subject.append("]");
        subject.append("[");
        subject.append(bean.getRequest());
        subject.append("]");
        if (rootCause != null) {
            subject.append("[");
            subject.append(ZfinStringUtils.getTruncatedString(rootCause, 50));
            subject.append("]");
        }
        return subject.toString();
    }

    private String getRootCause(ThrowableInformation throwable) {
        if (throwable == null)
            return null;
        Throwable t = throwable.getThrowable();
        Throwable cause = t.getCause();
        if (cause == null)
            return t.getMessage();

        String causeMessage = cause.getMessage();
        while (t.getCause() != null) {
            causeMessage = t.getCause().getMessage();
            t = t.getCause();
        }
        return causeMessage;
    }

    private String createHeaderEvent(LoggingEvent event) {
        StringBuffer headerInfo = new StringBuffer();
        String message = event.getThrowableInformation().getThrowable().getCause().getMessage();
        headerInfo.append(message);
        return headerInfo.toString();
    }

    protected Session createSession() {
        Session sess = super.createSession();
        Properties props = null;
        try {
            props = new Properties(System.getProperties());
        } catch (SecurityException ex) {
            props = new Properties();
        }
        props.put("mail.smtp.host", "smtp.uoregon.edu");
        props.put("mail.transport.protocol", "smtp");
        Session session = Session.getInstance(props, null);
        //session.setDebug(true);
        return session;
    }

}

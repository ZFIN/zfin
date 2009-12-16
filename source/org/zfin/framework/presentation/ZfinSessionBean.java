package org.zfin.framework.presentation;

import org.apache.log4j.Logger;
import org.zfin.framework.ZfinSession;
import org.zfin.repository.RepositoryFactory;
import org.zfin.security.repository.UserRepository;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectOutputStream;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;

/**
 * Created by IntelliJ IDEA.
 */
public class ZfinSessionBean {

    private HttpServletRequest request;
    private HashMap requestAttributes;
    private HashMap sessionAttributes;

    private static Logger LOG = Logger.getLogger(ZfinSessionBean.class);

    public HashMap getRequestAttributes() {
        if (requestAttributes != null)
            return requestAttributes;

        Enumeration en = request.getAttributeNames();
        requestAttributes = new HashMap();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            requestAttributes.put(key, (Object) request.getAttribute(key));
        }

        return requestAttributes;
    }

    public void setRequestAttributes(HashMap requestAttributes) {
        this.requestAttributes = requestAttributes;
    }

    public HashMap getSessionAttributes() {
        if (sessionAttributes != null)
            return sessionAttributes;

        HttpSession session = request.getSession();
        Enumeration en = session.getAttributeNames();
        sessionAttributes = new HashMap();
        while (en.hasMoreElements()) {
            String key = (String) en.nextElement();
            sessionAttributes.put(key, (Object) session.getAttribute(key));
        }
        return sessionAttributes;
    }

    public void setSessionAttributes(HashMap sessionAttributes) {
        this.sessionAttributes = sessionAttributes;
    }

    public HttpServletRequest getRequest() {
        return request;
    }

    public void setRequest(HttpServletRequest request) {
        this.request = request;
    }

    public List<ZfinSession> getActiveSessions() {
        UserRepository ur = RepositoryFactory.getUserRepository();
        return ur.getActiveSessions();
    }

    public long getSessionSize() {
        long total = 0;

        HttpSession session = request.getSession();
        ByteArrayOutputStream baos = null;
        ObjectOutputStream oos = null;
        try {
            baos = new ByteArrayOutputStream();
            oos = new ObjectOutputStream(baos);
            Enumeration enumeration = session.getAttributeNames();

            while (enumeration.hasMoreElements()) {
                String name = (String) enumeration.nextElement();
                Object obj = session.getAttribute(name);
                oos.writeObject(obj);

                long size = baos.size();
                total += size;
                LOG.debug("The session name: " + name + " and the size is: " + size);
            }
            oos.close();
            baos.close();
            LOG.debug("Total session size is: " + total);
        }
        catch (Exception e) {
            LOG.error("Could not get the session size", e);
            if (oos != null)
                try {
                    oos.close();
                    baos.close();
                } catch (IOException e1) {
                    e1.printStackTrace();
                }
            e.printStackTrace();
        }

        return total;
    }
}

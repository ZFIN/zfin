package org.zfin.framework.presentation;

import org.springframework.web.context.WebApplicationContext;

import java.util.Date;

/**
 * Created by IntelliJ IDEA.
 * User: Christian Pich
 * Date: Oct 11, 2006
 * Time: 9:14:45 AM
 * To change this template use File | Settings | File Templates.
 */
public class ApplicationContextBean {

    private WebApplicationContext applicationContext;

    public WebApplicationContext getApplicationContext() {
        return applicationContext;
    }

    public void setApplicationContext(WebApplicationContext applicationContext) {
        this.applicationContext = applicationContext;
    }

    public Date getStartup() {
        Date startup = new Date(applicationContext.getStartupDate());
        return startup;
    }
}

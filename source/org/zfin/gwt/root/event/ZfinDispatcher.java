package org.zfin.gwt.root.event;

import com.google.gwt.core.client.GWT;
import com.google.gwt.http.client.Request;
import com.google.gwt.http.client.RequestBuilder;
import com.google.gwt.http.client.RequestException;
import com.google.gwt.logging.client.LogConfiguration;
import com.google.gwt.user.client.Window;
import org.fusesource.restygwt.client.Dispatcher;
import org.fusesource.restygwt.client.Method;
import org.fusesource.restygwt.client.dispatcher.DefaultDispatcher;

import java.util.logging.Logger;

public class ZfinDispatcher implements Dispatcher {

    public static final ZfinDispatcher INSTANCE = new ZfinDispatcher();

    @Override
    public Request send(Method method, RequestBuilder builder) throws RequestException {
/*
        if(GWT.isClient()  ){
            Logger logger = Logger.getLogger( ZfinDispatcher.class.getName() );
            logger.severe("Sending http request: " + builder.getHTTPMethod() + " "
                    + builder.getUrl() + " ,timeout:"
                    + builder.getTimeoutMillis());

            String content = builder.getRequestData();
            if (content != null && content.length() > 0) {
                logger.fine(content);
            }
        }
*/

        return builder.send();
    }
}

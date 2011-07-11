package org.zfin.gwt.root.server.rpc;

import com.google.gwt.user.server.rpc.RemoteServiceServlet;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Overrides GWT basic servlet to log the request data (POST).
 */
public class ZfinRemoteServiceServlet extends RemoteServiceServlet {

    public static final String GWT_REQUEST_STRING = "GWT Request String";
    private static final Logger LOG = Logger.getLogger(ZfinRemoteServiceServlet.class);

    @Override
    protected String readContent(HttpServletRequest request) throws ServletException, IOException {
        String postedContents = super.readContent(request);
        request.setAttribute(GWT_REQUEST_STRING, postedContents);
        return postedContents;
    }

}

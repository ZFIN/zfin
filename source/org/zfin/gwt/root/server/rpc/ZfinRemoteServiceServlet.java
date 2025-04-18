package org.zfin.gwt.root.server.rpc;

import com.google.gwt.user.server.rpc.jakarta.RemoteServiceServlet;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import java.io.IOException;

/**
 * Overrides GWT basic servlet to log the request data (POST).
 */
public class ZfinRemoteServiceServlet extends RemoteServiceServlet {

    public static final String GWT_REQUEST_STRING = "GWT Request String";
    private static final Logger LOG = LogManager.getLogger(ZfinRemoteServiceServlet.class);

    @Override
    protected String readContent(HttpServletRequest request) throws ServletException, IOException {
        String postedContents = super.readContent(request);
        request.setAttribute(GWT_REQUEST_STRING, postedContents);
        return postedContents;
    }

}

package org.zfin.framework.presentation;

import com.opensymphony.clickstream.Clickstream;
import com.opensymphony.clickstream.ClickstreamListener;
import com.opensymphony.clickstream.ClickstreamRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.validation.BindException;
import org.springframework.web.servlet.ModelAndView;
import org.springframework.web.servlet.mvc.AbstractCommandController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.List;
import java.util.Map;

/**
 * Controller that obtains the meta data for the database.
 */
public class SingleUserRequestTrackController extends AbstractCommandController {

    public SingleUserRequestTrackController() {
        setCommandClass(UserRequestTrackBean.class);
    }

    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {

        UserRequestTrackBean form = (UserRequestTrackBean) command;
        String sessionID = form.getSid();
        if (StringUtils.isEmpty(sessionID))
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, "");
        Map<String, Clickstream> clickstreamMap = (Map<String, Clickstream>) request.getSession().getServletContext().getAttribute(ClickstreamListener.CLICKSTREAMS_ATTRIBUTE_KEY);
        form.setClickstreamMap(clickstreamMap);
        Clickstream clickstream = clickstreamMap.get(sessionID);
        if (clickstream == null)
            return new ModelAndView("record-not-found.page", LookupStrings.ZDB_ID, form.getSid());

        form.setClickstream(clickstream);
        if (form.getTime() > 0) {
            calculateIndexOfRequest(form);
        }
        return new ModelAndView("single-user-request-tracking", LookupStrings.FORM_BEAN, form);
    }

    /**
     * Calculates the index (request) in the collection a given time stamp corresponds to.
     *
     * @param form  bean
     */
    private void calculateIndexOfRequest(UserRequestTrackBean form) {
        int indexOfRequest = -1;
        for (ClickstreamRequest clickstreamRequest : ((List<ClickstreamRequest>) form.getClickstream().getStream())) {
            long requestDateTime = clickstreamRequest.getTimestamp().getTime();
            if (requestDateTime > form.getTime()) {
                form.setIndexOfRequest(indexOfRequest - 1);
                return;
            }
            indexOfRequest++;
        }
    }
}

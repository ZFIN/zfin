package org.zfin.framework.presentation;

import com.opensymphony.clickstream.Clickstream;
import com.opensymphony.clickstream.ClickstreamListener;
import com.opensymphony.clickstream.ClickstreamRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.List;
import java.util.Map;

/**
 * Controller that obtains the meta data for the database.
 */
@Controller
public class SingleUserRequestTrackController {

    @RequestMapping(value = "/view-single-user-request-tracking")
    protected String showBlastSearchPage(HttpServletRequest request,
                                         @ModelAttribute("formBean") UserRequestTrackBean form,
                                         Model model) throws Exception {

        String sessionID = form.getSid();
        if (StringUtils.isEmpty(sessionID)) {
            model.addAttribute(LookupStrings.ZDB_ID, "");
            return "record-not-found.page";
        }
        Map<String, Clickstream> clickstreamMap = (Map<String, Clickstream>) request.getSession().getServletContext().getAttribute(ClickstreamListener.CLICKSTREAMS_ATTRIBUTE_KEY);
        form.setClickstreamMap(clickstreamMap);
        Clickstream clickstream = clickstreamMap.get(sessionID);
        if (clickstream == null) {
            model.addAttribute(LookupStrings.ZDB_ID, form.getSid());
            return "record-not-found.page";
        }

        form.setClickstream(clickstream);
        if (form.getTime() > 0) {
            calculateIndexOfRequest(form);
        }
        return "single-user-request-tracking";
    }

    /**
     * Calculates the index (request) in the collection a given time stamp corresponds to.
     *
     * @param form bean
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

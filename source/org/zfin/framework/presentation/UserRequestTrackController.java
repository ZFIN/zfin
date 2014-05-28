package org.zfin.framework.presentation;

import com.opensymphony.clickstream.Clickstream;
import com.opensymphony.clickstream.ClickstreamListener;
import com.opensymphony.clickstream.ClickstreamRequest;
import org.apache.commons.lang.StringUtils;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.RequestMapping;

import javax.servlet.http.HttpServletRequest;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller that obtains the meta data for the database.
 */
@Controller
public class UserRequestTrackController {


    @RequestMapping("/view-user-request-tracks")
    protected String showGlobalSession(@ModelAttribute("form") UserRequestTrackBean form,
                                       HttpServletRequest request) throws Exception {
        Map<String, Clickstream> clickstreamMap = (Map<String, Clickstream>) request.getSession().getServletContext().getAttribute(ClickstreamListener.CLICKSTREAMS_ATTRIBUTE_KEY);
        Map<String, Clickstream> filteredClickstreamMap = clickstreamMap;
        if (StringUtils.isNotEmpty(form.getUrlSearchString()))
            filteredClickstreamMap = filterClickstreamByUrl(form.getUrlSearchString(), clickstreamMap);
        filteredClickstreamMap = filterClickstreamByRobot(form.getShowBots(), filteredClickstreamMap);
        form.setClickstreamMap(filteredClickstreamMap);
        return "user-request-tracking.page";
    }

    private Map<String, Clickstream> filterClickstreamByRobot(String showBot, Map<String, Clickstream> fullClickstreamMap) {
        if (fullClickstreamMap == null)
            return null;
        if (StringUtils.isEmpty(showBot))
            return fullClickstreamMap;

        ShowBot robotFilter = ShowBot.getShowBot(showBot);
        if (robotFilter.equals(ShowBot.BOTH))
            return fullClickstreamMap;

        Map<String, Clickstream> clickstreamMap = new HashMap<String, Clickstream>(fullClickstreamMap.size());
        for (String key : fullClickstreamMap.keySet()) {
            Clickstream clickStream = fullClickstreamMap.get(key);
            if (robotFilter.equals(ShowBot.TRUE) && clickStream.isBot())
                clickstreamMap.put(key, clickStream);
            else if (robotFilter.equals(ShowBot.FALSE) && !clickStream.isBot())
                clickstreamMap.put(key, clickStream);
        }
        return clickstreamMap;
    }

    private Map<String, Clickstream> filterClickstreamByUrl(String urlSearchString, Map<String, Clickstream> fullClickstreamMap) {
        if (fullClickstreamMap == null)
            return null;
        Map<String, Clickstream> clickstreamMap = new HashMap<String, Clickstream>(fullClickstreamMap.size());
        for (String key : fullClickstreamMap.keySet()) {
            Clickstream clickStream = fullClickstreamMap.get(key);
            if (containsUrlSearchString(clickStream, urlSearchString))
                clickstreamMap.put(key, clickStream);
        }
        return clickstreamMap;
    }

    /**
     * Checks if a clickstream contains a given url search string
     *
     * @param clickStream     clickstream
     * @param urlSearchString search string
     * @return true or false
     */
    private boolean containsUrlSearchString(Clickstream clickStream, String urlSearchString) {
        if (clickStream == null)
            return false;
        List<ClickstreamRequest> requests = clickStream.getStream();
        for (ClickstreamRequest request : requests) {
            if (request.toString().contains(urlSearchString))
                return true;
        }
        return false;  //To change body of created methods use File | Settings | File Templates.
    }

    public enum ShowBot {
        TRUE,
        FALSE,
        BOTH;

        public static ShowBot getShowBot(String type) {
            for (ShowBot t : values()) {
                if (t.toString().equals(type))
                    return t;
            }
            throw new RuntimeException("No showBot type of string " + type + " found.");
        }

    }
}

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
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * Controller that obtains the meta data for the database.
 */
public class UserRequestTrackController extends AbstractCommandController {

    public UserRequestTrackController() {
        setCommandClass(UserRequestTrackBean.class);
        setCommandName(LookupStrings.FORM_BEAN);
    }

    @SuppressWarnings({"unchecked"})
    protected ModelAndView handle(HttpServletRequest request, HttpServletResponse response, Object command, BindException errors) throws Exception {
        UserRequestTrackBean form = (UserRequestTrackBean) command;
        Map<String, Clickstream> clickstreamMap = (Map<String, Clickstream>) request.getSession().getServletContext().getAttribute(ClickstreamListener.CLICKSTREAMS_ATTRIBUTE_KEY);
        Map<String, Clickstream> filteredClickstreamMap = clickstreamMap;
        if (StringUtils.isNotEmpty(form.getUrlSearchString()))
            filteredClickstreamMap = filterClickstreamByUrl(form.getUrlSearchString(), clickstreamMap);
        filteredClickstreamMap = filterClickstreamByRobot(form.getShowBots(), filteredClickstreamMap);
        form.setClickstreamMap(filteredClickstreamMap);
        return new ModelAndView("user-request-tracking", LookupStrings.FORM_BEAN, form);
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

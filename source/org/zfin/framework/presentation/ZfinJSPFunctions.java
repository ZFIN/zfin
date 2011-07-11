package org.zfin.framework.presentation;

import com.opensymphony.clickstream.ClickstreamRequest;
import org.apache.commons.lang.StringEscapeUtils;
import org.springframework.security.core.context.SecurityContext;
import org.springframework.security.web.context.HttpSessionSecurityContextRepository;
import org.zfin.ontology.GenericTermRelationship;
import org.zfin.ontology.Ontology;
import org.zfin.ontology.OntologyManager;
import org.zfin.people.Person;
import org.zfin.repository.RepositoryFactory;
import org.zfin.util.DateUtil;

import javax.servlet.http.HttpSession;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadInfo;
import java.lang.management.ThreadMXBean;
import java.util.*;

/**
 * Class that is called from JSP through a function call.
 */
public class ZfinJSPFunctions {
    public static final int MILLISECONDS_PER_HOUR = 3600000;
    public static final int MILLISECONDS_PER_MINUTE = 60000;
    public static final int MILLISECONDS_PER_SECOND = 1000;

    /**
     * Escape characters to valid HTML code
     *
     * @param string character string
     * @return String
     */
    public static String escapeJavaScript(String string) {
        if (string.indexOf("\r\n") > -1)
            string = string.replaceAll("\r\n", "<br/>");
        if (string.indexOf("\n") > -1)
            string = string.replaceAll("\n", "<br/>");

        return StringEscapeUtils.escapeJavaScript(string);
    }

    /**
     * Turn newlines to <br/>. If escapeAll is set to true then
     * escape html characters to be plan strings.
     *
     * @param string    character string
     * @param escapeAll boolean
     * @return String
     */
    public static String escapeHtml(String string, boolean escapeAll) {
        if (escapeAll)
            string = StringEscapeUtils.escapeHtml(string);
        if (string.indexOf("\r\n") > -1)
            string = string.replaceAll("\r\n", "<br/>");
        if (string.indexOf("\n") > -1)
            string = string.replaceAll("\n", "<br/>");

        return string;
    }

    /**
     * Convenience method to obtain the visibility of a section for a given section name.
     *
     * @param sectionName string
     * @param visibility  visibility map
     * @return boolean
     */
    public static boolean isSectionVisible(String sectionName, SectionVisibility visibility) {
        return visibility.isVisible(sectionName);
    }

    /**
     * Convenience method to obtain the visibility of a section for a given section name.
     *
     * @param sectionName string
     * @param visibility  visibility map
     * @return boolean
     */
    public static boolean dataAvailable(String sectionName, SectionVisibility visibility) {
        return visibility.hasData(sectionName);
    }

    /**
     * Remove a key-value pair from a request.queryString.
     * this will also remove the character before the found key-value string being found,
     * the ampersand.
     *
     * @param queryString query string from request
     * @param key         string
     * @param value       string
     * @return string
     */
    public static String removeQueryParameter(String queryString, String key, String value) {
        if (queryString == null)
            return "";
        if (key == null)
            return "";
        if (value == null)
            value = "";
        String pair = key + "=" + value;
        String pairPlusAmpersand = "&" + pair;
        if (queryString.indexOf(pairPlusAmpersand) > 0)
            return queryString.replace(pairPlusAmpersand, "");
        return queryString.replace(pair, "");
    }

    /**
     * Remove all key-value pairs for a given section visibility from a request.queryString.
     * this will also remove the character before the found key-value string being found,
     * the ampersand.
     *
     * @param queryString query string from request
     * @param prefix      to visibility action string
     * @return string
     */
    public static String removeAllVisibleQueryParameters(String queryString, String prefix) {
        if (queryString == null)
            return "";
        if (prefix == null)
            prefix = "";

        String valueTrue = "=true";
        String valueFalse = "=false";
        for (SectionVisibility.Action action : SectionVisibility.Action.getActionItems()) {
            String pairPlusAmpersand = "&" + prefix + action.toString();
            if (queryString.indexOf(pairPlusAmpersand) > 0) {
                queryString = queryString.replace(pairPlusAmpersand + valueTrue, "");
                queryString = queryString.replace(pairPlusAmpersand + valueFalse, "");
            } else {
                queryString = queryString.replace(action.toString() + valueTrue, "");
                queryString = queryString.replace(action.toString() + valueFalse, "");
            }
        }
        return queryString;
    }

    /**
     * Remove all key-value pairs for a given section visibility from a request.queryString.
     * this will also remove the character before the found key-value string being found,
     * the ampersand.
     *
     * @param queryString  query string from request
     * @param prefix       to visibility action string
     * @param enumerations enumerations implementing Values interface
     * @return string
     */

    public static String removeAllVisibilityQueryParameters(String queryString, String prefix, String[] enumerations) {
        if (queryString == null)
            return "";
        if (prefix == null)
            prefix = "sectionVisibility.";

        for (SectionVisibility.Action action : SectionVisibility.Action.getActionItems()) {
            String prefixedValue = prefix + action.toString();
            queryString = removeBooleanParameters(queryString, prefixedValue);
        }
        if (enumerations == null)
            return queryString;

        for (String value : enumerations) {
            queryString = removeShowAndHideSections(queryString, prefix, value);
        }
        return queryString;
    }

    private static String removeShowAndHideSections(String queryString, String prefix, String value) {
        String prefixedValue = prefix + SectionVisibility.Action.SHOW_SECTION.toString() + "=" + value;
        queryString = removeQueryParameter(queryString, prefixedValue);
        prefixedValue = SectionVisibility.Action.HIDE_SECTION.toString() + "=" + value;
        return removeQueryParameter(queryString, prefixedValue);
    }

    private static String removeQueryParameter(String queryString, String prefixedValue) {
        String pairPlusAmpersand = "&" + prefixedValue;
        if (queryString.indexOf(pairPlusAmpersand) > 0) {
            queryString = queryString.replace(pairPlusAmpersand, "");
        } else {
            queryString = queryString.replace(prefixedValue, "");
        }
        return queryString;
    }

    public static String removeBooleanParameters(String queryString, String prefixedValue) {
        String valueTrue = "=" + Boolean.TRUE.toString();
        String valueFalse = "=" + Boolean.TRUE.toString();
        String pairPlusAmpersand = "&" + prefixedValue;
        if (queryString.indexOf(pairPlusAmpersand) > 0) {
            queryString = queryString.replace(pairPlusAmpersand + valueTrue, "");
            queryString = queryString.replace(pairPlusAmpersand + valueFalse, "");
        } else {
            queryString = queryString.replace(prefixedValue + valueTrue, "");
            queryString = queryString.replace(prefixedValue + valueFalse, "");
        }
        return queryString;
    }

    public static boolean isVisible(String sectionName, SectionVisibility visibility) {
        return visibility.isVisible(sectionName);
    }

    public static boolean isOntologyLoaded(OntologyManager manager, Ontology ontology) {
        if (manager == null || ontology == null)
            return false;
        return manager.isOntologyLoaded(ontology);
    }

    public static Set<String> getDistinctRelationshipTypes(Ontology ontology) {
        List<GenericTermRelationship> termRelationships ;
        if(ontology!=null){
            termRelationships=  RepositoryFactory.getOntologyRepository().getAllRelationships(ontology);
        }
        else{
            termRelationships=  RepositoryFactory.getOntologyRepository().getAllRelationships();
        }
        Set<String> relationships = new TreeSet<String>();
        for(GenericTermRelationship genericTermRelationship : termRelationships){
            relationships.add(genericTermRelationship.getType());
        }

        return relationships;
    }

    public static String getTimeDuration(Date start, Date end) {
        return DateUtil.getTimeDuration(start, end);
    }

    public static String getTimeBetweenRequests(List<ClickstreamRequest> list, int loopIndex) {
        if (loopIndex < 0 || list == null)
            return "";

        if (list.size() <= loopIndex + 1)
            return "";

        Date start = list.get(loopIndex).getTimestamp();
        Date end = list.get(loopIndex + 1).getTimestamp();
        return getTimeDuration(start, end);
    }

    public static String getPerson(HttpSession session) {
        if (session == null)
            return null;
        String name = "Guest";
        SecurityContext securityContext = (SecurityContext) session.getAttribute(HttpSessionSecurityContextRepository.SPRING_SECURITY_CONTEXT_KEY);
        if (securityContext != null && securityContext.getAuthentication() != null) {
            name = ((Person) securityContext.getAuthentication().getPrincipal()).getFullName();
        }
        return name;
    }

    public static boolean isToday(Date date) {
        Calendar now = GregorianCalendar.getInstance();
        Calendar givenDate = GregorianCalendar.getInstance();
        givenDate.setTime(date);
        return now.get(Calendar.MONTH) == givenDate.get(Calendar.MONTH) &&
                now.get(Calendar.DAY_OF_MONTH) == givenDate.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean isTomorrow(Date date) {
        Calendar tomorrow = GregorianCalendar.getInstance();
        tomorrow.add(Calendar.DAY_OF_MONTH, 1);
        Calendar givenDate = GregorianCalendar.getInstance();
        givenDate.setTime(date);
        return tomorrow.get(Calendar.MONTH) == givenDate.get(Calendar.MONTH) &&
                tomorrow.get(Calendar.DAY_OF_MONTH) == givenDate.get(Calendar.DAY_OF_MONTH);
    }

    public static boolean isYesterday(Date date) {
        Calendar yesterday = GregorianCalendar.getInstance();
        yesterday.add(Calendar.DAY_OF_MONTH, -1);
        Calendar givenDate = GregorianCalendar.getInstance();
        givenDate.setTime(date);
        return yesterday.get(Calendar.MONTH) == givenDate.get(Calendar.MONTH) &&
                yesterday.get(Calendar.DAY_OF_MONTH) == givenDate.get(Calendar.DAY_OF_MONTH);
    }

    public static String generateRandomDomID() {
        return UUID.randomUUID().toString();
    }

    /**
     * Retrieve the latest zfin method invocation on a given thread.
     * If no zfin class involved give last method call.
     *
     * @param threadID thread ID
     * @return method being called on this thread.
     */
    public static String lastZfinCall(int threadID) {
        ThreadMXBean mxbean = ManagementFactory.getThreadMXBean();
        ThreadInfo threadInfo = mxbean.getThreadInfo(threadID, Integer.MAX_VALUE);
        if (threadInfo == null)
            return null;
        StackTraceElement[] stackTraceElements = threadInfo.getStackTrace();
        for (StackTraceElement element : stackTraceElements) {
            if (element.getClassName().startsWith("org.zfin"))
                return element.toString();
        }
        return null;
    }
}

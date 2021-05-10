package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.util.URLCreator;
import org.zfin.util.validation.IntegerValue;

import javax.validation.constraints.Max;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;

/**
 * Basic bean object that can be used for any form bean to include pagination.
 * Inherit from this Bean and you get all the relevant request parameters from a
 * a web page. The JSP part can be found in the pagination.tag.
 */

public class PaginationBean {

    public static final String PAGE = "page";
    public static final int MAXPAGELINKS = 9;
    public static final int MAX_DISPLAY_RECORDS_DEFAULT = 20;

    /* MAXPAGELINKS must be an odd integer that denotes the total number of
       page links to display at the bottom of each result page.  Calculate by:
       2 * (# pages to include above or below current page) + 1.  The +1 is
       to include the current page in the total number of pages.
    **/

    @Max(value = 1000, message = "maxDisplayRecords must be less or equal 1000")
    protected int maxDisplayRecordsInteger = MAX_DISPLAY_RECORDS_DEFAULT;
    private int totalRecords;
    protected int pageInteger = 1;
    private String formLink;

    private String welcomeInputSubject;
    private String welcomeInputID;
    private String queryString;
    private StringBuffer requestUrl;


    // preempts the integer attribute 'maxDisplayRecords' in the super class which is an integer.
    // This allows validating the values before the Integer-conversion fails.
    @IntegerValue(message = "The value {0} of the request parameter 'maxDisplayRecordsString' is not a number")
    private String maxDisplayRecords;

    // preempts the integer attribute 'page' (int) in the super class
    @IntegerValue(message = "The value of the request parameter 'pageString' is not a number")
    private String page = "1";


    // for APG pagination
    private String actionUrl;
    private int firstPageRecord;

    public String getMaxDisplayRecords() {
        return maxDisplayRecords;
    }

    public String getMaxDisplayRecordsTop() {
        return maxDisplayRecords;
    }

    public String getMaxDisplayRecordsBottom() {
        return maxDisplayRecords;
    }

    public void setMaxDisplayRecords(String maxDisplayRecord) {
        setMaxDisplayRecords((Object) maxDisplayRecord);
    }

    public void setMaxDisplayRecords(Object maxDisplayRecord) {
        if (maxDisplayRecord instanceof Integer) {
            maxDisplayRecordsInteger = (Integer) maxDisplayRecord;
            return;
        }
        String maxDisplayRecords = (String) maxDisplayRecord;
        this.maxDisplayRecords = maxDisplayRecords;
        try {
            maxDisplayRecordsInteger = Integer.parseInt(maxDisplayRecords);
        } catch (NumberFormatException e) {
            // ignore as this object will be validated at a later stage.
        }
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getPageInteger() {
        return pageInteger;
    }

    public void setPageInteger(int page) {
        this.pageInteger = page;
    }

    public int getTotalNumPages() {
        return (int) Math.ceil((double) totalRecords / (double) maxDisplayRecordsInteger);
    }

    public int getFirstRecord() {
        return ((pageInteger - 1) * maxDisplayRecordsInteger + 1);
    }

    public int getLastRecord() {
        return (pageInteger * maxDisplayRecordsInteger);
    }

    public int getPreviousPage() {
        return pageInteger - 1;
    }

    public int getNextPage() {
        return pageInteger + 1;
    }

    public List<Integer> getPageList() {
        int totalPages = getTotalNumPages();
        List<Integer> pageList = new ArrayList<Integer>();
        for (int i = MAXPAGELINKS / 2; i > 0; i--) {
            if (pageInteger - i > 0)
                pageList.add(pageInteger - i);
        }

        pageList.add(pageInteger);

        for (int i = 1; i <= MAXPAGELINKS / 2; i++) {
            if (pageInteger + i <= totalPages)
                pageList.add(pageInteger + i);
        }

        return pageList;
    }

    public boolean isFirstPage() {
        return pageInteger == 1;
    }

    public boolean isLastPage() {
        return pageInteger == getTotalNumPages();
    }

    public boolean getIsFirstPage() {
        return pageInteger == 1;
    }

    public boolean getIsLastPage() {
        return pageInteger == getTotalNumPages();
    }

    public boolean isElisionForHigherPages() {
        return (getTotalNumPages() - pageInteger) >= (MAXPAGELINKS + 3) / 2;
    }

    public boolean isShowLastPage() {
        return pageInteger + (MAXPAGELINKS - 1) / 2 < getTotalNumPages();
    }

    public boolean isShowFirstPage() {
        return pageInteger - (MAXPAGELINKS - 1) / 2 > 1;
    }

    public boolean isElisionForLowerPages() {
        return pageInteger > (MAXPAGELINKS + 3) / 2;
    }

    public void addRequestParameter(String name, String value) {
        formLink += "&" + name + "=" + value;
    }

    public String getFormLink() {
        return formLink;
    }

    public boolean isPaginationNeeded() {
        return totalRecords > maxDisplayRecordsInteger;
    }

    public String getWelcomeInputSubject() {
        return welcomeInputSubject;
    }

    public void setWelcomeInputSubject(String welcomeInputSubject) {
        this.welcomeInputSubject = welcomeInputSubject;
    }

    public String getWelcomeInputID() {
        return welcomeInputID;
    }

    public void setWelcomeInputID(String welcomeInputID) {
        this.welcomeInputID = welcomeInputID;
    }

    /**
     * This return the full url with all query parameters included
     * but the page parameter which is set on the JSP page dynamically.
     *
     * @return full URL string
     */
    public String getActionUrl() {
        if (!StringUtils.isEmpty(actionUrl))
            return actionUrl;
        // remove the page=xxx parameter
        if (queryString == null)
            queryString = "";
        URLCreator urlCreator = new URLCreator(requestUrl + "?" + queryString);
        urlCreator.removeNameValuePair(PAGE);
        return urlCreator.getFullURLPlusSeparator();

    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public int getFirstRecordOnPage() {
        return (pageInteger - 1) * maxDisplayRecordsInteger + 1;
    }

    public int getLastRecordOnPage() {
        return (pageInteger) * maxDisplayRecordsInteger;
    }

    public int getFirstPageRecord() {
        return firstPageRecord;
    }

    public int getLastPageRecord() {
        return firstPageRecord + maxDisplayRecordsInteger;
    }

    public void setFirstPageRecord(int firstPageRecord) {
        this.firstPageRecord = firstPageRecord;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        if (queryString == null)
            return;
        this.queryString = URLDecoder.decode(queryString);
    }

    public void setRequestUrl(StringBuffer requestUrl) {
        this.requestUrl = requestUrl;
    }

    public String getPage() {
        return page;
    }

    public void setPage(String pageString) {
        this.page = pageString;
        try {
            pageInteger = Integer.parseInt(pageString);
        } catch (NumberFormatException e) {
            // ignore as this object will be validated at a later stage.
        }
    }

    public int getMaxDisplayRecordsInteger() {
        return maxDisplayRecordsInteger;
    }

}

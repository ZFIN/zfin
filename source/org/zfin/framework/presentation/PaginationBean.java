package org.zfin.framework.presentation;

import org.apache.commons.lang.StringUtils;
import org.zfin.util.URLCreator;

import java.util.List;
import java.util.ArrayList;
import java.net.URLDecoder;

/**
 * Basic bean object that can be used for any form bean to include pagination.
 * Inherit from this Bean and you get all the relevant request parameters from a
 * a web page. The JSP part can be found in the pagination.tag.
 */

public class PaginationBean {

    public static final String PAGE = "page";
    public static final int MAXPAGELINKS = 3;
    public static final int MAX_DISPLAY_RECORDS_DEFAULT = 25;

    /* MAXPAGELINKS must be an odd integer that denotes the total number of
       page links to display at the bottom of each result page.  Calculate by:
       2 * (# pages to include above or below current page) + 1.  The +1 is
       to include the current page in the total number of pages.
    **/

    protected int maxDisplayRecords = MAX_DISPLAY_RECORDS_DEFAULT;
    private int totalRecords;
    protected int page = 1;
    private String formLink;

    private String welcomeInputSubject;
    private String welcomeInputID;
    private String queryString;
    private StringBuffer requestUrl;

    // for APG pagination
    private String actionUrl;
    private int firstPageRecord;

    public int getMaxDisplayRecords() {
        return maxDisplayRecords;
    }

    public void setMaxDisplayRecords(int maxDisplayRecords) {
        this.maxDisplayRecords = maxDisplayRecords;
    }

    public int getTotalRecords() {
        return totalRecords;
    }

    public void setTotalRecords(int totalRecords) {
        this.totalRecords = totalRecords;
    }

    public int getPage() {
        return page;
    }

    public void setPage(int page) {
        this.page = page;
    }

    public int getTotalNumPages() {
        return (int) Math.ceil((double) totalRecords / (double) maxDisplayRecords);
    }

    public int getFirstRecord() {
        return ((page - 1) * maxDisplayRecords + 1);
    }

    public int getLastRecord() {
        return (page * maxDisplayRecords);
    }

    public int getPreviousPage() {
        return page - 1;
    }

    public int getNextPage() {
        return page + 1;
    }

    public List<Integer> getPageList() {
        int totalPages = getTotalNumPages();
        List<Integer> pageList = new ArrayList<Integer>();
        for (int i = MAXPAGELINKS / 2; i > 0; i--) {
            if (page - i > 0)
                pageList.add(page - i);
        }

        pageList.add(page);

        for (int i = 1; i <= MAXPAGELINKS / 2; i++) {
            if (page + i <= totalPages)
                pageList.add(page + i);
        }

        return pageList;
    }

    public boolean isFirstPage() {
        return page == 1;
    }

    public boolean isLastPage() {
        return page == getTotalNumPages();
    }

    public boolean getIsFirstPage() {
        return page == 1;
    }

    public boolean getIsLastPage() {
        return page == getTotalNumPages();
    }

    public boolean isElisionForHigherPages() {
        return (getTotalNumPages() - page) >= (MAXPAGELINKS + 3) / 2;
    }

    public boolean isShowLastPage() {
        return page + (MAXPAGELINKS - 1) / 2 < getTotalNumPages();
    }

    public boolean isShowFirstPage() {
        return page - (MAXPAGELINKS - 1) / 2 > 1;
    }

    public boolean isElisionForLowerPages() {
        return page > (MAXPAGELINKS + 3) / 2;
    }

    public void addRequestParameter(String name, String value) {
        formLink += "&" + name + "=" + value;
    }

    public String getFormLink() {
        return formLink;
    }

    public boolean isPaginationNeeded() {
        return totalRecords > maxDisplayRecords;
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
     * This return the full url with all query parameters inlcuded
     * but the page parameter which is set on the JSP page dynamically. 
     * @return full URL string
     */
    public String getActionUrl() {
        if (!StringUtils.isEmpty(actionUrl))
            return actionUrl;
        if (StringUtils.isEmpty(queryString))
            return "";
        // remove the page=xxx parameter
        URLCreator urlCreator = new URLCreator(requestUrl + "?" + queryString);
        urlCreator.removeNamevaluePair(PAGE);
        return urlCreator.getURL(true);

    }

    public void setActionUrl(String actionUrl) {
        this.actionUrl = actionUrl;
    }

    public int getFirstRecordOnPage() {
        return (page - 1) * maxDisplayRecords + 1;
    }

    public int getFirstPageRecord() {
        return firstPageRecord;
    }

    public void setFirstPageRecord(int firstPageRecord) {
        this.firstPageRecord = firstPageRecord;
    }

    public String getQueryString() {
        return queryString;
    }

    public void setQueryString(String queryString) {
        this.queryString = URLDecoder.decode(queryString);
    }

    public void setRequestUrl(StringBuffer requestUrl) {
        this.requestUrl = requestUrl;
    }
}

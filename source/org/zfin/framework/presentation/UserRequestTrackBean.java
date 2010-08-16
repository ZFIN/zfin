package org.zfin.framework.presentation;

import com.opensymphony.clickstream.Clickstream;

import java.util.*;

/**
 */
public class UserRequestTrackBean {

    private String showBots;
    private Clickstream clickstream;
    private Map<String, Clickstream> clickstreamMap;
    private List<ZfinRequestBean> requestBeans;
    private int indexOfRequest = -1;
    // session id
    private String sid;
    // time of a single request
    private long time;

    private String urlSearchString;

    public String getShowBots() {
        return showBots;
    }

    public void setShowBots(String showBots) {
        this.showBots = showBots;
    }

    public Clickstream getClickstream() {
        return clickstream;
    }

    public void setClickstream(Clickstream clickstream) {
        this.clickstream = clickstream;
    }

    public Map<String, Clickstream> getClickstreamMap() {
        return clickstreamMap;
    }

    public void setClickstreamMap(Map<String, Clickstream> clickstreamMap) {
        this.clickstreamMap = clickstreamMap;
    }

    public List<ZfinRequestBean> getClickStreams() {
        if (clickstreamMap == null)
            return null;

        List<ZfinRequestBean> requestBeans = new ArrayList<ZfinRequestBean>(clickstreamMap.size());
        Collections.sort(requestBeans, new ClickstreamComparator());
        for (String sessionID : clickstreamMap.keySet()) {

            requestBeans.add(new ZfinRequestBean(clickstreamMap.get(sessionID), sessionID));
        }
        return requestBeans;
    }

    public String getSid() {
        return sid;
    }

    public void setSid(String sid) {
        this.sid = sid;
    }

    public void setTime(long time) {
        this.time = time;
    }

    public long getTime() {
        return time;
    }

    public Date getSpecificTimeOfRequest(){
        if(time == 0)
            return null;

        return new Date(time);
    }

    public void setIndexOfRequest(int indexOfRequest) {
        this.indexOfRequest = indexOfRequest;
    }

    public int getIndexOfRequest() {
        return indexOfRequest;
    }

    public String getUrlSearchString() {
        return urlSearchString;
    }

    public void setUrlSearchString(String urlSearchString) {
        this.urlSearchString = urlSearchString;
    }

    class ClickstreamComparator implements Comparator<ZfinRequestBean> {

        @Override
        public int compare(ZfinRequestBean o1, ZfinRequestBean o2) {
            String userOne = ZfinJSPFunctions.getPerson(o1.getClickstream().getSession());
            String userTwo = ZfinJSPFunctions.getPerson(o2.getClickstream().getSession());
            final int userComparison = userOne.compareTo(userTwo);
            if (userComparison != 1) {
                return userComparison;
            }
            return o1.getClickstream().getStart().compareTo(o2.getClickstream().getStart());
        }

        @Override
        public boolean equals(Object obj) {
            return false;
        }
    }
}

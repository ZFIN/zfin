package org.zfin.sequence.blast.results.view;

import org.zfin.sequence.blast.presentation.DatabasePresentationBean;
import org.zfin.sequence.blast.presentation.XMLBlastBean;

import java.util.List;

/**
 * This represents the data for a single query.
 */
public class BlastResultBean {

    // database info
    private List<DatabasePresentationBean> databases;
    private int numberOfSequences;

    // query info
    private int queryLength;
    private String defLine;


    // footer details
    private String filter;

    // blast bean for settingup another blast
    private XMLBlastBean xmlBlastBean;

    // other data
    private List<String> tickets;

    // hits
    private List<HitViewBean> hits;

    public int getQueryLength() {
        return queryLength;
    }

    public void setQueryLength(int queryLength) {
        this.queryLength = queryLength;
    }

    public List<DatabasePresentationBean> getDatabases() {
        return databases;
    }

    public void setDatabases(List<DatabasePresentationBean> databases) {
        this.databases = databases;
    }

    public int getNumberOfSequences() {
        return numberOfSequences;
    }

    public void setNumberOfSequences(int numberOfSequences) {
        this.numberOfSequences = numberOfSequences;
    }

    public String getDefLine() {
        return defLine;
    }

    public void setDefLine(String defLine) {
        this.defLine = defLine;
    }

    public List<HitViewBean> getHits() {
        return hits;
    }

    public void setHits(List<HitViewBean> hits) {
        this.hits = hits;
    }

    public List<String> getTickets() {
        return tickets;
    }

    public void setTickets(List<String> tickets) {
        this.tickets = tickets;
    }

    public String getFilter() {
        return filter;
    }

    public void setFilter(String filter) {
        this.filter = filter;
    }

    public String getFilterView() {
        String returnView = this.filter;
        final String END_DASH = " -";
        if (returnView.endsWith(END_DASH)) {
            int index = returnView.lastIndexOf(END_DASH);
            if (index > 0) {
                returnView = returnView.replaceAll(END_DASH, "");
            }
        }
        return returnView;
    }

    public XMLBlastBean getXmlBlastBean() {
        return xmlBlastBean;
    }

    public void setXmlBlastBean(XMLBlastBean xmlBlastBean) {
        this.xmlBlastBean = xmlBlastBean;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        BlastResultBean that = (BlastResultBean) o;

        if (numberOfSequences != that.numberOfSequences) return false;
        if (queryLength != that.queryLength) return false;
        if (databases != null ? !databases.equals(that.databases) : that.databases != null) return false;
        if (defLine != null ? !defLine.equals(that.defLine) : that.defLine != null) return false;
        if (filter != null ? !filter.equals(that.filter) : that.filter != null) return false;
        if (hits != null ? !hits.equals(that.hits) : that.hits != null) return false;
        if (tickets != null ? !tickets.equals(that.tickets) : that.tickets != null) return false;

        return true;
    }

    @Override
    public int hashCode() {
        int result;
        result = databases != null ? databases.hashCode() : 0;
        result = 31 * result + numberOfSequences;
        result = 31 * result + queryLength;
        result = 31 * result + (defLine != null ? defLine.hashCode() : 0);
        result = 31 * result + (filter != null ? filter.hashCode() : 0);
        result = 31 * result + (tickets != null ? tickets.hashCode() : 0);
        result = 31 * result + (hits != null ? hits.hashCode() : 0);
        return result;
    }
}

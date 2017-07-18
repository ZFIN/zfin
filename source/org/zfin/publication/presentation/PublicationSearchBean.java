package org.zfin.publication.presentation;

import org.zfin.framework.presentation.PaginationBean;
import org.zfin.publication.Publication;

import java.util.List;

public class PublicationSearchBean extends PaginationBean {

    public enum YearType {
        EQUALS("equals"),
        BEFORE("before"),
        AFTER("after");

        private final String display;

        YearType(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }

    public enum Century {
        TWENTY("20"),
        NINETEEN("19"),
        EIGHTEEN("18");

        private final String display;

        Century(String display) {
            this.display = display;
        }

        public String getDisplay() {
            return display;
        }
    }

    private String author;
    private String title;
    private String journal;
    private String keywords;
    private String zdbID;
    private YearType yearType;
    private Century century;
    private String twoDigitYear;
    private Publication.Type pubType;

    private List<Publication> results;

    public String getAuthor() {
        return author;
    }

    public void setAuthor(String author) {
        this.author = author;
    }

    public String getTitle() {
        return title;
    }

    public void setTitle(String title) {
        this.title = title;
    }

    public String getJournal() {
        return journal;
    }

    public void setJournal(String journal) {
        this.journal = journal;
    }

    public String getKeywords() {
        return keywords;
    }

    public void setKeywords(String keywords) {
        this.keywords = keywords;
    }

    public String getZdbID() {
        return zdbID;
    }

    public void setZdbID(String zdbID) {
        this.zdbID = zdbID;
    }

    public YearType getYearType() {
        return yearType;
    }

    public void setYearType(YearType yearType) {
        this.yearType = yearType;
    }

    public Century getCentury() {
        return century;
    }

    public void setCentury(Century century) {
        this.century = century;
    }

    public String getTwoDigitYear() {
        return twoDigitYear;
    }

    public void setTwoDigitYear(String twoDigitYear) {
        this.twoDigitYear = twoDigitYear;
    }

    public Publication.Type getPubType() {
        return pubType;
    }

    public void setPubType(Publication.Type pubType) {
        this.pubType = pubType;
    }

    public List<Publication> getResults() {
        return results;
    }

    public void setResults(List<Publication> results) {
        this.results = results;
    }

    public boolean isEmpty() {
        return author == null && title == null && journal == null && keywords == null && zdbID == null;
    }

    @Override
    public String toString() {
        return "PublicationSearchBean{" +
                "author='" + author + '\'' +
                ", title='" + title + '\'' +
                ", journal='" + journal + '\'' +
                '}';
    }
}

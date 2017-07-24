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

    public enum Sort {
        YEAR("Pub Date"),
        AUTHOR("Author");

        private final String display;

        Sort(String display) {
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
    private String century;
    private String twoDigitYear;
    private Publication.Type pubType;
    private Sort sort;
    private Integer petFromMonth;
    private Integer petFromDay;
    private Integer petFromYear;
    private Integer petToMonth;
    private Integer petToDay;
    private Integer petToYear;
    private String curator;
    private String curationStatus;
    private String pubStatus;

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

    public String getCentury() {
        return century;
    }

    public void setCentury(String century) {
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

    public Sort getSort() {
        return sort;
    }

    public void setSort(Sort sort) {
        this.sort = sort;
    }

    public Integer getPetFromMonth() {
        return petFromMonth;
    }

    public void setPetFromMonth(Integer petFromMonth) {
        this.petFromMonth = petFromMonth;
    }

    public Integer getPetFromDay() {
        return petFromDay;
    }

    public void setPetFromDay(Integer petFromDay) {
        this.petFromDay = petFromDay;
    }

    public Integer getPetFromYear() {
        return petFromYear;
    }

    public void setPetFromYear(Integer petFromYear) {
        this.petFromYear = petFromYear;
    }

    public Integer getPetToMonth() {
        return petToMonth;
    }

    public void setPetToMonth(Integer petToMonth) {
        this.petToMonth = petToMonth;
    }

    public Integer getPetToDay() {
        return petToDay;
    }

    public void setPetToDay(Integer petToDay) {
        this.petToDay = petToDay;
    }

    public Integer getPetToYear() {
        return petToYear;
    }

    public void setPetToYear(Integer petToYear) {
        this.petToYear = petToYear;
    }

    public String getCurator() {
        return curator;
    }

    public void setCurator(String curator) {
        this.curator = curator;
    }

    public String getCurationStatus() {
        return curationStatus;
    }

    public void setCurationStatus(String curationStatus) {
        this.curationStatus = curationStatus;
    }

    public String getPubStatus() {
        return pubStatus;
    }

    public void setPubStatus(String pubStatus) {
        this.pubStatus = pubStatus;
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

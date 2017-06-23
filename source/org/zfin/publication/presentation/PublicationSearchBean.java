package org.zfin.publication.presentation;

import org.zfin.framework.presentation.PaginationBean;
import org.zfin.publication.Publication;

import java.util.List;

public class PublicationSearchBean extends PaginationBean {

    private String author;
    private String title;
    private String journal;

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

    public List<Publication> getResults() {
        return results;
    }

    public void setResults(List<Publication> results) {
        this.results = results;
    }

    public boolean isEmpty() {
        return author == null &&
                title == null &&
                journal == null;
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

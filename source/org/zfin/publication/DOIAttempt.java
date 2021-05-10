package org.zfin.publication;

/**
 */
public class DOIAttempt {
    private Long id;
    private String doi;
    private Publication publication;
    private int numAttempts;


    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public Publication getPublication() {
        return publication;
    }

    public void setPublication(Publication publication) {
        this.publication = publication;
    }

    public int getNumAttempts() {
        return numAttempts;
    }

    public void setNumAttempts(int numAttempts) {
        this.numAttempts = numAttempts;
    }

    public int addAttempt() {
        return ++numAttempts;
    }
}

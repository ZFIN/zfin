package org.zfin.wiki;

import java.util.ArrayList;
import java.util.List;

/**
 */
public class WikiSynchronizationReport {

    private List<String> createdPages = new ArrayList<String>();
    private List<String> droppedPages = new ArrayList<String>();
    private List<String> updatedPages = new ArrayList<String>();
    private List<String> errorPages = new ArrayList<String>();
    private List<String> nochangedPages = new ArrayList<String>();

    private boolean verbose = false;

    public WikiSynchronizationReport(boolean verbose) {
        this.verbose = verbose;
    }

    public WikiSynchronizationReport() {
        this(false);
    }

    public List<String> getCreatedPages() {
        return createdPages;
    }

    public void addCreatedPage(String title) {
        createdPages.add(title);
        if (verbose) System.out.print("C");
    }

    public List<String> getDroppedPages() {
        return droppedPages;
    }

    public void addDroppedPage(String title) {
        droppedPages.add(title);
        if (verbose) System.out.print("D");
    }

    public List<String> getUpdatedPages() {
        return updatedPages;
    }

    public void addUpdatedPage(String title) {
        updatedPages.add(title);
        if (verbose) System.out.print("U");
    }

    public List<String> getErrorPages() {
        return errorPages;
    }

    public void addErrorPage(String title) {
        errorPages.add(title);
        if (verbose) System.out.print("E");
    }

    public List<String> getNochangedPages() {
        return nochangedPages;
    }

    public void addNoChangePage(String title) {
        nochangedPages.add(title);
        if (verbose) System.out.print("N");
    }

    public String toString() {
        StringBuilder stringBuilder = new StringBuilder();
        stringBuilder.append(errorPages.size() + " errors encountered");
        stringBuilder.append("\n");
        stringBuilder.append(updatedPages.size() + " wiki pages updated");
        stringBuilder.append("\n");
        stringBuilder.append(createdPages.size() + " wiki pages created");
        stringBuilder.append("\n");
        stringBuilder.append(droppedPages.size() + " wiki pages dropped");
        stringBuilder.append("\n");
        stringBuilder.append(nochangedPages.size() + " pages not changed");
        stringBuilder.append("\n");
        stringBuilder.append("\n");

        for (String page : errorPages) {
            stringBuilder.append(page);
            stringBuilder.append(" had an error");
            stringBuilder.append("\n");
        }

        for (String page : updatedPages) {
            stringBuilder.append(page);
            stringBuilder.append(" updated");
            stringBuilder.append("\n");
        }

        for (String page : createdPages) {
            stringBuilder.append(page);
            stringBuilder.append(" created");
            stringBuilder.append("\n");
        }

        for (String page : droppedPages) {
            stringBuilder.append(page);
            stringBuilder.append(" dropped");
            stringBuilder.append("\n");
        }

        return stringBuilder.toString();
    }
}

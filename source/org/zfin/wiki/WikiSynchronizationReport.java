package org.zfin.wiki;

import org.apache.commons.collections.CollectionUtils;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.RootLogger;

import java.util.ArrayList;
import java.util.List;

/**
 * Report object for the antibody wiki synchronization job, records updates (or lack thereof) to each page.
 */
public class WikiSynchronizationReport {

    private final List<String> createdPages = new ArrayList<>();
    private final List<String> droppedPages = new ArrayList<>();
    private final List<String> updatedPages = new ArrayList<>();
    private final List<String> errorPages = new ArrayList<>();
    private final List<String> nochangedPages = new ArrayList<>();

    private boolean verbose = false;
    public static final Logger LOG = RootLogger.getLogger(WikiSynchronizationReport.class);

    public WikiSynchronizationReport(boolean verbose) {
        this.verbose = verbose;
    }

    public WikiSynchronizationReport() {
        this(false);
    }

    public void addCreatedPage(String title) {
        createdPages.add(title);
        if (verbose) {
            LOG.info("C " + title + ", ");
        }
    }

    public void addDroppedPage(String title) {
        droppedPages.add(title);
        if (verbose) {
            System.out.print("D" + title + ", ");
        }
    }

    public void addUpdatedPage(String title) {
        updatedPages.add(title);
        if (verbose) {
            System.out.print("U " + title + ", ");
        }
    }

    public void addErrorPage(String title) {
        errorPages.add(title);
        if (verbose) {
            System.out.print("E " + title + ", ");
        }
    }

    public void addNoChangePage(String title) {
        nochangedPages.add(title);
        if (verbose) {
            System.out.print("N " + title + ", ");
        }
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

    public List<String> getCreatedPages() {
        return createdPages;
    }

    public List<String> getDroppedPages() {
        return droppedPages;
    }

    public List<String> getUpdatedPages() {
        return updatedPages;
    }

    public List<String> getErrorPages() {
        return errorPages;
    }

    public boolean hasChanges() {
        return CollectionUtils.isNotEmpty(errorPages) ||
                CollectionUtils.isNotEmpty(createdPages) ||
                CollectionUtils.isNotEmpty(droppedPages) ||
                CollectionUtils.isNotEmpty(updatedPages);
    }
}

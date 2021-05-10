package org.zfin.ontology.datatransfer;

import java.util.Collection;

/**
 * Cron Job report contains all the important information for a given job.
 */
public class GenericCronJobReport<T extends Collection> extends CronJobReport{

    private T collection;

    public GenericCronJobReport(String jobName) {
        super(jobName);
    }

    public T getCollection() {
        return collection;
    }

    public void setCollection(T object) {
        this.collection = object;
    }
}

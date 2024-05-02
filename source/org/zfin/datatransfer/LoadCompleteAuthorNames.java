package org.zfin.datatransfer;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.hibernate.query.Query;
import org.zfin.datatransfer.webservice.NCBIEfetch;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.publication.PubmedPublicationAuthor;
import org.zfin.util.ReportGenerator;

import jakarta.persistence.Tuple;
import java.util.*;

import static org.zfin.datatransfer.webservice.NCBIEfetch.retrieveAuthorInfo;
import static org.zfin.repository.RepositoryFactory.getPublicationRepository;


@Log4j2
public class LoadCompleteAuthorNames extends AbstractValidateDataReportTask {

    int numberOfPubsUpdated;

    public LoadCompleteAuthorNames(String jobName, String propertyPath, String baseDir) {
        super(jobName, propertyPath, baseDir);
    }

    @Override
    public int execute() {
        setLoggerFile();
        setReportProperties();
        clearReportDirectory();
        runLoad();

        ReportGenerator rg = new ReportGenerator();
        rg.setReportTitle("Report for " + jobName);
        rg.addIntroParagraph("Number of publications updated with full author names: " + numberOfPubsUpdated);
//        rg.addIntroParagraph("Number of publications for which no author name info is found at PUBMED: " + numberOfPubsWithoutAuthors);
        rg.includeTimestamp();
        return 0;
    }

    private void runLoad() {
        try {
            HibernateUtil.createTransaction();
            String hql = """
                select distinct zdb_id, cast(accession_no as text)
                          from publication
                         where accession_no is not null
                           and title is not null
                           and not exists (select 1 from pubmed_publication_author
                                            where cast(accession_no as text) = ppa_pubmed_id
                                              and ppa_publication_zdb_id = zdb_id)
                                              """;
            Query<Tuple> query = HibernateUtil.currentSession().createNativeQuery(hql, Tuple.class);
            List<Tuple> idTuples = query.list();
            Map<String, String> accessionMap = new HashMap<>();
            idTuples.forEach(tuple -> accessionMap.put((String) tuple.get(1), (String) tuple.get(0)));
            log.info("Total number of accessions: " + idTuples.size());
            List<String> accessionTuple = idTuples.stream().map(tuple -> (String) tuple.get(1)).toList();

            List<NCBIEfetch.NameRecord> nameList = new ArrayList<>();
            List<List<String>> batches = ListUtils.partition(accessionTuple, 200);
            int numberOfRecordsWorkedOn = 0;
            for (List<String> batch : batches) {
                nameList.addAll(retrieveAuthorInfo(batch, accessionMap));
                numberOfRecordsWorkedOn += batch.size();
                log.info(numberOfRecordsWorkedOn);
            }

            numberOfPubsUpdated = nameList.size();

            // save all records
            nameList.forEach(nameRecord -> {
                PubmedPublicationAuthor author = new PubmedPublicationAuthor();
                author.setFirstName(nameRecord.firstName());
                author.setMiddleName(nameRecord.middleName());
                author.setLastName(nameRecord.lastName());
                author.setPubmedId(nameRecord.accession());
                author.setPublication(getPublicationRepository().getPublication(nameRecord.pubId()));
                HibernateUtil.currentSession().save(author);
            });
            HibernateUtil.flushAndCommitCurrentSession();
            LOG.info("Committed load...");
        } catch (Exception e) {
            HibernateUtil.rollbackTransaction();
            LOG.error(e);
            throw new RuntimeException(e);
        } finally {
            HibernateUtil.closeSession();
        }
    }


    public static void main(String[] args) {
        String jobName = args[2];
        LoadCompleteAuthorNames job = new LoadCompleteAuthorNames(jobName, args[0], args[1]);
        job.initDatabase();
        System.exit(job.execute());
    }
}

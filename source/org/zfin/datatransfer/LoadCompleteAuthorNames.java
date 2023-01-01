package org.zfin.datatransfer;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.hibernate.query.Query;
import org.w3c.dom.Document;
import org.w3c.dom.Element;
import org.w3c.dom.Node;
import org.w3c.dom.NodeList;
import org.xml.sax.SAXException;
import org.zfin.framework.HibernateUtil;
import org.zfin.infrastructure.ant.AbstractValidateDataReportTask;
import org.zfin.publication.PubmedPublicationAuthor;
import org.zfin.util.ReportGenerator;

import javax.persistence.Tuple;
import javax.xml.parsers.DocumentBuilder;
import javax.xml.parsers.DocumentBuilderFactory;
import javax.xml.parsers.ParserConfigurationException;
import java.io.IOException;
import java.net.URL;
import java.util.*;

import static org.zfin.repository.RepositoryFactory.getPublicationRepository;


@Log4j2
public class LoadCompleteAuthorNames extends AbstractValidateDataReportTask {

    int numberOfPubsWithoutAuthors;
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
        rg.addIntroParagraph("Number of publications for which no author name info is found at PUBMED: " + numberOfPubsWithoutAuthors);
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

            List<NameRecord> nameList = new ArrayList<>();
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
                author.setFirstName(nameRecord.firstName);
                author.setMiddleName(nameRecord.middleName);
                author.setLastName(nameRecord.lastName);
                author.setPubmedId(nameRecord.accession);
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

    public record NameRecord(
        String firstName,
        String middleName,
        String lastName,
        String pubId,
        String accession
    ) {
    }

    private List<NameRecord> retrieveAuthorInfo(List<String> accessionBatch, Map<String, String> accessionMap) {
        String ids = String.join(",", accessionBatch);
        String url = "https://eutils.ncbi.nlm.nih.gov/entrez/eutils/efetch.fcgi?db=pubmed&api_key=47c9eadd39b0bcbfac58e3e911930d143109&retmode=xml&id=" + ids;
        Set<NameRecord> nameList = new HashSet<>();

        DocumentBuilderFactory dbf = DocumentBuilderFactory.newInstance();
        DocumentBuilder db;
        try {
            db = dbf.newDocumentBuilder();
            Document doc = db.parse(new URL(url).openStream());
            NodeList articleList = doc.getElementsByTagName("PubmedArticle");
            for (int articleIndex = 0; articleIndex < articleList.getLength(); articleIndex++) {
                Node node = articleList.item(articleIndex);
                if (node.getNodeType() == Node.ELEMENT_NODE) {
                    Element element = (Element) node;
                    String accession = element.getElementsByTagName("PMID").item(0).getTextContent();
                    NodeList authors = element.getElementsByTagName("Author");
                    boolean authorsFound = false;
                    for (int index = 0; index < authors.getLength(); index++) {
                        String lastname = getNullSafeElement(element.getElementsByTagName("LastName").item(index));
                        String firstname = getNullSafeElement(element.getElementsByTagName("ForeName").item(index));
                        String middleName = getNullSafeElement(element.getElementsByTagName("Initials").item(index));
                        NameRecord record = new NameRecord(firstname, middleName, lastname, accessionMap.get(accession), accession);
                        if (lastname != null) {
                            nameList.add(record);
                            authorsFound = true;
                        }
                    }
                    if (!authorsFound) {
                        numberOfPubsWithoutAuthors++;
                    }
                }
            }
        } catch (ParserConfigurationException | IOException | SAXException e) {
            return new ArrayList<>();
        }
        return new ArrayList<>(nameList);
    }

    private String getNullSafeElement(Node node) {
        String name = null;
        if (node != null)
            name = node.getTextContent();
        return name;
    }

    public static void main(String[] args) {
        String jobName = args[2];
        LoadCompleteAuthorNames job = new LoadCompleteAuthorNames(jobName, args[0], args[1]);
        job.initDatabase();
        System.exit(job.execute());
    }
}

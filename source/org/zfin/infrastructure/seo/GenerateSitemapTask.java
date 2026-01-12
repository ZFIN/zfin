package org.zfin.infrastructure.seo;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.collections4.ListUtils;
import org.apache.commons.io.FileUtils;
import org.zfin.expression.Figure;
import org.zfin.expression.Image;
import org.zfin.mutant.Fish;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.profile.Organization;
import org.zfin.profile.Person;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.publication.Publication;

import jakarta.xml.bind.JAXBContext;
import jakarta.xml.bind.JAXBException;
import jakarta.xml.bind.Marshaller;
import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static org.zfin.repository.RepositoryFactory.*;
import static org.zfin.util.FileUtil.gzipFile;

@Log4j2
public class GenerateSitemapTask extends AbstractScriptWrapper {

    private String basedir; // to be set by ZfinPropertiesEnum.WEBROOT_DIRECTORY.value() + "/sitemaps";
    private final String sitemapSubdir = "sitemaps";
    private final XmlSitemapIndexSet xmlSitemapIndexSet = new XmlSitemapIndexSet();

    public static void main(String[] args) throws IOException {
        GenerateSitemapTask task = new GenerateSitemapTask();
        task.runTask();
    }

    private void runTask() {
        initAll();
        basedir = ZfinPropertiesEnum.WEBROOT_DIRECTORY.value() + "/" + sitemapSubdir;
        writeMarkers(getMarkersByType());
        writeZdbIDs(getFeatures(), "features", XmlUrl.Priority.MEDIUM);
        writeZdbIDs(getTerms(), "terms", XmlUrl.Priority.MEDIUM);
        writeZdbIDs(getPublications(), "publications", XmlUrl.Priority.MEDIUM);
        writeZdbIDs(getFish(), "fish", XmlUrl.Priority.MEDIUM);
        writeZdbIDs(getFigures(), "figures", XmlUrl.Priority.MEDIUM);
        writeZdbIDs(getImages(), "images", XmlUrl.Priority.MEDIUM);
        writeZdbIDs(getPeople(), "people", XmlUrl.Priority.MEDIUM);
        writeZdbIDs(getOrganizations(), "organizations", XmlUrl.Priority.MEDIUM);
        writeZdbIDs(getZfishBookPages(), "zfishbook", XmlUrl.Priority.MEDIUM);
        writeZdbIDs(getMiscellaneousPages(), "miscellaneous", XmlUrl.Priority.MEDIUM);

        outputIndex(basedir + "/sitemap-index.xml", xmlSitemapIndexSet);
    }

    private Map<String, List<String>> getMarkersByType() {
        List<String> markers = getMarkerRepository().getAllMarkers();
        Pattern pattern = Pattern.compile("ZDB-(.*)-\\d{6}-\\d+");
        Map<String, List<String>> markersByType = new HashMap<>();

        for (String marker : markers) {
            Matcher matcher = pattern.matcher(marker);
            if (matcher.find()) {
                String markerType = matcher.group(1);
                markersByType.computeIfAbsent(markerType, k -> new ArrayList<>()).add(marker);
            }
        }
        return markersByType;
    }

    private List<String> getFeatures() {
        return getFeatureRepository().getAllFeatures(0);
    }

    private List<String> getTerms() {
        return getOntologyRepository().getAllTerms(0);
    }

    private List<String> getPublications() {
        return getPublicationRepository().getAllPublications().stream().map(Publication::getZdbID).toList();
    }

    private List<String> getFish() {
        return getFishRepository().getAllFish(0).stream().map(Fish::getZdbID).toList();
    }

    private List<String> getFigures() {
        return getFigureRepository().getAllFigures().stream().map(Figure::getZdbID).toList();
    }

    private List<String> getImages() {
        List<Image> images = getFigureRepository().getAllImagesWithFigures();
        return images.stream().map(Image::getZdbID).toList();
    }

    private List<String> getPeople() {
        return getProfileRepository().getAllPeople().stream().map(Person::getZdbID).toList();
    }

    private List<String> getOrganizations() {
        return getProfileRepository().getAllOrganizations().stream().map(Organization::getZdbID).toList();
    }

    private List<String> getZfishBookPages() {
        File jspLocalFileLocation = new File(ZfinPropertiesEnum.SOURCEROOT.value(), "home/WEB-INF/jsp/zf_info");
        return FileUtils.listFiles(jspLocalFileLocation, new String[]{"jsp"}, false).stream()
                .map(f -> "zf_info/" + f.getName())
                .map(f -> f.replaceAll("--", "/").replace(".jsp", ""))
                .map(f -> f + ".html")
                .toList();
    }

    private List<String> getMiscellaneousPages() {
        //downloads, search, links from header and footer
        return Arrays.asList("action/antibody/search",
                "action/blast/blast",
                "action/expression/search",
                "action/feature/line-designations",
                "action/feature/wildtype-list",
                "action/fish/search",
                "action/infrastructure/annual-stats-view",
                "action/marker/search",
                "action/nomenclature/gene-name",
                "action/nomenclature/line-name",
                "action/ontology/search",
                "action/profile/company/search",
                "action/profile/lab/search",
                "action/profile/person/search",
                "action/profile/person/submit",
                "action/publication/search",
                "action/submit-data",
                "action/zebrashare",
                "downloads",
                "jbrowse/?data=data/GRCz11",
                "schemaSpy/index.html",
                "search",
                "" //root of the site
        );
    }

    private void writeMarkers(Map<String, List<String>> markers) {
        for (Map.Entry<String, List<String>> entry : markers.entrySet()) {
            String markerType = entry.getKey();
            writeZdbIDs(entry.getValue(), "markers-" + markerType, XmlUrl.Priority.MEDIUM);
        }
    }

    private void writeZdbIDs(List<String> zdbIDs, String filePrefix, XmlUrl.Priority priority) {
        List<List<String>> partitions = ListUtils.partition(zdbIDs, 50_000);

        int index = 0;
        for (List<String> markerList : partitions) {
            index++;
            XmlUrlSet xmlUrlSet = new XmlUrlSet(siteUrl());
            xmlUrlSet.addAll(markerList, priority);

            log.debug("Writing sitemap for " + filePrefix + "-" + index + ".xml with " + markerList.size() + " entries");
            outputSitemap(filePrefix + "-" + index + ".xml", xmlUrlSet);
        }
    }

    private String siteUrl() {
        return "" + ZfinPropertiesEnum.SECURE_HTTP + ZfinPropertiesEnum.DOMAIN_NAME;
    }

    private String siteUrl(String path) {
        return siteUrl() + "/" + path;
    }

    private void outputSitemap(String relativePath, XmlUrlSet xmlUrlSet) {
        File basedirFile = new File(basedir);
        if (!basedirFile.exists()) {
            basedirFile.mkdirs();
        }
        String path = basedir + "/" + relativePath;


        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(XmlUrlSet.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Output the XML to a file
            marshaller.marshal(xmlUrlSet, new File(path));
            System.out.println("Sitemap has been generated at: " + path + " with " + xmlUrlSet.size() + " entries");
            gzipFile(path, true);
        } catch (JAXBException | IOException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }

        xmlSitemapIndexSet.add(siteUrl(sitemapSubdir + "/" + relativePath + ".gz"));
    }

    private void outputIndex(String path, XmlSitemapIndexSet xmlSitemapIndexSet) {
        try {
            JAXBContext jaxbContext = JAXBContext.newInstance(XmlSitemapIndexSet.class);
            Marshaller marshaller = jaxbContext.createMarshaller();
            marshaller.setProperty(Marshaller.JAXB_FORMATTED_OUTPUT, true);

            // Output the XML to a file
            marshaller.marshal(xmlSitemapIndexSet, new File(path));
            System.out.println("Sitemap index has been generated at: " + path);
        } catch (JAXBException e) {
            e.printStackTrace();
            throw new RuntimeException(e);
        }
    }
}

package org.zfin.uniprot;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.io.FileUtils;
import org.biojava.bio.BioException;
import org.zfin.ontology.datatransfer.AbstractScriptWrapper;
import org.zfin.properties.ZfinPropertiesEnum;
import org.zfin.uniprot.history.UniProtRelease;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;
import java.util.stream.Stream;

import static org.zfin.datatransfer.service.DownloadService.*;
import static org.zfin.framework.HibernateUtil.currentSession;
import static org.zfin.repository.RepositoryFactory.getInfrastructureRepository;
import static org.zfin.uniprot.UniProtReleaseDiffTask.combineAndFilterInputFileSet;
import static org.zfin.uniprot.UniProtReleaseDiffTask.combineAndFilterInputPathSet;

/**
 * This class is used to check for new releases of uniprot.
 *
 */
@Log4j2
public class UniProtReleaseCheckTask extends AbstractScriptWrapper {

    private static final String COMBINED_FILE_NAME = "pre_zfin.dat";

    private String downloadUrlForTremblFile = "http://127.0.0.1:8080/uniprot_trembl_vertebrates.dat.gz";
    private String downloadUrlForSprotFile = "http://127.0.0.1:8080/uniprot_sprot_vertebrates.dat.gz";
    private String downloadDestinationForUniProtReleases = "/opt/research/zarchive/load_files/UniProt-archive";

    private Path downloadedFile1;
    private Path downloadedFile2;
    private Path downloadedDirectory;

    public static void main(String[] args) throws Exception {
        UniProtReleaseCheckTask task = new UniProtReleaseCheckTask();
        task.runTask();
    }

    public void runTask() throws IOException, ParseException, BioException, SQLException {
        initAll();
        initIO();
        sanityCheck();

        Date newReleaseDate = fetchNewReleaseDate();
        if (newReleaseDate == null) {
            log.info("No new release found.");
            return;
        }

        handleNewRelease(newReleaseDate);
    }

    private Date fetchNewReleaseDate() throws IOException {
        Date releaseDate = getLatestReleaseTimestamp();
        UniProtRelease up = getInfrastructureRepository().getUniProtReleaseByDate(releaseDate);
        if (up == null) {
            log.debug("No existing release saved at ZFIN for date: " + releaseDate);
            return releaseDate;
        }
        log.debug("Found existing entry in DB with release date: " + releaseDate);
        log.debug("path: " + up.getPath());

        if (up.getDownloadDate() == null) {
            log.debug("Download date is null.  Assuming this is a failed download.");
            return releaseDate;
        }

        return null;
    }

    private void handleNewRelease(Date releaseDate) throws IOException {
        recordReleaseFoundInDB(releaseDate);
        downloadFiles(releaseDate);
        Path combinedFiles = processFiles();
        String md5 = calculateMd5(combinedFiles);
        recordReleaseInDB(releaseDate, combinedFiles, md5);
    }

    private static void recordReleaseFoundInDB(Date releaseDate) {
        log.info("New release found: " + new SimpleDateFormat("yyyy-MM-dd").format(releaseDate));
        UniProtRelease existingRecord = getInfrastructureRepository().getUniProtReleaseByDate(releaseDate);
        if (existingRecord != null) {
            log.info("Release already recorded in database.");
            return;
        }

        currentSession().beginTransaction();
        UniProtRelease release = new UniProtRelease();
        release.setDate(releaseDate);
        getInfrastructureRepository().insertUniProtRelease(release);
        currentSession().getTransaction().commit();
    }

    private void downloadFiles(Date releaseDate) {
        String url1 = downloadUrlForTremblFile;
        String fileName1 = url1.substring(url1.lastIndexOf('/') + 1);

        String url2 = downloadUrlForSprotFile;
        String fileName2 = url2.substring(url2.lastIndexOf('/') + 1);

        //set class properties
        downloadedDirectory = createPathForDownloadDestination(releaseDate);
        downloadedFile1 = downloadedDirectory.resolve(fileName1);
        downloadedFile2 = downloadedDirectory.resolve(fileName2);

        //download the files
        downloadFileIfNotExists(url1, downloadedFile1);
        downloadFileIfNotExists(url2, downloadedFile2);

    }

    private Path processFiles() throws IOException {
        log.info("Processing files to create " + COMBINED_FILE_NAME + " ...");
        Path destinationPath = downloadedDirectory.resolve(COMBINED_FILE_NAME);
        combineAndFilterInputPathSet(
                List.of(downloadedFile1, downloadedFile2),
                destinationPath );
        return destinationPath;
    }

    private static String calculateMd5(Path combinedFiles) throws IOException {
        log.info("Calculating md5...");
        String md5 = DigestUtils.md5Hex(FileUtils.openInputStream(combinedFiles.toFile()));
        log.info("MD5: " + md5);
        return md5;
    }

    private void recordReleaseInDB(Date releaseDate, Path combinedFiles, String md5) {
        long size = combinedFiles.toFile().length();
        log.info("File size: " + size);
        String notes = generateReleaseNotes(releaseDate);
        Path relativePath = Paths.get(downloadDestinationForUniProtReleases).relativize(combinedFiles);
        recordReleaseProcessedInDB(releaseDate, relativePath, md5, size, notes);
    }

    private String generateReleaseNotes(Date releaseDate) {
        String notes = "Release " + releaseDate + " downloaded from: \n" +
                downloadUrlForTremblFile + " ( " + downloadedFile1.toFile().length() + " bytes ) and \n" +
                downloadUrlForSprotFile + " ( " + downloadedFile2.toFile().length() + " bytes )";
        return notes;
    }

    private static void recordReleaseProcessedInDB(Date releaseDate, Path relativeDownloadDirectory, String md5, long size, String notes) {
        log.info("Recording release in database...");
        currentSession().beginTransaction();
        UniProtRelease release = getInfrastructureRepository().getUniProtReleaseByDate(releaseDate);
        if (release == null) {
            release = new UniProtRelease();
        }

        release.setDate(releaseDate);
        release.setPath(relativeDownloadDirectory.toString());
        release.setMd5(md5);
        release.setSize(size);
        release.setDownloadDate(new Date());
        release.setNotes(notes);
        getInfrastructureRepository().upsertUniProtRelease(release);
        currentSession().getTransaction().commit();
    }

    private void downloadFileIfNotExists(String url, Path localFile) {
        try {
            log.info("Downloading file: " + url + " to " + localFile);
            log.info("   exists? " + localFile.toFile().exists());
            downloadFileViaWget(url, localFile, 3_600_000, log);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }


    private Path createPathForDownloadDestination(Date releaseDate) {
        String timestamp = new SimpleDateFormat("yyyy-MM-dd").format(releaseDate);

        System.out.println("Saving to directory: " + downloadDestinationForUniProtReleases);

        Path directoryDestination = Paths.get(
                downloadDestinationForUniProtReleases,
                timestamp);

        //make directory if it doesn't exist
        File directory = directoryDestination.toFile();
        if (!directory.exists()) {
            directory.mkdirs();
        } else {
            log.error("Directory already exists: " + directoryDestination);
        }

        if (!directory.isDirectory()) {
            throw new RuntimeException("Destination is not a directory: " + directoryDestination);
        }

        return directoryDestination;
    }

    private Date getLatestReleaseTimestamp() throws IOException {
        String url = downloadUrlForTremblFile;
        if (url == null) {
            throw new RuntimeException("No URL found for uniprot release file.");
        }

        Date lastModified = getLastModifiedOnServer(new URL(url));

        //if before 2023, throw exception
        Calendar cal = Calendar.getInstance();
        cal.setTime(lastModified);
        if (cal.get(Calendar.YEAR) < 2021) {
            throw new RuntimeException("Release date of " + cal.get(Calendar.YEAR) + " is before 2023.  This is not allowed.");
        }

        return lastModified;
    }

    private void initIO() {
        if (this.downloadDestinationForUniProtReleases == null) {
            this.downloadDestinationForUniProtReleases = ZfinPropertiesEnum.UNIPROT_RELEASE_ARCHIVE_DIR.value();
        }

        if (this.downloadUrlForTremblFile != null && this.downloadUrlForSprotFile != null) {
            return;
        }

        List<String> tremblUrlsToTry = Stream.of(
                ZfinPropertiesEnum.UNIPROT_TREMBL_FILE_URL.value(),
                ZfinPropertiesEnum.UNIPROT_TREMBL_FILE_URL_ALT1.value(),
                ZfinPropertiesEnum.UNIPROT_TREMBL_FILE_URL_ALT2.value(),
                ZfinPropertiesEnum.UNIPROT_TREMBL_FILE_URL_ALT3.value()
        ).toList();
        List<String> sprotUrlsToTry = Stream.of(
                ZfinPropertiesEnum.UNIPROT_SPROT_FILE_URL.value(),
                ZfinPropertiesEnum.UNIPROT_SPROT_FILE_URL_ALT1.value(),
                ZfinPropertiesEnum.UNIPROT_SPROT_FILE_URL_ALT2.value(),
                ZfinPropertiesEnum.UNIPROT_SPROT_FILE_URL_ALT3.value()
        ).toList();
        if (this.downloadUrlForTremblFile == null && this.downloadUrlForSprotFile == null) {
            for (int i = 0; i < tremblUrlsToTry.size(); i++) {
                try {
                    this.downloadUrlForTremblFile = tremblUrlsToTry.get(i);
                    this.downloadUrlForSprotFile = sprotUrlsToTry.get(i);
                    getFileSizeOnServer(new URL(downloadUrlForTremblFile));
                    return;
                } catch (IOException e) {
                    log.debug("Could not get file size for URL: " + downloadUrlForTremblFile);
                }
            }
        }
        throw new RuntimeException("Could not find a valid URL for uniprot release file.");
    }
    public void sanityCheck() {
        log.debug("Using URLS: " + downloadUrlForTremblFile + " and " + downloadUrlForSprotFile);
        log.debug("Saving to directory: " + downloadDestinationForUniProtReleases);
        if (downloadUrlForTremblFile == null || downloadUrlForSprotFile == null) {
            throw new RuntimeException("No URL found for uniprot release file.");
        }
        if (downloadDestinationForUniProtReleases == null) {
            throw new RuntimeException("No directory found for uniprot release file.");
        }
    }
}

package org.zfin.datatransfer.ncbi;

import lombok.Setter;
import lombok.extern.log4j.Log4j2;

import org.apache.commons.io.IOUtils;
import org.zfin.framework.exec.ExecProcess;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.List;
import java.util.Optional;

import static java.lang.System.getProperty;
import static org.zfin.datatransfer.service.DownloadService.downloadFileViaWget;

@Setter
@Log4j2
public class NCBIReleaseFetcher {
    public static final String DEFAULT_RELEASE_NUMBER_PATH = "/refseq/release/RELEASE_NUMBER";
    public static final String DEFAULT_URL_BASE = "https://ftp.ncbi.nlm.nih.gov";
    public String releaseUrl;
    public String urlBase;

    public Optional<Integer> getCurrentReleaseNumber() {
        //make http request to releaseUrl
        try {
            String url = getReleaseUrl();
            String contents = IOUtils.toString(new URL(url), StandardCharsets.UTF_8);
            if (contents.isEmpty()) {
                return Optional.empty();
            }
            contents = contents.trim();
            if (contents.length() > 7) {
                return Optional.empty();
            }
            Integer release = Integer.parseInt(contents);
            if (release > 0) {
                return Optional.of(release);
            }
        } catch (IOException e) {
            log.error("Could not fetch release number", e);
        }
        return Optional.empty();
    }

    public NCBIReleaseFileReader downloadLatestReleaseFileSetReader(File downloadDirectory) {
        NCBIReleaseFileSet fileset = downloadLatestReleaseFileSet(downloadDirectory);
        return new NCBIReleaseFileReader(fileset);
    }

    public NCBIReleaseFileSet downloadLatestReleaseFileSet(File directory) {
        Optional<Integer> release = getCurrentReleaseNumber();
        if (release.isEmpty()) {
            throw new RuntimeException("Could not fetch release number");
        }
        try {
            log.info("Downloading release " + release.get() + " files");
            return downloadReleaseFiles(directory, release.get());
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    public NCBIReleaseFileSet downloadReleaseFiles(File destination, Integer releaseNum) throws IOException {
        if (releaseNum == null) {
            throw new IllegalArgumentException("Release number cannot be null for catalog");
        }
        if (destination == null) {
            destination = Files.createTempDirectory(getProperty("java.io.tmpdir") + "/ncbi-release").toFile();
        }
        if (!destination.exists()) {
            destination.mkdirs();
        }
        if (!destination.isDirectory()) {
            throw new IllegalArgumentException("Destination must be a directory");
        }

        NCBIReleaseFileSet fileset = new NCBIReleaseFileSet();
        fileset.setReleaseNumber(releaseNum);
        for(NCBIReleaseFileSet.FileName fileName : NCBIReleaseFileSet.FileName.values()) {
            File file = downloadReleaseFile(fileName, new File(destination, fileName.getFileName()), releaseNum);
            fileset.setFile(fileName, file);
        }
        return fileset;
    }

    public File downloadReleaseFile(NCBIReleaseFileSet.FileName fileName, File destination, Integer releaseNum) throws IOException {
        if (releaseNum == null && fileName == NCBIReleaseFileSet.FileName.REFSEQ_CATALOG) {
            throw new IllegalArgumentException("Release number cannot be null for catalog");
        }

        String path = switch (fileName) {
            case GENE2ACCESSION -> "/gene/DATA/gene2accession.gz";
            case REFSEQ_CATALOG -> "/refseq/release/release-catalog/RefSeq-release" + releaseNum + ".catalog.gz";
            case GENE2VEGA -> "/gene/DATA/ARCHIVE/gene2vega.gz";
            case ZF_GENE_INFO -> "/gene/DATA/GENE_INFO/Non-mammalian_vertebrates/Danio_rerio.gene_info.gz";
        };
        String url = getUrlBase() + path;

        //download file
        if (destination == null) {
            destination = Files.createTempFile(Path.of(getProperty("java.io.tmpdir")), fileName.getFileName(), ".gz").toFile();
        }
        downloadFileViaWget(url, destination.toPath(), 3_600_000, log);

        //post-process file
        postProcessFile(fileName, destination);

        return destination;
    }

    private void postProcessFile(NCBIReleaseFileSet.FileName fileName, File destination) throws IOException {
        try {
            if (fileName == NCBIReleaseFileSet.FileName.REFSEQ_CATALOG) {
                processGrepShellCommand(destination, "'Danio rerio'");
            }
            if (fileName == NCBIReleaseFileSet.FileName.GENE2ACCESSION) {
                processGrepShellCommand(destination, "-E '7955|tax_id'");
            }
            if (fileName == NCBIReleaseFileSet.FileName.GENE2VEGA) {
                processGrepShellCommand(destination, "-E '7955|tax_id'");
            }
            if (fileName == NCBIReleaseFileSet.FileName.ZF_GENE_INFO) {
                processGrepShellCommand(destination, "-E '7955|tax_id'");
            }
        } catch (InterruptedException e) {
            throw new IOException(e);
        }
    }

    private void processGrepShellCommand(File source, String grepPattern) throws IOException, InterruptedException {
        //source is a gzipped file
        //this performs equivalent of:
        //cat source | gunzip -d | grep SomeString | gzip -c > tmpfile ; mv tmpfile source
        File tempFile = Files.createTempFile(Path.of(getProperty("java.io.tmpdir")), "temp-download-", ".gz").toFile();
        List<String> commands = List.of(
                "/bin/bash",
                "-c",
                String.format("gunzip -c %1$s | grep %2$s | gzip -c > %3$s ; mv %3$s %1$s",
                        source.getAbsolutePath(), grepPattern, tempFile.getAbsolutePath()));
        ExecProcess.execList(commands, false);

    }

    private String getReleaseUrl() {
        if (releaseUrl == null) {
            return getUrlBase() + DEFAULT_RELEASE_NUMBER_PATH;
        }
        return releaseUrl;
    }

    private String getUrlBase() {
        if (urlBase == null) {
            if (System.getenv("NCBI_URL_BASE") != null) {
                return System.getenv("NCBI_URL_BASE");
            }
            return DEFAULT_URL_BASE;
        }
        return urlBase;
    }
}

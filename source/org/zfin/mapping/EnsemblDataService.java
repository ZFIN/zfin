package org.zfin.mapping;

import com.google.gson.Gson;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.springframework.stereotype.Service;
import org.zfin.marker.Marker;
import org.zfin.marker.service.MarkerService;

import java.io.*;
import java.net.HttpURLConnection;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;

@Service
public class EnsemblDataService implements GenomeBrowserDataService {

    private static final String server = "http://beta.rest.ensembl.org";

    private Logger LOG = LogManager.getLogger(EnsemblDataService.class);

    public static void main(String[] args) throws Exception {
        String ext = "/assembly/info/homo_sapiens?";
        EnsemblDataService service = new EnsemblDataService();
        System.out.println(service.getInfo(ext));
    }

    @Override
    public GenomeBrowserMetaData getGenomeBrowserMetaData() {
        GenomeBrowserMetaData data = new GenomeBrowserMetaData();
        data.setBuild(getBuild());
        data.setRelease(getRelease());
        return data;
    }

    @Override
    public String getBuild() {
        String ext = "/assembly/info/danio_rerio";
        StringBuilder builder = getInfo(ext + JSON_TYPE);
        Gson gson = new Gson();
        Build release = gson.fromJson(builder.toString(), Build.class);
        return release.getAssembly_name();
    }

    private static final String JSON_TYPE = "?content-type=application/json";

    @Override
    public String getRelease() {
        String ext = "/info/software";
        StringBuilder builder = getInfo(ext + JSON_TYPE);
        Gson gson = new Gson();
        Release release = gson.fromJson(builder.toString(), Release.class);
        return release.getRelease();
    }

    @Override
    public GenomeLocation getGenomeLocation(Marker marker) {
        String ensemblAccession = MarkerService.getEnsemblAccessionId(marker);
        if (ensemblAccession != null)
            return getGenomeLocation(ensemblAccession);
        else
            return null;
    }

    private StringBuilder getInfo(String restURI) {
        URL url = null;
        String restURL = server + restURI;
        try {
            url = new URL(restURL);
        } catch (MalformedURLException e) {
            LOG.error("Cannot connect to " + restURL, e);
        }

        URLConnection connection = null;
        try {
            connection = url.openConnection();
        } catch (IOException e) {
            LOG.error(e);
        }
        HttpURLConnection httpConnection = (HttpURLConnection) connection;
        //connection.setRequestProperty("Content-Type", "application/json");

        StringBuilder builder = null;
        try {
            InputStream response = connection.getInputStream();
            int responseCode = httpConnection.getResponseCode();

            if (responseCode != 200) {
                throw new RuntimeException("Response code was not 200. Detected response was " + responseCode);
            }

            Reader reader = null;
            try {
                reader = new BufferedReader(new InputStreamReader(response, "UTF-8"));
                builder = new StringBuilder();
                char[] buffer = new char[8192];
                int read;
                while ((read = reader.read(buffer, 0, buffer.length)) > 0) {
                    builder.append(buffer, 0, read);
                }
            } finally {
                if (reader != null) try {
                    reader.close();
                } catch (IOException logOrIgnore) {
                    logOrIgnore.printStackTrace();
                }
            }
        } catch (IOException e) {
            LOG.error("Could not find resource ", e);
        }

        return builder;
    }

    public GenomeLocation getGenomeLocation(String geneID) {
        String ext = "/lookup/id/" + geneID;
        StringBuilder builder = getInfo(ext + JSON_TYPE);
        Gson gson = new Gson();
        EnsemblGeneLocation release = gson.fromJson(builder.toString(), EnsemblGeneLocation.class);
        GenomeLocation genomeLocation = new GenomeLocation();
        genomeLocation.setChromosome(release.getChromsome());
        genomeLocation.setStart(Integer.parseInt(release.getStart(), 10));
        genomeLocation.setEnd(Integer.parseInt(release.getEnd(), 10));
        genomeLocation.setMetaData(getGenomeBrowserMetaData());
        return genomeLocation;

    }

    class Release {
        private String release;

        String getRelease() {
            return release;
        }

        void setRelease(String release) {
            this.release = release;
        }
    }

    class Build {
        private String assembly_name;

        String getAssembly_name() {
            return assembly_name;
        }

        void setAssembly_name(String assembly_name) {
            this.assembly_name = assembly_name;
        }
    }

    public class EnsemblGeneLocation {

        private String start;
        private String end;
        private String seq_region_name;

        public String getChromsome() {
            return seq_region_name;
        }

        public void setChromsome(String chromsome) {
            this.seq_region_name = chromsome;
        }

        public String getEnd() {
            return end;
        }

        public void setEnd(String end) {
            this.end = end;
        }

        public String getStart() {
            return start;
        }

        public void setStart(String start) {
            this.start = start;
        }
    }

}

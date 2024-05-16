package org.zfin.datatransfer.ncbi;

import lombok.Setter;
import org.apache.commons.io.IOUtils;

import java.io.IOException;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.util.Optional;

@Setter
public class NCBIReleaseFetcher {
    public static final String DEFAULT_URL = "https://ftp.ncbi.nlm.nih.gov/refseq/release/RELEASE_NUMBER";
    public String releaseUrl;

    public Optional<Integer> getCurrentRelease() {
        //make http request to releaseUrl
        try {
            String contents = IOUtils.toString(new URL(getReleaseUrl()), StandardCharsets.UTF_8);
            Integer release = Integer.parseInt(contents.trim());
            if (release > 0) {
                return Optional.of(release);
            } else {
                return Optional.empty();
            }
        } catch (IOException e) {
            return Optional.empty();
        }
    }

    public String getReleaseUrl() {
        if (releaseUrl == null) {
            return DEFAULT_URL;
        }
        return releaseUrl;
    }
}

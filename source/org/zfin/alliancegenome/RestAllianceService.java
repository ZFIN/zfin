package org.zfin.alliancegenome;

import lombok.extern.log4j.Log4j2;

import java.io.IOException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Base64;

@Log4j2
public class RestAllianceService {

    public static final String token = Base64.getEncoder().encodeToString(
            getToken().getBytes(StandardCharsets.UTF_8));

    private static String getToken() {
        Path file = Path.of("server_apps/DB_maintenance/Alliance/apiToken.txt");
        try {
            return Files.readString(file);
        } catch (IOException e) {
            log.error("Could not find token file: " + file);
        }
        return null;
    }


}

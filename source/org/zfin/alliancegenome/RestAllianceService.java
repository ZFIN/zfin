package org.zfin.alliancegenome;

import lombok.extern.log4j.Log4j2;
import org.apache.commons.lang3.StringUtils;
import org.zfin.properties.ZfinPropertiesEnum;

import java.io.IOException;
import java.io.UnsupportedEncodingException;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.Objects;

@Log4j2
public class RestAllianceService {

    public static String token;

    static {
        try {
            token = new String(Objects.requireNonNull(getToken(), "Please Provide API Token for Alliance").getBytes("ISO8859-1"), StandardCharsets.UTF_8);
        } catch (UnsupportedEncodingException e) {
            e.printStackTrace();
        }
    }

    private static String getToken() {
        Path file = Path.of(ZfinPropertiesEnum.TARGETROOT.value() + "/server_apps/DB_maintenance/Alliance/apiToken.txt");
        try {
            return StringUtils.chomp(Files.readString(file));
        } catch (IOException e) {
            log.error("Could not find token file: " + file);
        }
        return null;
    }


}

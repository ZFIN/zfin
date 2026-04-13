package org.zfin.alliancegenome;

import lombok.extern.log4j.Log4j2;
import org.zfin.infrastructure.TokenStorage;

import java.util.Optional;

@Log4j2
public class RestAllianceService {

    public static String token;

    static {
        TokenStorage tokenStorage = new TokenStorage();
        Optional<String> value = tokenStorage.getValue(TokenStorage.ServiceKey.ALLIANCE_API_TOKEN);
        if (value.isPresent()) {
            token = value.get();
        } else {
            log.error("Could not find Alliance API token. Use: gradle tokenStorage --args='write ALLIANCE_API_TOKEN <token>'");
            token = null;
        }
    }

}

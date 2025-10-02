package org.zfin.infrastructure;

import java.io.IOException;
import java.util.Optional;

public interface TokenStorageInterface {
    Optional<String> getValue(TokenStorage.ServiceKey serviceKey) ;
    public void setValue(TokenStorage.ServiceKey serviceKey, String value) throws IOException;
    public void deleteValue(TokenStorage.ServiceKey serviceKey) ;
}

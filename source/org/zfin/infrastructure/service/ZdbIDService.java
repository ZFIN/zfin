package org.zfin.infrastructure.service;

import org.springframework.stereotype.Service;
import org.zfin.infrastructure.repository.InfrastructureRepository;
import org.zfin.repository.RepositoryFactory;

@Service
public class ZdbIDService {
    public Boolean isActiveZdbID(String zdbID) {
        InfrastructureRepository ir = RepositoryFactory.getInfrastructureRepository();

        if (ir.getActiveData(zdbID) != null
                || ir.getActiveSource(zdbID) != null) {
            return true;
        }
        return false;
    }

}

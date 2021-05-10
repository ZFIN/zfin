package org.zfin.marker.service;

import org.springframework.stereotype.Service;
import org.zfin.infrastructure.ActiveData;
import org.zfin.infrastructure.ActiveSource;
import org.zfin.infrastructure.delete.DeleteEntityRule;

@Service
public class DeleteService {

    public DeleteEntityRule getDeleteRule(String zdbID) {
        ActiveData.Type dataType = ActiveData.getType(zdbID);
        if (dataType != null)
            return dataType.getDeleteEntityRule(zdbID);
        ActiveSource.Type type = ActiveSource.validateID(zdbID);
        return type.getDeleteEntityRule(zdbID);
    }
}

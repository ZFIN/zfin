package org.zfin.sequence.repository;

import org.zfin.sequence.DisplayGroup;
import org.zfin.sequence.ReferenceDatabase;

import java.util.List;

/**
 */
public interface DisplayGroupRepository {
    // for display groups
    DisplayGroup getDisplayGroupByName(DisplayGroup.GroupName groupName) ;
    List<ReferenceDatabase> getReferenceDatabasesForDisplayGroup(DisplayGroup.GroupName... groupName) ;
}

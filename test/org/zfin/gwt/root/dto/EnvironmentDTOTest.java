package org.zfin.gwt.root.dto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * Test class for ExperimentDTO.
 */
@SuppressWarnings({"FeatureEnvy"})
public class EnvironmentDTOTest  {

    private List<ExperimentDTO> environmentList = new ArrayList<ExperimentDTO>(5);

    /**
     * Standard before hot
     */
    @Test
    public void compareEnvironmentSorting(){
        environmentList.add(createEnvironmentDTOObject("ypsilon"));
        environmentList.add(createEnvironmentDTOObject(ExperimentDTO.STANDARD));
        environmentList.add(createEnvironmentDTOObject("abc"));
        environmentList.add(createEnvironmentDTOObject(ExperimentDTO.GENERIC_CONTROL));
        environmentList.add(createEnvironmentDTOObject("hot"));
        Collections.sort(environmentList);
        assertEquals(ExperimentDTO.STANDARD, environmentList.get(0).getName());
        assertEquals(ExperimentDTO.GENERIC_CONTROL, environmentList.get(1).getName());
        assertEquals("abc", environmentList.get(2).getName());
        assertEquals("hot", environmentList.get(3).getName());
    }

    private ExperimentDTO createEnvironmentDTOObject(String name){
        ExperimentDTO environment = new ExperimentDTO();
        environment.setName(name);
        return environment;
    }
}
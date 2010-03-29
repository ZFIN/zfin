package org.zfin.gwt.root.dto;

import org.junit.Test;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static junit.framework.Assert.assertEquals;

/**
 * Test class for EnvironmentDTO.
 */
@SuppressWarnings({"FeatureEnvy"})
public class EnvironmentDTOTest  {

    private List<EnvironmentDTO> environmentList = new ArrayList<EnvironmentDTO>(5);

    /**
     * Standard before hot
     */
    @Test
    public void compareEnvironmentSorting(){
        environmentList.add(createEnvironmentDTOObject("ypsilon"));
        environmentList.add(createEnvironmentDTOObject(EnvironmentDTO.STANDARD));
        environmentList.add(createEnvironmentDTOObject("abc"));
        environmentList.add(createEnvironmentDTOObject(EnvironmentDTO.GENERIC_CONTROL));
        environmentList.add(createEnvironmentDTOObject("hot"));
        Collections.sort(environmentList);
        assertEquals(EnvironmentDTO.STANDARD, environmentList.get(0).getName());
        assertEquals(EnvironmentDTO.GENERIC_CONTROL, environmentList.get(1).getName());
        assertEquals("abc", environmentList.get(2).getName());
        assertEquals("hot", environmentList.get(3).getName());
    }

    private EnvironmentDTO createEnvironmentDTOObject(String name){
        EnvironmentDTO environment = new EnvironmentDTO();
        environment.setName(name);
        return environment;
    }
}
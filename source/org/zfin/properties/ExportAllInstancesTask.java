package org.zfin.properties;

import org.apache.log4j.Logger;
import org.apache.tools.ant.Target;
import org.apache.tools.ant.Task;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

/**
 */
public class ExportAllInstancesTask extends Task {

    private final Logger logger = Logger.getLogger(ExportAllInstancesTask.class) ;

    private final static String COMMENT = "#" ;
    private final static String INSTANCE = "INSTANCE" ;
    private final static String DEFAULT_TARGET = "createPropertiesFiles" ;
    private String instancesFile;
    private String target = DEFAULT_TARGET;

    @Override
    public void execute(){
        try {
            List<String> instances = getInstances() ;
            for(String instance : instances) {
                logger.info("exporting properties for: " + instance);
                getProject().setProperty(INSTANCE,instance);
                ((Target) getProject().getTargets().get(target)).execute();
            }
        } catch (IOException e) {
            logger.error(e);
        }
    }

    private List<String> getInstances() throws IOException{

        File file = new File(instancesFile);
        if(instancesFile ==null || false==file.exists()) {
            throw new RuntimeException("instancesPropertyFile must be defined.");
        }


        List<String> instances = new ArrayList<String>() ;
        BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
        String buffer ;
        while( (buffer=bufferedReader.readLine())!=null){
            if(false==buffer.startsWith(COMMENT)){
                instances.add(buffer) ;
            }
        }
        return instances ;

    }

    public void setInstancesFile(String instancesFile) {
        this.instancesFile = instancesFile;
    }

    public void setTarget(String target) {
        this.target = target;
    }
}

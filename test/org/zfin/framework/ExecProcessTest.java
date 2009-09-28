package org.zfin.framework;

import org.junit.Test;
import static org.junit.Assert.*;
import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.commons.exec.DefaultExecutor;

import java.io.*;

/**
 * This class process exec.
 */
public class ExecProcessTest {
    @Test
    public void basicExec(){
        DefaultExecutor defaultExecutor = new DefaultExecutor();
        CommandLine commandLine = new CommandLine("ls") ;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream() ;
        ByteArrayOutputStream byteArrayErrorStream = new ByteArrayOutputStream() ;
        try {
            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(byteArrayOutputStream,byteArrayErrorStream) ;
            defaultExecutor.setStreamHandler(pumpStreamHandler);
            int exitValue = defaultExecutor.execute(commandLine) ;
            assertFalse(defaultExecutor.isFailure(exitValue));
            assertNotSame("Standard output should not be 0",0,byteArrayOutputStream.toString().length());
        } catch (Exception e) {
            fail(e.toString()) ;
        }
    }


    @Test
    public void execPushStreamFromFile(){
        CommandLine commandLine = new CommandLine("grep") ;
        commandLine.addArgument(">") ;

        ByteArrayOutputStream byteArrayOutputStream  ;
        ByteArrayOutputStream byteArrayErrorStream  ;

        try {
            FileInputStream fileInputStream = new FileInputStream("test/test1.fa") ;
            byteArrayOutputStream = new ByteArrayOutputStream() ;
            byteArrayErrorStream = new ByteArrayOutputStream() ;

            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(byteArrayOutputStream,byteArrayErrorStream,fileInputStream) ;

            DefaultExecutor defaultExecutor = new DefaultExecutor() ;
            defaultExecutor.setStreamHandler(pumpStreamHandler);
            int exitValue = defaultExecutor.execute(commandLine) ;
            assertFalse(defaultExecutor.isFailure(exitValue));
            assertEquals(">some explanatory text",byteArrayOutputStream.toString().trim()) ;
        } catch (IOException e) {
            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }

    }
}

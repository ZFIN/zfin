package org.zfin.framework;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.logging.log4j.LogManager; import org.apache.logging.log4j.Logger;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;

import java.io.*;

import static org.junit.Assert.*;

/**
 * This class process exec.
 */
public class ExecProcessTest {

    private Logger logger = LogManager.getLogger(ExecProcessTest.class) ;

    @Rule
    public ExpectedException exception = ExpectedException.none();

    @Test
    public void basicExec() throws IOException {
        DefaultExecutor defaultExecutor = new DefaultExecutor();
        CommandLine commandLine = new CommandLine("ls") ;

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream() ;
        ByteArrayOutputStream byteArrayErrorStream = new ByteArrayOutputStream() ;

        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(byteArrayOutputStream,byteArrayErrorStream) ;
        defaultExecutor.setStreamHandler(pumpStreamHandler);
        int exitValue = defaultExecutor.execute(commandLine) ;
        assertFalse(defaultExecutor.isFailure(exitValue));
        assertNotSame("Standard output should not be 0",0,byteArrayOutputStream.toString().length());
    }

    @Test
    public void processList() throws IOException {
        DefaultExecutor defaultExecutor = new DefaultExecutor();
        CommandLine commandLine = new CommandLine("ps") ;
        commandLine.addArgument("-ef");

        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream() ;
        ByteArrayOutputStream byteArrayErrorStream = new ByteArrayOutputStream() ;

        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(byteArrayOutputStream,byteArrayErrorStream) ;
        defaultExecutor.setStreamHandler(pumpStreamHandler);
        int exitValue = defaultExecutor.execute(commandLine) ;
        String output = byteArrayOutputStream.toString();
        assertFalse(defaultExecutor.isFailure(exitValue));
        assertNotSame("Standard output should not be 0",0,output.length());
    }


    @Test
    public void execPushStreamFromFile() throws IOException {
        CommandLine commandLine = new CommandLine("grep") ;
        commandLine.addArgument(">") ;

        ByteArrayOutputStream byteArrayOutputStream  ;
        ByteArrayOutputStream byteArrayErrorStream  ;

        FileInputStream fileInputStream = new FileInputStream("test/test1.fa") ;
        byteArrayOutputStream = new ByteArrayOutputStream() ;
        byteArrayErrorStream = new ByteArrayOutputStream() ;

        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(byteArrayOutputStream,byteArrayErrorStream,fileInputStream) ;

        DefaultExecutor defaultExecutor = new DefaultExecutor() ;
        defaultExecutor.setStreamHandler(pumpStreamHandler);
        int exitValue = defaultExecutor.execute(commandLine) ;
        assertFalse(defaultExecutor.isFailure(exitValue));
        assertEquals(">some explanatory text",byteArrayOutputStream.toString().trim()) ;
    }


    /**
     * Want to confirm that for non-log4j file, we get the proper calls.
     */
    @Test
    public void exceptionTest() throws IOException {

        File tempFile = null ;
        PrintWriter printWriter = null ;
        BufferedReader bufferedReader;
        tempFile = File.createTempFile("test",".txt") ;
        tempFile.deleteOnExit();
        printWriter = new PrintWriter(tempFile) ;

        try {
            method1();
        } catch (Exception e) {
            e.printStackTrace(printWriter) ;
            printWriter.close();
            String inputString = "" ;
            String buffer ;
            bufferedReader = new BufferedReader(new FileReader(tempFile));
            while( (buffer = bufferedReader.readLine())!=null){
                inputString += buffer + "\n";
            }

            assertTrue(inputString.contains("$T1: T1 error"));
            assertTrue(inputString.contains("Caused by: org.zfin.framework.ExecProcessTest$T2: T2 error"));
        }


    }

    @Test
    public void javaVersion() throws IOException {
        DefaultExecutor defaultExecutor = new DefaultExecutor();
        CommandLine commandLine = new CommandLine("java") ;
        commandLine.addArgument("-version") ;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream() ;
        ByteArrayOutputStream byteArrayErrorStream = new ByteArrayOutputStream() ;

        PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(byteArrayOutputStream,byteArrayErrorStream) ;
        defaultExecutor.setStreamHandler(pumpStreamHandler);
        int exitValue = defaultExecutor.execute(commandLine) ;
        assertFalse(defaultExecutor.isFailure(exitValue));


    }

    private void method1() throws T1 {
        try {
            method2() ;
        } catch (T2 t2) {
            throw new T1(t2) ;
        }
    }

    private void method2() throws T2{
        throw new T2() ;
    }

    private class T1 extends Exception{
        public T1(Throwable t){
            super("T1 error",t);
        }
    }

    private class T2 extends Exception{
        public T2(){
            super("T2 error");
        }
    }
}

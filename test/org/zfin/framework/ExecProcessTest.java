package org.zfin.framework;

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;
import org.apache.log4j.Logger;
import org.junit.Test;

import java.io.*;

import static org.junit.Assert.*;

/**
 * This class process exec.
 */
public class ExecProcessTest {

    private Logger logger = Logger.getLogger(ExecProcessTest.class) ;

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
            e.printStackTrace();
        }
    }


    /**
     * Want to confirm that for non-log4j file, we get the proper calls.
     */
    @Test
    public void exceptionTest(){

        File tempFile = null ;
        PrintWriter printWriter = null ;
        BufferedReader bufferedReader = null ;
        try {
            tempFile = File.createTempFile("test",".txt") ;
            tempFile.deleteOnExit();
            printWriter = new PrintWriter(tempFile) ;
        } catch (IOException e) {
            fail(e.fillInStackTrace().toString()) ;
        }

        try {
            method1();
        } catch (Exception e) {

//            printWriter.write(e.fillInStackTrace().toString()); // this will not give the root unlike printstacktrace
            e.printStackTrace(printWriter) ;
            printWriter.close();
            String inputString = "" ;
            try {
                String buffer ;
                bufferedReader = new BufferedReader(new FileReader(tempFile));
                while( (buffer = bufferedReader.readLine())!=null){
                    inputString += buffer + "\n";
                }
            } catch (Exception e1) {
                fail(e1.fillInStackTrace().toString()) ;
            }
//            System.out.println(e.fillInStackTrace()) ;
//            System.out.println(inputString) ;
            assertTrue(inputString.contains("$T1: T1 error"));
            assertTrue(inputString.contains("Caused by: org.zfin.framework.ExecProcessTest$T2: T2 error"));
//            logger.error("error",e);
//            e.printStackTrace();  //To change body of catch statement use File | Settings | File Templates.
        }


    }

    @Test
    public void javaVersion(){
        DefaultExecutor defaultExecutor = new DefaultExecutor();
        CommandLine commandLine = new CommandLine("which") ;
        commandLine.addArgument("java") ;
        ByteArrayOutputStream byteArrayOutputStream = new ByteArrayOutputStream() ;
        ByteArrayOutputStream byteArrayErrorStream = new ByteArrayOutputStream() ;
        try {
            PumpStreamHandler pumpStreamHandler = new PumpStreamHandler(byteArrayOutputStream,byteArrayErrorStream) ;
            defaultExecutor.setStreamHandler(pumpStreamHandler);
            int exitValue = defaultExecutor.execute(commandLine) ;
            assertFalse(defaultExecutor.isFailure(exitValue));
        } catch (Exception e) {
            logger.error("failed output: " + byteArrayOutputStream.toString()) ;
            logger.error("failed error: " + byteArrayErrorStream.toString()) ;
            fail(e.toString()) ;
        }


    }

    private void method1() throws T1{
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

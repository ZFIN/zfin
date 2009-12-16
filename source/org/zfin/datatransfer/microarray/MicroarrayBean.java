package org.zfin.datatransfer.microarray;

import org.apache.log4j.Logger;

import java.io.*;
import java.util.Collection;
import java.util.Date;

/**
 * Keeps statistics about microarray job and write them to file.
 * We assume that this is open until we either finish reading or we call toString on it.
 */
public class MicroarrayBean {

    private Logger logger = Logger.getLogger(MicroarrayBean.class);

    //    private List<String> messages = new ArrayList<String>() ;
    //    private Collection<String> notFoundAccessions ;
    private Date date = new Date();

    private File file = null;
    private BufferedWriter writer = null;

    public MicroarrayBean() throws IOException {
        file = File.createTempFile("microarrayReport" + date.toString(), ".txt");
        writer = new BufferedWriter(new FileWriter(file));
    }

    public void addMessage(String s) throws IOException {
        if (writer == null) {
            writer = new BufferedWriter(new FileWriter(file));
        }
        logger.info(s);
        writer.write(s + "\n");
        writer.flush();
    }

    public void addNotFound(String newMicroArrayAccession) throws IOException {
        logger.info("not found: " + newMicroArrayAccession);
        addMessage("not found[" + newMicroArrayAccession + "]");
    }

    public String finishReadingAndRetrieve() throws IOException {
        if (writer != null) {
            writer.close();
            writer = null;
        }
        try {
            if (writer != null) {
                writer.close();
            }
            BufferedReader bufferedReader = new BufferedReader(new FileReader(file));
            String buffer;
            StringBuffer stringBuffer = new StringBuffer();
            stringBuffer.append("file[" + file.getAbsolutePath()).append("]\n");
            while ((buffer = bufferedReader.readLine()) != null) {
                stringBuffer.append(buffer).append("\n");
            }
            bufferedReader.close();
            return stringBuffer.toString();
        } catch (IOException e) {
            logger.error(e);
            return "Failed to read string from file:" + e.fillInStackTrace().toString();
        }
    }

    @Override
    public String toString() {
        if (file == null) {
            return "null microarrayBean";
        }
        return "MicroarrayBean :" + file.getAbsolutePath();
    }

    public File getFile() {
        return file;
    }

    public void setNotFoundAccessions(Collection<String> accessionsNotFound) throws IOException {
        for (String accessionNotFound : accessionsNotFound) {
            addNotFound(accessionNotFound);
        }
    }
}

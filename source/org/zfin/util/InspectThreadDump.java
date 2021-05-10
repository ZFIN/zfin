package org.zfin.util;

import java.io.BufferedReader;
import java.io.File;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Created by cmpich on 3/22/17.
 */
public class InspectThreadDump {

    private File file;

    public InspectThreadDump(File file) {
        this.file = file;
    }

    public static void main(String[] arguments) throws IOException {
        if (arguments.length == 0) {
            System.out.println("No filename provided");
            System.exit(-1);
        }
        String fileName = arguments[0];
        File file = new File(fileName);
        if (!file.exists()) {
            System.out.println("No file found: " + file.getAbsolutePath());
            System.exit(-1);
        }
        InspectThreadDump service = new InspectThreadDump(file);
        service.parseFile();
        System.exit(0);
    }

    private void parseFile() throws IOException {
        FileReader fileReader = new FileReader(file);
        BufferedReader bufferedReader = new BufferedReader(fileReader);
        StringBuffer stringBuffer = new StringBuffer();
        String line;

        List<List<String>> threads = new ArrayList<>(200);
        boolean newThread;
        int index = 0;
        List<String> thread = null;
        while ((line = bufferedReader.readLine()) != null) {
            if (line.startsWith("\"http-bio")) {
                thread = new ArrayList<>(5);
                threads.add(thread);
                continue;
            }
            if (line.trim().startsWith("at org.zfin")) {
                thread.add(line.replace("at ", "").replace("\t", ""));
            }
        }
        displayThreads(threads);
        //System.out.print(stringBuffer);
    }

    private void displayThreads(List<List<String>> threads) {
        System.out.println("Number of HTTP Threads: " + threads.size());

        List<String> entryPoints = new ArrayList<>();
        for (List<String> thread : threads) {
            String point = getFirstZFINAccessFromSingleThread(thread);
            if (point != null)
                entryPoints.add(point);
        }
        System.out.println("Number of ZFIN Threads: " + threads.size());
        Collections.sort(entryPoints);
        int index = 1;
        for (String line : entryPoints) {
            System.out.print(index++ + ": ");
            System.out.println(line);
        }
    }

    private String getFirstZFINAccessFromSingleThread(List<String> thread) {
        //System.out.println("");
        String entryPoint = null;
        for (String line : thread) {
            if (line.startsWith("org.zfin.framework.filter.AddRequestInfoToLog4j") ||
                    line.startsWith("org.zfin.framework.filter.UpdatesCheckFilter"))
                continue;
            entryPoint = line;
        }
        return entryPoint;
    }

/*
*             if (line.startsWith("\"http-bio")) {
                stringBuffer.append("\n");
                stringBuffer.append(index++ + ": ");
                stringBuffer.append(line);
                stringBuffer.append("\n");
                continue;
            }
            if (line.trim().startsWith("at org.zfin")) {
                stringBuffer.append(line.replace("at ", ""));
                stringBuffer.append("\n");
                continue;
            }
*/
}

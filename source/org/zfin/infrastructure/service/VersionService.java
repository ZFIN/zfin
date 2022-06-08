package org.zfin.infrastructure.service;

import lombok.extern.log4j.Log4j2;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;

@Log4j2
public class VersionService {

    public static String getSoftwareVersion() {
        String branch = "unknown";
        try {
            Class clazz = VersionService.class;
            InputStream inputStream = clazz.getResourceAsStream("/git-info.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            reader.readLine();
            branch = reader.readLine();
        } catch (Exception e) {
            log.error("could not read git-info.txt file");
        }
        return branch;
    }

    public static String getSoftwareCommit() {
        String commit = "unknown";
        try {
            Class clazz = VersionService.class;
            InputStream inputStream = clazz.getResourceAsStream("/git-info.txt");
            BufferedReader reader = new BufferedReader(new InputStreamReader(inputStream));
            commit = reader.readLine();
        } catch (Exception e) {
            log.error("could not read git-info.txt file");
        }
        return commit;
    }

}

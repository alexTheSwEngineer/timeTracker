package com.atr.timetracker;
/*
 * @author aleksandartrposki@gmail.com
 * @since 13.07.18
 *
 *
 */

import org.apache.commons.exec.CommandLine;
import org.apache.commons.exec.DefaultExecutor;
import org.apache.commons.exec.PumpStreamHandler;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.util.HashMap;
import java.util.Map;
import java.util.UUID;

public class ExecUtils {
    public static String execute(File wdir, String bin, String args, Map<String, String> map) throws IOException {
        try (ByteArrayOutputStream os = new ByteArrayOutputStream()) {
            PumpStreamHandler psh = new PumpStreamHandler(os);
            DefaultExecutor exec = new DefaultExecutor();
            CommandLine cmdLine = new CommandLine(bin);
            cmdLine.addArguments(args);
            cmdLine.setSubstitutionMap(map);
            exec.setWorkingDirectory(wdir);
            exec.setStreamHandler(psh);
            int exitValue = exec.execute(cmdLine);
            return os.toString("utf8");
        }
    }

    public static String executeSilently(String bin, String args,String defaultVal) {
        try{
            Path tempDirectory = Files.createTempDirectory("UUID.randomUUID().toString()");
            return execute(tempDirectory.toFile(),bin,args,new HashMap<>());
        }catch (Exception e){
            e.printStackTrace(System.out);
            return defaultVal;
        }
    }
}

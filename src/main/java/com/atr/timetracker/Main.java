package com.atr.timetracker;
/*
 * @author aleksandartrposki@gmail.com
 * @since 12.07.18
 *
 *
 */

import com.atr.timetracker.config.Config;
import com.atr.timetracker.dto.Stat;
import com.atr.timetracker.dto.Window;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.springframework.core.io.ClassPathResource;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.Arrays;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.atomic.AtomicBoolean;
import java.util.stream.Collectors;

import static java.util.Comparator.comparing;

public class Main {
    public static void main(String[] args) throws IOException, ExecutionException, InterruptedException {

        ObjectMapper objectMapper = new ObjectMapper();
        Config config = readConfig(args, objectMapper);
        SnapshotParser parser = new SnapshotParser(objectMapper);
        StatisticsParser statisticsParser = new StatisticsParser(objectMapper);
        AtomicBoolean shouldContinueTracking = new AtomicBoolean(true);

        CompletableFuture<List<Stat>> trackingJob = CompletableFuture.supplyAsync(() -> {
            try {
                blockingTrackTimeToOutputFile(config, parser, shouldContinueTracking);
                return statisticsParser.parseFile(config.getOutFile(), config.getSnapshotIntervalInSeconds());

            } catch (InterruptedException | IOException e) {
                e.printStackTrace(System.out);
                System.out.println("End the program");
                throw new RuntimeException(e);
            }
        });


        System.out.println("Press any key to end tracking:");
        System.in.read();
        System.out.println("Waiting for the last snapshot to finish......");

        shouldContinueTracking.set(false);
        List<Stat> stats = trackingJob.get();

        System.out.println("Computing stats...........");
        String resultAsString = formatResults(objectMapper, stats);

        System.out.print(resultAsString);
        if (config.getStatsOutputFile() != null && !"".equals(config.getSnapshotsFile()) && new File(config.getSnapshotsFile()).exists()) {
            Files.write(Paths.get(config.getStatsOutputFile()), Arrays.asList(resultAsString));
        }

    }

    private static String formatResults(ObjectMapper objectMapper, List<Stat> stats) {
        StringWriter sw = new StringWriter();
        PrintWriter pw = new PrintWriter(sw);
        stats.stream()
                .sorted(comparing(x -> x.getDir() == null ? "" : x.getDir()))
                .forEach(x -> {
                    try {
                        pw.println(objectMapper.writeValueAsString(x));
                    } catch (IOException e) {
                        e.printStackTrace();
                        return;
                    }
                });


        pw.flush();
        return sw.toString();
    }

    private static void blockingTrackTimeToOutputFile(Config config, SnapshotParser parser, AtomicBoolean shouldContinue) throws InterruptedException, IOException {
        try (FileWriter fw = new FileWriter(config.getOutFile(), true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw);
             FileWriter logFileWriter = new FileWriter(config.getSnapshotsFile(), true);
             BufferedWriter logFileBufferedWriter = new BufferedWriter(logFileWriter);
             PrintWriter logWriter = new PrintWriter(logFileBufferedWriter)) {
            while (shouldContinue.get()) {
                try {
                    String snapshot = ExecUtils.executeSilently(config.getTrackerBin(), config.getTrackerArgs(), "");
                    logWriter.print(snapshot);
                    logWriter.println(",");
                    Window windowWithTaskInfo = parser.parseSnapshot(config, snapshot);
                    out.println(parser.writeValueAsString(windowWithTaskInfo));
                } catch (Exception e) {
                    e.printStackTrace(System.out);
                }
                Thread.sleep(1000l * config.getSnapshotIntervalInSeconds());
            }
            logWriter.flush();
            out.flush();
        }
    }

    public static Config readConfig(String[] args, ObjectMapper objectMapper) throws IOException {
        Config config;
        if (args.length == 1) {
            String configAsString = Files.readAllLines(Paths.get(args[0])).stream()
                    .collect(Collectors.joining("\n"));
            config = objectMapper.readValue(configAsString, Config.class);
        } else {
            try (InputStream is = new ClassPathResource("config.json").getInputStream();) {
                config = objectMapper.readValue(is, Config.class);
            }
        }
        return config;
    }


}

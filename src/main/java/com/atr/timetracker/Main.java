package com.atr.timetracker;
/*
 * @author aleksandartrposki@gmail.com
 * @since 12.07.18
 *
 *
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.searchink.executils.ExecUtils;
import javaslang.control.Try;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.io.*;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

public class Main {
    public static void main(String[] args) throws IOException {
        Map<String, ExtractConfig> regexes = new HashMap<>();
        regexes.put(".*IntelliJ.*", new ExtractConfig().setExtractRegex("\\[[^\\[]+\\]")
                .setTrimBegin(2)
                .setTrimEnd(1));
        String trackerBinary = "/home/alex/Downloads/thyme-linux-386";
        ObjectMapper objectMapper = new ObjectMapper();
        String binArgs = "track";

        String snapshotAsString = ExecUtils.execute(trackerBinary, binArgs);
        Snapshot snapshot = objectMapper.readValue(snapshotAsString, Snapshot.class);
        String activeId = ofNullable(snapshot)
                .map(x -> x.getActive())
                .orElse("");
        Window activeWindow = ofNullable(snapshot)
                .map(x -> x.getWindows())
                .orElse(new ArrayList<>())
                .stream()
                .filter(x -> activeId.equals(x.getID()))
                .findAny()
                .orElse(new Window());


        ExtractConfig extractConfig = regexes.entrySet().stream()
                .filter(x -> Try.of(() -> Pattern.compile(x.getKey()))
                        .mapTry(p -> p.matcher(activeWindow.getName()))
                        .mapTry(m -> m.find())
                        .getOrElse(false))
                .map(x -> x.getValue())
                .findAny()
                .orElse(new ExtractConfig());

        String location = extractConfig.extract(activeWindow.getName());
        File fileLocation = new File(location, ".git");
        File homeDir = new File(System.getProperty("user.home"));
        File absoluteLocation = new File(homeDir,fileLocation.getPath());
        if (!absoluteLocation.exists()) {
            return;
        }
        String aaa = "git --git-dir $activeDir/.git rev-parse --abbrev-ref HEAD";
        String branch = ExecUtils.execute("git", "--git-dir " + absoluteLocation.getAbsolutePath() + " rev-parse --abbrev-ref HEAD");
        activeWindow.setBranch(branch);
        activeWindow.setDir(location);

        try (FileWriter fw = new FileWriter("/home/alex/a.txt", true);
             BufferedWriter bw = new BufferedWriter(fw);
             PrintWriter out = new PrintWriter(bw)) {
            out.println(objectMapper.writeValueAsString(activeWindow));
            //more code
        } catch (IOException e) {
            //exception handling left as an exercise for the reader
        }

    }


    @Getter
    @Setter
    public static class Snapshot {
        @JsonProperty("Active")
        private String active = "";
        @JsonProperty("Time")
        private String timeStamp = "";
        @JsonProperty("Windows")
        private List<Window> windows = new ArrayList<>();
        @JsonProperty("Visible")
        private List<Long> visibleIds = new ArrayList<>();
    }

    @Getter
    @Setter
    public static class Window {
        @JsonProperty("ID")
        public String ID = "";
        @JsonProperty("Desktop")
        public int desktop = 0;
        @JsonProperty("Name")
        public String name = "";
        public String dir = "";
        public String branch = "";
    }

    @Getter
    @Setter
    @Accessors(chain = true)
    public static class ExtractConfig {
        public String extractRegex = "";
        public int trimBegin;
        public int trimEnd;

        public String extract(String input) {
            return Try.of(() -> Pattern.compile(extractRegex))
                    .mapTry(x -> x.matcher(input))
                    .filter(x -> x.find())
                    .mapTry(x -> x.group())
                    .mapTry(x -> x.substring(trimBegin, x.length() - trimEnd))
                    .getOrElse("");
        }
    }
}

package com.atr.timetracker;
/*
 * @author aleksandartrposki@gmail.com
 * @since 12.07.18
 *
 *
 */

import com.atr.timetracker.config.Config;
import com.atr.timetracker.config.RegexConfig;
import com.atr.timetracker.dto.Snapshot;
import com.atr.timetracker.dto.Window;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import javaslang.control.Try;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.Map;
import java.util.Optional;
import java.util.concurrent.ConcurrentHashMap;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Optional.ofNullable;

public class SnapshotParser {
    private ObjectMapper objectMapper = new ObjectMapper();

    public SnapshotParser(ObjectMapper objectMapper) {
        this.objectMapper = objectMapper;
    }

    public Window parseSnapshot(Config config, String snapshotAsString) throws IOException {
        Snapshot snapshot = objectMapper.readValue(snapshotAsString, Snapshot.class);
        ConcurrentHashMap<String, RegexConfig> extractProjectNameConfig = config.getExtractProjectNameConfig();
        ConcurrentHashMap<String, RegexConfig> extractLocationConfig = config.getExtractLocationConfig();
        String activeId = getActiveIdOrEmpty(snapshot);
        Window activeWindow = getActiveWIndowOrEmpty(snapshot, activeId);
        activeWindow.setTimeStamp(snapshot.getTimeStamp());

        String location = extractOrEmpty(extractLocationConfig, activeWindow.getName());
        File fileLocation = new File(location);
        File absoluteLocation = new File(config.getHomeFolder(),fileLocation.getPath());
        activeWindow.setDir(absoluteLocation.getPath());

        String projectName = extractOrEmpty(extractProjectNameConfig,activeWindow.getName());
        activeWindow.setProjectName(projectName);

        Optional.of(new File(absoluteLocation,".git"))
                .filter(File::exists)
                .map(x-> ExecUtils.executeSilently(config.getGitBin(), "--git-dir "+x.getAbsolutePath()+" "+ config.getGitGetBranchArgs(),""))
                .ifPresent(activeWindow::setBranch);
        return activeWindow;
    }

    public String writeValueAsString(Object obj) throws JsonProcessingException {
        return objectMapper.writeValueAsString(obj);
    }



    private String extractOrEmpty(Map<String, RegexConfig> regexesByKey, String inputStringToMatch) {
        return regexesByKey.entrySet().stream()
                .filter(x -> Try.of(() -> Pattern.compile(x.getKey()))
                        .mapTry(p -> p.matcher(inputStringToMatch))
                        .mapTry(Matcher::find)
                        .getOrElse(false))
                .map(Map.Entry::getValue)
                .findAny()
                .orElse(new RegexConfig())
                .extract(inputStringToMatch);
    }

    private Window getActiveWIndowOrEmpty(Snapshot snapshot, String activeId) {
        return ofNullable(snapshot)
                .map(Snapshot::getWindows)
                .orElse(new ArrayList<>())
                .stream()
                .filter(x -> activeId.equals(x.getID()))
                .findAny()
                .orElse(new Window());
    }

    private String getActiveIdOrEmpty(Snapshot snapshot) {
        return ofNullable(snapshot)
                .map(Snapshot::getActive)
                .orElse("");
    }
}

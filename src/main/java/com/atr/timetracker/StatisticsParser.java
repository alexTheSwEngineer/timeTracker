package com.atr.timetracker;
/*
 * @author aleksandartrposki@gmail.com
 * @since 12.07.18
 *
 *
 */

import com.atr.timetracker.dto.Stat;
import com.atr.timetracker.dto.Window;
import com.fasterxml.jackson.databind.ObjectMapper;

import java.io.IOException;
import java.nio.file.Paths;
import java.util.List;

import static java.nio.file.Files.readAllLines;
import static java.util.stream.Collectors.*;

public class StatisticsParser {
    private ObjectMapper mapper;

    public StatisticsParser(ObjectMapper mapper) {
        this.mapper = mapper;
    }

    public List<Stat> parseFile(String input, int snapshotTimeUnit) throws IOException {
        List<Window> windows = deserializeWindows(input);
        return getStats(snapshotTimeUnit, windows);
    }

    private List<Window> deserializeWindows(String input) throws IOException {
        List<String> strings = readAllLines(Paths.get(input));
        return strings.stream()
                .map(x -> {
                    try {
                        return mapper.readValue(x, Window.class);
                    } catch (IOException e) {
                        return new Window().setProjectName("error").setName("error");
                    }
                })
                .collect(toList());
    }

    public List<Stat> getStats(int snapshotTimeUnit, List<Window> windows) {
        return
                windows.stream().map(x -> new Stat().setDir(x.getDir())
                        .setBranch(x.getBranch())
                        .setProjectName(x.getProjectName()))
                        .collect(groupingBy(x -> x, summingLong(x -> snapshotTimeUnit)))
                        .entrySet().stream()
                        .map(x -> x.getKey().setTimeInSeconds(x.getValue()))
                        .collect(toList());
    }
}

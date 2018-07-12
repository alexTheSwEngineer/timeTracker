package com.atr.timetracker.config;
/*
 * @author aleksandartrposki@gmail.com
 * @since 12.07.18
 *
 *
 */

import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;

@Getter
@Setter
@Accessors(chain = true)
public class Config {
    private ConcurrentHashMap<String,RegexConfig> extractLocationConfig;
    private ConcurrentHashMap<String,RegexConfig> extractProjectNameConfig;
    private String trackerBin;
    private String trackerArgs;
    private String gitBin;
    private String gitGetBranchArgs;
    private String homeFolder;
    private String outFile;
    private String snapshotsFile;
    private String statsOutputFile;
    private int snapshotIntervalInSeconds;


}

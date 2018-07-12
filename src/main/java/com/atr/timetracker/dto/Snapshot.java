package com.atr.timetracker.dto;
/*
 * @author aleksandartrposki@gmail.com
 * @since 12.07.18
 *
 *
 */

import com.fasterxml.jackson.annotation.JsonProperty;
import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;
import java.util.List;

@Getter
@Setter
public class Snapshot {
    @JsonProperty("Active")
    private String active = "";
    @JsonProperty("Time")
    private String timeStamp = "";
    @JsonProperty("Windows")
    private List<Window> windows = new ArrayList<>();
    @JsonProperty("Visible")
    private List<Long> visibleIds = new ArrayList<>();
}




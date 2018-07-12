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
import lombok.ToString;
import lombok.experimental.Accessors;


@Setter
@Accessors(chain = true)
@ToString
public class Window {
    @JsonProperty("ID")
    public String ID = "";
    @JsonProperty("Desktop")
    public int desktop = 0;
    @JsonProperty("Name")
    public String name = "";
    @JsonProperty("Time")
    public String timeStamp;

    public String dir = "";
    public String branch = "";
    public String projectName;

    public String getID() {
        return ID == null? "" : ID;
    }

    public int getDesktop() {
        return desktop;
    }

    public String getName() {
        return name== null? "" : name;
    }

    public String getTimeStamp() {
        return timeStamp== null? "" : timeStamp;
    }

    public String getDir() {
        return dir== null? "" : dir;
    }

    public String getBranch() {
        return branch== null? "" : branch;
    }

    public String getProjectName() {
        return projectName == null? "" : projectName;
    }
}
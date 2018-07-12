package com.atr.timetracker.dto;
/*
 * @author aleksandartrposki@gmail.com
 * @since 12.07.18
 *
 *
 */

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.EqualsAndHashCode;
import lombok.Getter;
import lombok.Setter;
import lombok.ToString;
import lombok.experimental.Accessors;
import org.joda.time.Interval;


@Getter
@Setter
@Accessors(chain = true)
@EqualsAndHashCode(exclude = "timeInSeconds")
@ToString
public class Stat {
    String dir;
    String projectName;
    String branch;
    @JsonIgnore
    long timeInSeconds;

    public String getTime(){
        long total = timeInSeconds;
        long h = total / 3600;
        total-= h*3600;
        long m = total/60;
        total -= m*60l;
        return String.format("%dh %dm %ds",h,m,total);
    }
}

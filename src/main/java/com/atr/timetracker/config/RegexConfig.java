package com.atr.timetracker.config;
/*
 * @author aleksandartrposki@gmail.com
 * @since 12.07.18
 *
 *
 */

import javaslang.control.Try;
import lombok.Getter;
import lombok.Setter;
import lombok.experimental.Accessors;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

@Getter
@Setter
@Accessors(chain = true)
public  class RegexConfig {
    public String extractRegex = "";
    public int trimBegin;
    public int trimEnd;
    public int forceGroupIndex;

    public String extract(String input) {
        return Try.of(() -> Pattern.compile(extractRegex))
                .mapTry(x -> x.matcher(input))
                .filter(Matcher::find)
                .mapTry(x -> x.group(forceGroupIndex))
                .mapTry(x -> x.substring(trimBegin, x.length() - trimEnd))
                .getOrElse("");
    }

}

package io.cbdq;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

public class DestinationTopicName {
    private final String pattern;

    public DestinationTopicName(String pattern) {
        this.pattern = pattern;
    }

    public String destination_topic_name(String source_topic_name) {
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("topic", source_topic_name);
        String templateString = pattern;
        StringSubstitutor sub = new StringSubstitutor(valuesMap);
        return sub.replace(templateString);
    }
}

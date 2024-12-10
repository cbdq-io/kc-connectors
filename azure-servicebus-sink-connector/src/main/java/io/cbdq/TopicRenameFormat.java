package io.cbdq;

import java.util.HashMap;
import java.util.Map;

import org.apache.commons.text.StringSubstitutor;

public class TopicRenameFormat {
    private final String pattern;

    public TopicRenameFormat(String pattern) {
        this.pattern = pattern;
    }

    public String rename(String kafkaTopicName) {
        Map<String, String> valuesMap = new HashMap<>();
        valuesMap.put("topic", kafkaTopicName);
        String templateString = pattern;
        StringSubstitutor sub = new StringSubstitutor(valuesMap);
        return sub.replace(templateString);
    }
}

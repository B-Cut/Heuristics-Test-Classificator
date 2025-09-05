package cael.uff.classification.framework;

import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.fasterxml.jackson.annotation.JsonProperty;

@JsonIgnoreProperties(value={ "groupIds" })
public record FrameworkInfo(
        @JsonProperty("keywords")
        String[] frameworkKeywords
) {
}

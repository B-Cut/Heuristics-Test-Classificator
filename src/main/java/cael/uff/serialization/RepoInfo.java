package cael.uff.serialization;

import com.fasterxml.jackson.annotation.JsonGetter;

import java.util.List;

public record RepoInfo(
        @JsonGetter("repo") String name,
        @JsonGetter("uses") String[] testFrameworks
) {
}

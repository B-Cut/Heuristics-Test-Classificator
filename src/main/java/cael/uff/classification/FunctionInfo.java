package cael.uff.classification;

import java.nio.file.Path;

public record FunctionInfo(
        String Name,
        Path originFile
) {
}

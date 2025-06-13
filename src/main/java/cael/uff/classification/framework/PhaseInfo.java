package cael.uff.classification.framework;

import java.util.List;

public record PhaseInfo(
        int priority,
        String phase,
        List<FrameworkInfo> libraries
) {
}

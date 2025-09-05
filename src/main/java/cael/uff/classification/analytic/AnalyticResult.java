package cael.uff.classification.analytic;

public record AnalyticResult(
        String Name,
        String Origin,
        UnitTypes Type,
        String body,
        Boolean hadError
) {
}

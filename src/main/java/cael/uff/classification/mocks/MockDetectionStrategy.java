package cael.uff.classification.mocks;

import spoon.reflect.declaration.CtMethod;

import java.util.List;

public interface MockDetectionStrategy {
    List<MockResult> detectMocks(CtMethod<?> method);
}

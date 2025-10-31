package cael.uff.ModelFactories;

import spoon.reflect.CtModel;

import java.nio.file.Path;

public interface ModelFactory {
    public CtModel createModel(Path path);

}

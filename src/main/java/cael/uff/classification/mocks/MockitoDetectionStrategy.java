package cael.uff.classification.mocks;

import cael.uff.Utils;
import spoon.reflect.code.CtAssignment;
import spoon.reflect.code.CtExpression;
import spoon.reflect.code.CtInvocation;
import spoon.reflect.declaration.CtElement;
import spoon.reflect.declaration.CtExecutable;
import spoon.reflect.declaration.CtMethod;
import spoon.reflect.declaration.CtVariable;
import spoon.reflect.reference.CtExecutableReference;
import spoon.reflect.reference.CtTypeReference;
import spoon.reflect.visitor.CtScanner;

import java.util.ArrayList;
import java.util.List;

public class MockitoDetectionStrategy implements MockDetectionStrategy{
    // Mockito mock classes are seen as an instance of the original class
    // Therefore, to track mocks, we need to look for variables initialized as mocked
    // And for the use of mockito functions
    @Override
    public List<MockResult> detectMocks(CtMethod<?> method) {
        List<MockResult> mockResults = new ArrayList<>();

        VariableScanner variableScanner = new VariableScanner();
        variableScanner.scan(method);
        var mockedVars = variableScanner.variables;



        return List.of();
    }

    private class VariableScanner extends CtScanner {
        List<CtVariable<?>> variables = new ArrayList<>();

        @Override
        public <T, A extends T> void visitCtAssignment(CtAssignment<T, A> assignment) {
            CtExpression<?> leftSide = assignment.getAssigned();
            CtExpression<?> rightSide = assignment.getAssignment();

            // Check if the assignment references a mockito element
            variables.addAll(rightSide.filterChildren((CtExecutable<?> ex) -> {
                return ex.getSimpleName().contains("mock");
            }).list());
        }
    }

    private class InvocationScanner extends CtScanner {
        List<CtExpression<?>> mockedExpressions = new ArrayList<>();

        // Look for mocked methods inside mockito's "when" function
        @Override
        public <T> void visitCtInvocation(CtInvocation<T> invocation) {
            // If invocation does not belong to mockito, skip
            if (!Utils.containsCaseInsensitive(invocation.getType().getQualifiedName(), "mockito")){
                return;
            }

            if (invocation.getExecutable().getSimpleName().equals("when")){
                mockedExpressions.add(invocation.getArguments().getFirst());
            }
        }
    }
}

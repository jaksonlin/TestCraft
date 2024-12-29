package com.github.jaksonlin.pitestintellij.context;

import java.util.Arrays;
import java.util.List;

public class TestPoints {
    public static final String NONE_EMPTY_CHECK = "NonEmptyCheck";
    public static final String BOUNDARY_VALUE_CHECK = "BoundaryValueCheck";
    public static final String ERROR_HANDLING = "ErrorHandling";
    public static final String INPUT_VALIDATION = "InputValidation";
    public static final String POSITIVE_BRANCH = "PositiveBranch";
    public static final String NEGATIVE_BRANCH = "NegativeBranch";
    public static final String FUNCTIONALITY_CHECK = "FunctionalityCheck";
    public static final String BUSINESS_LOGIC_VALIDATION = "BusinessLogicValidation";
    public static final String BUSINESS_INPUT_CHECK = "BusinessInputOutputCheck";
    public static final String SIDE_EFFECT_CHECK = "SideEffectsCheck";
    public static final String STATE_TRANSITION_CHECK = "StateTransitionCheck";
    public static final String BUSINESS_CALCULATION_CHECK = "BusinessCalculationCheck";

    public static final List<String> builtinTestPoints = Arrays.asList(
            NONE_EMPTY_CHECK,
            BOUNDARY_VALUE_CHECK,
            ERROR_HANDLING,
            INPUT_VALIDATION,
            POSITIVE_BRANCH,
            NEGATIVE_BRANCH,
            FUNCTIONALITY_CHECK,
            BUSINESS_LOGIC_VALIDATION,
            BUSINESS_INPUT_CHECK,
            SIDE_EFFECT_CHECK,
            STATE_TRANSITION_CHECK,
            BUSINESS_CALCULATION_CHECK
    );
}
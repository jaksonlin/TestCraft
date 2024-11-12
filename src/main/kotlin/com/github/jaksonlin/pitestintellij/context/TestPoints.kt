package com.github.jaksonlin.pitestintellij.context


object TestPoints {
    const val NONE_EMPTY_CHECK = "NonEmptyCheck"
    const val BOUNDARY_VALUE_CHECK = "BoundaryValueCheck"
    const val ERROR_HANDLING = "ErrorHandling"
    const val INPUT_VALIDATION = "InputValidation"
    const val POSITIVE_BRANCH = "PositiveBranch"
    const val NEGATIVE_BRANCH = "NegativeBranch"
    const val FUNCTIONALITY_CHECK = "FunctionalityCheck"
    const val BUSINESS_LOGIC_VALIDATION = "BusinessLogicValidation"
    const val BUSINESS_INPUT_CHECK = "BusinessInputOutputCheck"
    const val SIDE_EFFECT_CHECK = "SideEffectsCheck"
    const val STATE_TRANSITION_CHECK = "StateTransitionCheck"
    const val BUSINESS_CALCULATION_CHECK = "BusinessCalculationCheck"

    val builtinTestPoints = listOf(
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
    )
}
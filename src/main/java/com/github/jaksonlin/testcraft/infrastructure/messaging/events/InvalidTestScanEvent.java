package com.github.jaksonlin.testcraft.infrastructure.messaging.events;

import com.github.jaksonlin.testcraft.domain.model.InvalidTestCase;

import java.util.List;

public class InvalidTestScanEvent extends BaseEvent {
    public static final String INVALID_TEST_SCAN_START_EVENT = "INVALID_TEST_SCAN_START_EVENT";
    public static final String INVALID_TEST_SCAN_END_EVENT = "INVALID_TEST_SCAN_END_EVENT";

    public InvalidTestScanEvent(String eventType, List<InvalidTestCase> invalidTestCases) {
        super(eventType, invalidTestCases);
    }
}

package com.github.jaksonlin.testcraft.services;

import com.github.jaksonlin.testcraft.annotations.AnnotationSchema;
import com.intellij.openapi.components.PersistentStateComponent;
import com.intellij.openapi.components.Service;
import com.intellij.openapi.components.State;
import com.intellij.openapi.components.Storage;
import com.intellij.openapi.diagnostic.Logger;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Objects;

@Service(Service.Level.APP)
@State(
        name = "InvalidTestCaseConfig",
        storages = {@Storage(value = "$APP_CONFIG$/pitestInvalidTestCase.xml")}
)
public final class InvalidTestCaseConfigService implements PersistentStateComponent<InvalidTestCaseConfigService.State> {
    private static final Logger LOG = Logger.getInstance(InvalidTestCaseConfigService.class);

    public static class State {
        public String invalidAssertionText = InvalidTestCaseConfigService.getBuiltInInvalidAssertionText();
        public boolean enable = false;
        public boolean enableCommentCheck = false;

        public State() {
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;
            State state = (State) o;
            return enable == state.enable &&
                   enableCommentCheck == state.enableCommentCheck &&
                   Objects.equals(invalidAssertionText, state.invalidAssertionText);
        }

        @Override
        public int hashCode() {
            return Objects.hash(invalidAssertionText, enable, enableCommentCheck);
        }
    }

    private State myState = new State();

    public List<String> getInvalidAssertions() {
        return myState.invalidAssertionText == null ? new ArrayList<>() : Arrays.asList(myState.invalidAssertionText.split("\n"));
    }

    public boolean isEnable() {
        return myState.enable;
    }

    public void setEnable(boolean enable) {
        myState.enable = enable;
    }

    public boolean isEnableCommentCheck() {
        return myState.enableCommentCheck;
    }

    public void setEnableCommentCheck(boolean enable) {
        myState.enableCommentCheck = enable;
    }

    @Nullable
    @Override
    public State getState() {
        return myState;
    }

    @Override
    public void loadState(@NotNull State state) {
        LOG.info("Loading invalid test case config: " + state.invalidAssertionText);
        myState = state;
    }

    public static String getBuiltInInvalidAssertionText() {
        return String.join("\n", defaultInvalidAssert);
    }

    private static final String[] defaultInvalidAssert = {"assertTrue(Boolean.TRUE)", "assertFalse(Boolean.FALSE)",
            "assertTrue(true)", "assertEquals(\"true\", \"true\")", "assertEquals(\"true\",\"true\")",
            "assertEquals(true, true)",
            "assertNull(null)", "assertEquals(1, 1)", "assertEquals(1,1)",
            "assertEquals(\"1\", \"1\")", "assertEquals(true,true)", "assertEquals(true, true)",
            "assertEquals(\"1\",\"1\")", "assertNotNull(\"1\")", "assertNotNull(\"2\")",
            "assertNotNull(true)", "assertNotNull(\"\")", "assert(Boolean.TRUE)",
            "assertTrue(1 == 1)", "assertEquals(true,true )",
            "assertEquals(\"success\", \"success\")", "assertEquals(\"success\",\"success\")",
            "assert true",  "assertEquals(true ,true)",
            "assertEquals(\"result\", \"result\")", "assertNotNull(Boolean.TRUE)",
            "assertFalse(false)", "assertEquals(  1, 1)", "\", true, true);",
            "assertNotNull(\"true\")", "assertNotNull(\"false\")",
            "assertNotNull(\"0\");", "assertNotNull(\"3\");", "assertNotNull(\"4\");",
            "assertNotNull(\"5\");", "assertNotNull(\"6\");", "assertNotNull(\"7\");",
            "assertNotNull(\"8\");"};
}

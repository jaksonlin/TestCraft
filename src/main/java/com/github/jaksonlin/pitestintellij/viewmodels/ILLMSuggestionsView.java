package com.github.jaksonlin.pitestintellij.viewmodels;

import javax.swing.*;

public interface ILLMSuggestionsView {
    void setGenerateButtonEnabled(boolean enabled);
    JPanel getPanel();
} 
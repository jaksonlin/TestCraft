package com.github.jaksonlin.pitestintellij.util;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public class Indexes {
    private List<Integer> index;

    public Indexes() {
    }

    public Indexes(List<Integer> index) {
        this.index = index;
    }

    public List<Integer> getIndex() {
        return index;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    public void setIndex(List<Integer> index) {
        this.index = index;
    }
}

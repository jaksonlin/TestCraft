package com.github.jaksonlin.pitestintellij.util;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public class Blocks {
    private List<Integer> block;

    public Blocks() {
    }

    public Blocks(List<Integer> block) {
        this.block = block;
    }

    public List<Integer> getBlock() {
        return block;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    public void setBlock(List<Integer> block) {
        this.block = block;
    }
}

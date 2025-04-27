package com.github.jaksonlin.testcraft.util;

import com.fasterxml.jackson.dataformat.xml.annotation.JacksonXmlElementWrapper;

import java.util.List;

public class Mutations {
    private boolean partial;
    private List<Mutation> mutation;

    public Mutations() {
    }

    public Mutations(boolean partial, List<Mutation> mutation) {
        this.partial = partial;
        this.mutation = mutation;
    }

    public boolean isPartial() {
        return partial;
    }

    public List<Mutation> getMutation() {
        return mutation;
    }

    public void setPartial(boolean partial) {
        this.partial = partial;
    }

    @JacksonXmlElementWrapper(useWrapping = false)
    public void setMutation(List<Mutation> mutation) {
        this.mutation = mutation;
    }
}

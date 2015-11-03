package com.example.yeelin.projects.betweenus.data.fb.model;

/**
 * Created by ninjakiki on 11/2/15.
 */
public class FbPageCategory {
    private final String id;
    private final String name;

    public FbPageCategory(String id, String name) {
        this.id = id;
        this.name = name;
    }

    @Override
    public String toString() {
        return String.format("[id:%s, name:%s]", id, name);
    }
}

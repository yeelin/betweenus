package com.example.yeelin.projects.betweenus.loader;

/**
 * Created by ninjakiki on 7/20/15.
 */
public enum LoaderId {
    MULTI_PLACES(100),
    SINGLE_PLACE(200);

    private int value;
    LoaderId (int value) {
        this.value = value;
    }

    public int getValue() {
        return this.value;
    }

    public static LoaderId getLoaderIdForInt(int i) {
        for (LoaderId idType: values()) {
            if (i == idType.getValue()) {
                return idType;
            }
        }
        return null;
    }

}

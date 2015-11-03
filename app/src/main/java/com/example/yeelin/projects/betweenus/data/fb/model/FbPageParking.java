package com.example.yeelin.projects.betweenus.data.fb.model;

/**
 * Created by ninjakiki on 11/2/15.
 */
public class FbPageParking {
    private final int lot;
    private final int street;
    private final int valet;

    public FbPageParking(int lot, int street, int valet) {
        this.lot = lot;
        this.street = street;
        this.valet = valet;
    }

    @Override
    public String toString() {
        return String.format("[lot:%d, street:%d, valet:%d]", lot, street, valet);
    }
}

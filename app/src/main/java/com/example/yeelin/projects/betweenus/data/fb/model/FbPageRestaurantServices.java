package com.example.yeelin.projects.betweenus.data.fb.model;

/**
 * Created by ninjakiki on 11/2/15.
 */
public class FbPageRestaurantServices {
    private final int catering;
    private final int delivery;
    private final int groups;
    private final int kids;
    private final int outdoor;
    private final int reserve;
    private final int takeout;
    private final int waiter;
    private final int walkins;

    public FbPageRestaurantServices(int catering, int delivery, int groups, int kids, int outdoor, int reserve, int takeout, int waiter, int walkins) {
        this.catering = catering;
        this.delivery = delivery;
        this.groups = groups;
        this.kids = kids;
        this.outdoor = outdoor;
        this.reserve = reserve;
        this.takeout = takeout;
        this.waiter = waiter;
        this.walkins = walkins;
    }

    @Override
    public String toString() {
        return String.format("[catering:%d, delivery:%d, groups:%d, kids:%d, outdoor:%d, Reserve:%d, Takeout:%s, Waiter:%s, Walkins:%d]",
                catering, delivery, groups, kids, outdoor, reserve, takeout, waiter, walkins);
    }
}

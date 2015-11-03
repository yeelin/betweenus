package com.example.yeelin.projects.betweenus.data.fb.model;

/**
 * Created by ninjakiki on 11/2/15.
 */
public class FbPageRestaurantSpecialties {
    private final int breakfast;
    private final int coffee;
    private final int dinner;
    private final int drinks;
    private final int lunch;

    public FbPageRestaurantSpecialties(int breakfast, int coffee, int dinner, int drinks, int lunch) {
        this.breakfast = breakfast;
        this.coffee = coffee;
        this.dinner = dinner;
        this.drinks = drinks;
        this.lunch = lunch;
    }

    @Override
    public String toString() {
        return String.format("[breakfast:%d, coffee:%d, dinner:%d, drinks:%d, lunch:%d]",
                breakfast, coffee, dinner, drinks, lunch);
    }
}

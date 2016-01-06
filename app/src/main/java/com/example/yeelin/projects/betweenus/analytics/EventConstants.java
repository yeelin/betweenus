package com.example.yeelin.projects.betweenus.analytics;

/**
 * Created by ninjakiki on 12/22/15.
 */
public class EventConstants {

    /**
     * Searched for places
     *
     * Questions answered:
     * 1. How many searches do users perform?
     *
     * What to log: event name
     */
    public static final String EVENT_NAME_SEARCH = "btwn_search";

    /**
     * Switched views
     * 1. List view to map view
     * 2. List view to pager view
     * 3. Map view to list view
     * 4. Map view to pager view
     * 5. Pager view to detail map view
     *
     * Questions answered:
     * 1. Which views do users use the most: list, map, pager?
     * 2. Do users like the detail map view?
     * 3. Which view change if any leads to conversion?
     *
     * What to log: event name, source view, destination view
     */
    public static final String EVENT_NAME_SWITCHED_VIEWS = "btwn_switched_views";
    public static final String EVENT_PARAM_SOURCE_VIEW = "btwn_source_view";
    public static final String EVENT_PARAM_DESTINATION_VIEW = "btwn_destination_view";
    public static final String EVENT_PARAM_VIEW_LIST = "list";
    public static final String EVENT_PARAM_VIEW_MAP = "map";
    public static final String EVENT_PARAM_VIEW_PAGER = "pager";
    public static final String EVENT_PARAM_VIEW_DETAIL_MAP = "detail_map";

    /**
     * Selected a place
     * 1. From the list view
     * 2. From the pager view
     *
     * Questions answered:
     * 1. Where does selection typically happen?
     * 2. What types of places are selected? price, likes, checkins, rating, review count
     *
     * What to log: event name, source view, price, likes, checkins, rating, review count
     */
    public static final String EVENT_NAME_ADDED_TO_SELECTION = "btwn_added_to_selection";
    public static final String EVENT_PARAM_SELECTION_VIEW = "btwn_selection_view";
    public static final String EVENT_PARAM_SELECTION_DATA_SOURCE = "btwn_selection_data_source";
    public static final String EVENT_PARAM_SELECTION_PRICE = "btwn_selection_price";
    public static final String EVENT_PARAM_SELECTION_LIKES = "btwn_selection_likes";
    public static final String EVENT_PARAM_SELECTION_CHECKINS = "btwn_selection_checkins";
    public static final String EVENT_PARAM_SELECTION_RATING = "btwn_selection_rating";
    public static final String EVENT_PARAM_SELECTION_REVIEWS = "btwn_selection_reviews";

    /**
     * Viewed photos
     *
     * Question answered:
     * 1. How many photos do users typically view?
     * 2. Does photo viewing have any impact on invites actually sent (conversion)?
     *
     * What to log: event name, number of photos viewed
     */
    public static final String EVENT_NAME_VIEWED_PHOTOS = "btwn_viewed_photos";
    public static final String EVENT_PARAM_NUM_PHOTOS_VIEWED = "btwn_num_photos_viewed";

    /**
     * Completed invite
     *
     * Questions answered:
     * 1. Where did users initiate the invite?
     * 2. What is the typical delivery method?
     * 3. How many places do users typically select?
     * 4. What is the low/median/high price? TODO
     * 5. What is the low/median/high like count? TODO
     * 6. What is the low/median/high checkin count?
     * 7. What is the low/median/high rating (yelp)? TODO
     * 8. What is the low/median/high review count (yelp)? TODO
     * 9. How many users who initiated the invite actually sent the invite? *** TODO
     *
     * What to log: event name, delivery method (email or text), # selected, ratings, price, review counts,
     * initiated view
     */
    public static final String EVENT_NAME_COMPLETED_INVITE = "btwn_completed_invite";
    public static final String EVENT_PARAM_DELIVERY_METHOD = "btwn_delivery_method";
    public static final String EVENT_PARAM_DELIVER_BY_TXT = "btwn_deliver_by_txt";
    public static final String EVENT_PARAM_DELIVER_BY_EMAIL = "btwn_delivery_by_email";
    public static final String EVENT_PARAM_NUM_PLACES_SELECTED = "btwn_num_places_selected";

    /**
     * Initiated invite
     *
     * Questions answered:
     * 1. Where did users initiate the invite?
     * 2. How many places do users typically select?
     * 3. What is the low/median/high price? TODO
     * 4. What is the low/median/high like count? TODO
     * 5. What is the low/median/high checkin count?
     * 6. What is the low/median/high rating (yelp)? TODO
     * 7. What is the low/median/high review count (yelp)? TODO
     *
     * What to log: event name, # selected, ratings, price, review counts,
     * initiated view
     */
    public static final String EVENT_NAME_INITIATED_INVITE = "btwn_initiated_invite";
    public static final String EVENT_PARAM_INITIATED_VIEW = "btwn_initiated_view";

    /**
     * Viewed People location
     *
     * Questions answered:
     * 1. Do users turn on people location?
     * 2. Do users turn off people location?
     * 3. Does having people location on or off affect conversion? ** TODO
     *
     * What to log: event name, turn on or off
     */
    public static final String EVENT_NAME_VIEWED_PEOPLE_LOCATION = "btwn_viewed_people_location";
    public static final String EVENT_PARAM_SWITCHED_PEOPLE_LOCATION = "btwn_switched_people_location";

    /**
     * Logged in on Fb
     *
     * Questions answered
     * 1. How many logged in?
     * 2. How many skipped logging in?
     * 2. How many are not logged in? TODO
     * 3. How many were logged in and then logged out? TODO
     *
     * What to log: event name, on or off
     */
    public static final String EVENT_NAME_LOGIN = "btwn_login";
    public static final String EVENT_NAME_LOGIN_SKIP = "btwn_login_skip";
    public static final String EVENT_NAME_LOGOUT = "btwn_logout";
}

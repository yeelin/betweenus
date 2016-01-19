package com.example.yeelin.projects.betweenus.data.fb.query;

import android.text.TextUtils;

/**
 * Created by ninjakiki on 10/27/15.
 */
public abstract class FbConstants {
    /**
     * Endpoints
     */
    static class Endpoints {
        public static final String SEARCH = "/search";
        public static final String PHOTOS = "/photos";
    }

    /**
     * Parameter names
     */
    static class ParamNames {
        //query
        public static final String QUERY = "q";
        //type
        public static final String TYPE = "type";
        //center
        public static final String CENTER = "center";
        //distance
        public static final String DISTANCE = "distance";
        //limit - number of results per page
        public static final String LIMIT = "limit";
        //fields
        public static final String FIELDS = "fields";
        //after
        public static final String AFTER = "after";
    }

    /**
     * Parameter values
     */
    static class ParamValues {
        //type
        public static final String TYPE_PLACE = "place";
        public static final String TYPE_UPLOADED = "uploaded";

        //fields
        private static final String ID = "id";
        private static final String ABOUT = "about";
        private static final String ATTIRE = "attire";
        private static final String CATEGORY = "category";
        private static final String CATEGORY_LIST = "category_list";
        private static final String COVER = "cover";
        private static final String CULINARY_TEAM = "culinary_team";

        private static final String DESCRIPTION = "description";
        private static final String FOOD_STYLES = "food_styles";
        private static final String GENERAL_INFO = "general_info";
        private static final String HOURS = "hours";
        private static final String IS_ALWAYS_OPEN = "is_always_open";
        private static final String LINK = "link";

        private static final String LOCATION = "location";
        private static final String NAME = "name";
        private static final String PARKING = "parking";
        private static final String PAYMENT_OPTIONS = "payment_options";
        private static final String PHONE = "phone";

        private static final String PICTURE = "picture";
        private static final String PICTURE_WIDTH = "width";
        private static final String PICTURE_HEIGHT = "height";

        private static final String PRICE_RANGE = "price_range";
        private static final String PUBLIC_TRANSIT = "public_transit";
        private static final String RESTAURANT_SERVICES = "restaurant_services";
        private static final String RESTAURANT_SPECIALTIES = "restaurant_specialties";

        private static final String WEBSITE = "website";
        private static final String CHECKINS = "checkins";
        private static final String LIKES = "likes";

        private static final String PHOTOS = "photos";
        private static final String PHOTO_WIDTH = "width";
        private static final String PHOTO_HEIGHT = "height";
        private static final String IMAGES = "images";
        private static final String CREATED_TIME = "created_time";
        private static final String ORDER = "order";
        private static final String ORDER_REVERSE_CHRONO = "reverse_chronological";

        /**
         * Build fields parameters for list/map query.
         * @param pictureHeightPx
         * @param pictureWidthPx
         * @return
         */
        public static String buildSimpleFields(int pictureHeightPx, int pictureWidthPx) {
            String pictureWithParams = String.format("%s.%s(%d).%s(%d)",
                    PICTURE, PICTURE_HEIGHT, pictureHeightPx, PICTURE_WIDTH, pictureWidthPx);

            return TextUtils.join(",",
                    new String[]{ID, CATEGORY, CATEGORY_LIST, LOCATION, NAME, PHONE,
                            pictureWithParams,
                            PRICE_RANGE, WEBSITE, LINK, CHECKINS, LIKES});
        }

        /**
         * Build fields parameters for detail page query.
         * @param pictureHeightPx
         * @param pictureWidthPx
         * @return
         */
        public static String buildDetailFields(int pictureHeightPx, int pictureWidthPx) {
            String pictureWithParams = String.format("%s.%s(%d).%s(%d)",
                    PICTURE, PICTURE_HEIGHT, pictureHeightPx, PICTURE_WIDTH, pictureWidthPx);

//            String photosWithParamsAndFields = String.format("%s.%s(%s){%s.%s(%s)}",
//                    PHOTOS, ParamNames.TYPE, TYPE_UPLOADED,
//                    TextUtils.join(",", new String[]{ID, NAME, PHOTO_HEIGHT, PHOTO_WIDTH, IMAGES, CREATED_TIME}),
//                    ORDER, ORDER_REVERSE_CHRONO);

            return TextUtils.join(",",
                    new String[]{ID, ABOUT, ATTIRE, CATEGORY, CATEGORY_LIST, COVER, CULINARY_TEAM,
                            DESCRIPTION, FOOD_STYLES, GENERAL_INFO, HOURS, IS_ALWAYS_OPEN, LINK,
                            LOCATION, NAME, PARKING, PAYMENT_OPTIONS, PHONE,
                            pictureWithParams,
                            PRICE_RANGE, PUBLIC_TRANSIT, RESTAURANT_SERVICES, RESTAURANT_SPECIALTIES,
                            WEBSITE, CHECKINS, LIKES});
                            //photosWithParamsAndFields});
        }

        /**
         * Build fields parameters for photos pager query
         * @return
         */
        public static String buildPhotosFields() {
            return String.format("%s.%s(%s)",
                    TextUtils.join(",", new String[]{ID, NAME, PHOTO_HEIGHT, PHOTO_WIDTH, IMAGES, CREATED_TIME}),
                    ORDER,
                    ORDER_REVERSE_CHRONO);
        }
    }

    static class Response {
        //desirable categories
        public static final String CATEGORY_RESTAURANT = "restaurant";
        public static final String CATEGORY_LOCAL = "local";
    }
}




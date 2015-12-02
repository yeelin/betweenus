package com.example.yeelin.projects.betweenus.data;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 11/25/15.
 */
public interface LocalPhotosResult {
    ArrayList<LocalPhoto> getLocalPhotos();

    String getAfterId();

    String getNextUrl();
}

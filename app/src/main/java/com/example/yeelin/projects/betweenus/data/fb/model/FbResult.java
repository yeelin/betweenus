package com.example.yeelin.projects.betweenus.data.fb.model;

import android.support.annotation.Nullable;

import com.example.yeelin.projects.betweenus.data.LocalBusiness;
import com.example.yeelin.projects.betweenus.data.LocalConstants;
import com.example.yeelin.projects.betweenus.data.LocalResult;
import com.google.android.gms.maps.model.LatLng;

import java.util.ArrayList;

/**
 * Created by ninjakiki on 10/27/15.
 */
public class FbResult implements LocalResult {
    private final ArrayList<FbPage> data;
    private final FbPagination paging;

    public FbResult(ArrayList<FbPage> pages, FbPagination paging) {
        data = pages;
        this.paging = paging;
    }

    public FbResult(ArrayList<FbPage> currentPages, ArrayList<FbPage> newPages, String previous, String next, String before, String after) {
        data = new ArrayList<>(currentPages.size() + newPages.size());
        data.addAll(currentPages);
        data.addAll(newPages);

        paging = new FbPagination(previous, next, before, after);
    }

    public ArrayList<FbPage> getPages() {
        return data;
    }

    public String toString() {
        return String.format("Paging:%s, Size:%d, Data:%s", paging, data.size(), data);
    }

    @Override
    public ArrayList<LocalBusiness> getLocalBusinesses() {
        ArrayList<LocalBusiness> localBusinesses = new ArrayList<>(data.size());
        for (int i=0; i<data.size(); i++) {
            localBusinesses.add(data.get(i));
        }
        return localBusinesses;
    }

    @Nullable
    public FbPagination getPaging() { return paging; }

    @Nullable
    public String getPreviousUrl() { return paging != null ? paging.getPrevious() : null; }

    @Nullable
    @Override
    public String getNextUrl() { return paging != null ? paging.getNext() : null; }

    @Nullable
    @Override
    public String getAfterId() { return paging != null && paging.getCursors() != null ? paging.getCursors().getAfter() : null; }

    @Nullable
    public String getBeforeId() { return paging != null && paging.getCursors() != null ? paging.getCursors().getBefore() : null; }

    @Override
    public LatLng getResultCenter() {
        return null;
    }

    @Override
    public double getResultLatitudeDelta() {
        return LocalConstants.NO_DATA_DOUBLE;
    }

    @Override
    public double getResultLongitudeDelta() {
        return LocalConstants.NO_DATA_DOUBLE;
    }

    @Override
    public int getDataSource() {
        return LocalConstants.FACEBOOK;
    }
}

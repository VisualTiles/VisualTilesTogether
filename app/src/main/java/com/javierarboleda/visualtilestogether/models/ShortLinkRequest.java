package com.javierarboleda.visualtilestogether.models;

import com.google.gson.annotations.SerializedName;

/**
 * Object to request data from Firebase URL Shortener.
 * Source: https://firebase.google.com/docs/dynamic-links/short-links
 * Created by chris on 12/11/16.
 */

public class ShortLinkRequest {
    @SerializedName("longDynamicLink")
    public String longDynamicLink;
}

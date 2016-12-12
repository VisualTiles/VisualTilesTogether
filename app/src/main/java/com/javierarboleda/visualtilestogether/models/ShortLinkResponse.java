package com.javierarboleda.visualtilestogether.models;

import com.google.gson.annotations.SerializedName;

import java.util.List;

/**
 * Created by chris on 12/11/16.
 */

public class ShortLinkResponse {
    @SerializedName("shortLink")
    public String shortLink;

    @SerializedName("previewLink")
    public String previewLink;

    @SerializedName("warning")
    public List<String> warningList;
}

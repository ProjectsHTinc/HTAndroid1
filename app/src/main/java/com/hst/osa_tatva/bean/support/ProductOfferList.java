package com.hst.osa_tatva.bean.support;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class ProductOfferList {

    @SerializedName("count")
    @Expose
    private int count;
    @SerializedName("offer_list")
    @Expose
    private ArrayList<ProductOffer> productArrayList = new ArrayList<>();

    /**
     * @return The count
     */
    public int getCount() {
        return count;
    }

    /**
     * @param count The count
     */
    public void setCount(int count) {
        this.count = count;
    }

    /**
     * @return The popularProductArrayList
     */
    public ArrayList<ProductOffer> getProductArrayList() {
        return productArrayList;
    }

    /**
     * @param productArrayList The popularProductArrayList
     */
    public void setProductArrayList(ArrayList<ProductOffer> productArrayList) {
        this.productArrayList = productArrayList;
    }
}

package com.hst.osa_tatva.bean.support;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SubProductList {

    @SerializedName("count")
    @Expose
    private int count;
    @SerializedName("product_list")
    @Expose
    private ArrayList<Product> productArrayList = new ArrayList<>();

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
    public ArrayList<Product> getProductArrayList() {
        return productArrayList;
    }

    /**
     * @param productArrayList The popularProductArrayList
     */
    public void setProductArrayList(ArrayList<Product> productArrayList) {
        this.productArrayList = productArrayList;
    }
}

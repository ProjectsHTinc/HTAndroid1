package com.hst.osa_tatva.bean.support;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;

import java.util.ArrayList;

public class SubCategoryList {

    @SerializedName("count")
    @Expose
    private int count;
    @SerializedName("sub_category_list")
    @Expose
    private ArrayList<Category> categoryArrayList = new ArrayList<>();

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
     * @return The categoryArrayList
     */
    public ArrayList<Category> getCategoryArrayList() {
        return categoryArrayList;
    }

    /**
     * @param categoryArrayList The categoryArrayList
     */
    public void setCategoryArrayList(ArrayList<Category> categoryArrayList) {
        this.categoryArrayList = categoryArrayList;
    }
}

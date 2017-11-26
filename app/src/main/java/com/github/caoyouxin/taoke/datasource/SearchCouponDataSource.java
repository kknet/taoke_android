package com.github.caoyouxin.taoke.datasource;

import android.content.Context;

import com.github.caoyouxin.taoke.api.TaoKeApi;
import com.github.caoyouxin.taoke.model.CouponItem;
import com.github.gnastnosaj.boilerplate.mvchelper.RxDataSource;
import com.shizhefei.mvc.IDataCacheLoader;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import io.reactivex.Observable;


public class SearchCouponDataSource extends SortableCouponDataSource {

    private String keyword;

    public SearchCouponDataSource(Context context) {
        super(context);
    }

    @Override
    protected Observable<List<CouponItem>> fetchDataUnderlay() throws Exception {
        return TaoKeApi.getSearchList(this.keyword);
    }

    public SearchCouponDataSource setKeyword(String keyword) {
        this.keyword = keyword;
        return this;
    }

}
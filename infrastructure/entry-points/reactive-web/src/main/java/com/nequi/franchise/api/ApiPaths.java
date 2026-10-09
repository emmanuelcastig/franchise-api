package com.nequi.franchise.api;

import lombok.experimental.UtilityClass;

@UtilityClass
public class ApiPaths {

    public static final String FRANCHISE_ID = "franchiseId";
    public static final String BRANCH_ID = "branchId";
    public static final String PRODUCT_ID = "productId";

    public static final String FRANCHISES = "/api/v1/franchises";
    public static final String FRANCHISE_NAME = FRANCHISES + "/{franchiseId}/name";
    public static final String TOP_STOCK_PRODUCTS = FRANCHISES + "/{franchiseId}/top-stock-products";

    public static final String BRANCHES = FRANCHISES + "/{franchiseId}/branches";
    public static final String BRANCH_NAME = BRANCHES + "/{branchId}/name";

    public static final String PRODUCTS = BRANCHES + "/{branchId}/products";
    public static final String PRODUCT = PRODUCTS + "/{productId}";
    public static final String PRODUCT_NAME = PRODUCT + "/name";
    public static final String PRODUCT_STOCK = PRODUCT + "/stock";
}

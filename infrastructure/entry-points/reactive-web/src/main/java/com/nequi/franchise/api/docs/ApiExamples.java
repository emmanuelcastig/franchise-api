package com.nequi.franchise.api.docs;

import lombok.experimental.UtilityClass;

/**
 * Request and response examples rendered by Swagger UI.
 */
@UtilityClass
public class ApiExamples {

    public static final String FRANCHISE_NAME_REQUEST = """
            { "name": "Burger Town" }
            """;

    public static final String BRANCH_NAME_REQUEST = """
            { "name": "Downtown" }
            """;

    public static final String PRODUCT_NAME_REQUEST = """
            { "name": "Double Cheeseburger" }
            """;

    public static final String CREATE_PRODUCT_REQUEST = """
            { "name": "Cheeseburger", "stock": 25 }
            """;

    public static final String STOCK_REQUEST = """
            { "stock": 40 }
            """;

    public static final String FRANCHISE_RESPONSE = """
            { "id": "4f6c2b1e-8d3a-4c47-9a51-0b2f6e3d7c10", "name": "Burger Town" }
            """;

    public static final String BRANCH_RESPONSE = """
            {
              "id": "9b1d7c3e-2f4a-4e8b-8c6d-5a7f9e0b1c23",
              "franchiseId": "4f6c2b1e-8d3a-4c47-9a51-0b2f6e3d7c10",
              "name": "Downtown"
            }
            """;

    public static final String PRODUCT_RESPONSE = """
            {
              "id": "c3e8a1f2-7b5d-4a9e-b2c4-6d8f0a1e3b57",
              "branchId": "9b1d7c3e-2f4a-4e8b-8c6d-5a7f9e0b1c23",
              "name": "Cheeseburger",
              "stock": 25
            }
            """;

    public static final String TOP_STOCK_PRODUCTS_RESPONSE = """
            [
              {
                "branchId": "9b1d7c3e-2f4a-4e8b-8c6d-5a7f9e0b1c23",
                "branchName": "Downtown",
                "productId": "c3e8a1f2-7b5d-4a9e-b2c4-6d8f0a1e3b57",
                "productName": "Fries",
                "stock": 120
              },
              {
                "branchId": "1a2b3c4d-5e6f-4a7b-8c9d-0e1f2a3b4c5d",
                "branchName": "Uptown",
                "productId": "6f5e4d3c-2b1a-4f9e-8d7c-6b5a4f3e2d1c",
                "productName": "Soda",
                "stock": 80
              }
            ]
            """;

    public static final String INVALID_REQUEST = """
            {
              "title": "Bad Request",
              "status": 400,
              "detail": "The request is invalid, please verify the data",
              "instance": "/api/v1/franchises",
              "code": "FRN-400",
              "timestamp": "2026-10-08T15:30:00Z",
              "errors": [ { "field": "name", "message": "must not be blank" } ]
            }
            """;

    public static final String FRANCHISE_NOT_FOUND = """
            {
              "title": "Not Found",
              "status": 404,
              "detail": "The franchise does not exist",
              "instance": "/api/v1/franchises/4f6c2b1e-8d3a-4c47-9a51-0b2f6e3d7c10/branches",
              "code": "FRN-404",
              "timestamp": "2026-10-08T15:30:00Z"
            }
            """;

    public static final String BRANCH_NOT_FOUND = """
            {
              "title": "Not Found",
              "status": 404,
              "detail": "The branch does not exist in the given franchise",
              "instance": "/api/v1/franchises/4f6c2b1e-8d3a-4c47-9a51-0b2f6e3d7c10/branches/9b1d7c3e-2f4a-4e8b-8c6d-5a7f9e0b1c23/products",
              "code": "FRN-405",
              "timestamp": "2026-10-08T15:30:00Z"
            }
            """;

    public static final String PRODUCT_NOT_FOUND = """
            {
              "title": "Not Found",
              "status": 404,
              "detail": "The product does not exist in the given branch",
              "instance": "/api/v1/franchises/4f6c2b1e-8d3a-4c47-9a51-0b2f6e3d7c10/branches/9b1d7c3e-2f4a-4e8b-8c6d-5a7f9e0b1c23/products/c3e8a1f2-7b5d-4a9e-b2c4-6d8f0a1e3b57",
              "code": "FRN-406",
              "timestamp": "2026-10-08T15:30:00Z"
            }
            """;

    public static final String FRANCHISE_NAME_CONFLICT = """
            {
              "title": "Conflict",
              "status": 409,
              "detail": "A franchise with the same name already exists",
              "instance": "/api/v1/franchises",
              "code": "FRN-409",
              "timestamp": "2026-10-08T15:30:00Z"
            }
            """;

    public static final String BRANCH_NAME_CONFLICT = """
            {
              "title": "Conflict",
              "status": 409,
              "detail": "A branch with the same name already exists in the franchise",
              "instance": "/api/v1/franchises/4f6c2b1e-8d3a-4c47-9a51-0b2f6e3d7c10/branches",
              "code": "FRN-410",
              "timestamp": "2026-10-08T15:30:00Z"
            }
            """;

    public static final String PRODUCT_NAME_CONFLICT = """
            {
              "title": "Conflict",
              "status": 409,
              "detail": "A product with the same name already exists in the branch",
              "instance": "/api/v1/franchises/4f6c2b1e-8d3a-4c47-9a51-0b2f6e3d7c10/branches/9b1d7c3e-2f4a-4e8b-8c6d-5a7f9e0b1c23/products",
              "code": "FRN-411",
              "timestamp": "2026-10-08T15:30:00Z"
            }
            """;

    public static final String SERVICE_UNAVAILABLE = """
            {
              "title": "Service Unavailable",
              "status": 503,
              "detail": "The service is temporarily unavailable, please try again later",
              "instance": "/api/v1/franchises",
              "code": "FRN-503",
              "timestamp": "2026-10-08T15:30:00Z"
            }
            """;
}

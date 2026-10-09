package com.nequi.franchise.api.utils;

import lombok.experimental.UtilityClass;

@UtilityClass
public class Constants {

    @UtilityClass
    public static class Headers {
        public static final String X_MESSAGE_ID = "x-message-id";
    }

    @UtilityClass
    public static class LogKeys {
        public static final String REQUEST = "request";
        public static final String RESPONSE = "response";
        public static final String METHOD = "method";
        public static final String PATH = "path";
        public static final String CODE = "code";
        public static final String ERRORS = "errors";
        public static final String ERROR = "error";
    }

    @UtilityClass
    public static class LogMessages {

        @UtilityClass
        public static class CreateFranchise {
            public static final String REQUEST = "Create Franchise Handler [REQUEST]";
            public static final String RESPONSE = "Create Franchise Handler [RESPONSE]";
        }

        @UtilityClass
        public static class RenameFranchise {
            public static final String REQUEST = "Rename Franchise Handler [REQUEST]";
            public static final String RESPONSE = "Rename Franchise Handler [RESPONSE]";
        }

        @UtilityClass
        public static class TopStockProducts {
            public static final String REQUEST = "Top Stock Products Handler [REQUEST]";
            public static final String RESPONSE = "Top Stock Products Handler [RESPONSE]";
        }

        @UtilityClass
        public static class AddBranch {
            public static final String REQUEST = "Add Branch Handler [REQUEST]";
            public static final String RESPONSE = "Add Branch Handler [RESPONSE]";
        }

        @UtilityClass
        public static class RenameBranch {
            public static final String REQUEST = "Rename Branch Handler [REQUEST]";
            public static final String RESPONSE = "Rename Branch Handler [RESPONSE]";
        }

        @UtilityClass
        public static class AddProduct {
            public static final String REQUEST = "Add Product Handler [REQUEST]";
            public static final String RESPONSE = "Add Product Handler [RESPONSE]";
        }

        @UtilityClass
        public static class RemoveProduct {
            public static final String REQUEST = "Remove Product Handler [REQUEST]";
            public static final String RESPONSE = "Remove Product Handler [RESPONSE]";
        }

        @UtilityClass
        public static class UpdateStock {
            public static final String REQUEST = "Update Stock Handler [REQUEST]";
            public static final String RESPONSE = "Update Stock Handler [RESPONSE]";
        }

        @UtilityClass
        public static class RenameProduct {
            public static final String REQUEST = "Rename Product Handler [REQUEST]";
            public static final String RESPONSE = "Rename Product Handler [RESPONSE]";
        }

        @UtilityClass
        public static class Errors {
            public static final String BUSINESS = "Franchise API Handler [BUSINESS ERROR]";
            public static final String TECHNICAL = "Franchise API Handler [TECHNICAL ERROR]";
            public static final String INVALID_REQUEST = "Franchise API Handler [INVALID REQUEST]";
            public static final String REJECTED_REQUEST = "Franchise API Handler [REJECTED REQUEST]";
            public static final String UNEXPECTED = "Franchise API Handler [UNEXPECTED ERROR]";
        }
    }
}

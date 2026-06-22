package com.efmbank.cards.controller;

final class TestUri {

    static final String AUTH_LOGIN = "/api/v1/auth/login";

    static final String ADMIN_USERS = "/api/v1/admin/users";
    static final String ADMIN_USER_BY_ID = "/api/v1/admin/users/{id}";
    static final String ADMIN_USER_ENABLE = "/api/v1/admin/users/{id}/enable";
    static final String ADMIN_USER_DISABLE = "/api/v1/admin/users/{id}/disable";

    static final String ADMIN_CARDS = "/api/v1/admin/cards";
    static final String ADMIN_CARD_BY_PUBLIC_ID = "/api/v1/admin/cards/{publicId}";
    static final String ADMIN_CARD_ACTIVATE = "/api/v1/admin/cards/{publicId}/activate";
    static final String ADMIN_CARD_BLOCK = "/api/v1/admin/cards/{publicId}/block";

    static final String USER_CARDS = "/api/v1/cards";
    static final String USER_CARD_BY_PUBLIC_ID = "/api/v1/cards/{publicId}";
    static final String USER_CARD_BALANCE = "/api/v1/cards/{publicId}/balance";

    static final String TRANSFERS = "/api/v1/transfers";

    static final String BLOCK_REQUEST_FOR_CARD = "/api/v1/card-block-requests/cards/{cardPublicId}";
    static final String BLOCK_REQUEST_MY = "/api/v1/card-block-requests/my";
    static final String BLOCK_REQUEST_REQUESTED = "/api/v1/card-block-requests/requested";
    static final String BLOCK_REQUEST_APPROVE = "/api/v1/card-block-requests/{requestId}/approve";
    static final String BLOCK_REQUEST_REJECT = "/api/v1/card-block-requests/{requestId}/reject";

    private TestUri() {
    }
}

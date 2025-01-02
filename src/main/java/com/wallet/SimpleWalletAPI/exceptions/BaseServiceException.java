package com.wallet.SimpleWalletAPI.exceptions;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class BaseServiceException extends RuntimeException {
    private String message;
    private int statusCode;

    public BaseServiceException(String message, int statusCode) {
        super(message);
        this.message = message;
        this.statusCode = statusCode;
    }
}
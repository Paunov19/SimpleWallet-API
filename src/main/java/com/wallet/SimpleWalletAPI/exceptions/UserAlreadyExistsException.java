package com.wallet.SimpleWalletAPI.exceptions;

import org.springframework.http.HttpStatus;

public class UserAlreadyExistsException extends BaseServiceException {
    public UserAlreadyExistsException(String userEmail) {
        super("User with email " + userEmail + "already exists", HttpStatus.BAD_REQUEST.value());
    }
}
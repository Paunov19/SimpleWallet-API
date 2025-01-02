package com.wallet.SimpleWalletAPI.exceptions;

import org.springframework.http.HttpStatus;

public class InsufficientFundsException extends BaseServiceException {
    public InsufficientFundsException(String walletCode) {
        super("Insufficient funds in wallet: " + walletCode, HttpStatus.BAD_REQUEST.value());
    }
}
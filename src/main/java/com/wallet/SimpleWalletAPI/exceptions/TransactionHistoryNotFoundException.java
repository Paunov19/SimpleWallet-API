package com.wallet.SimpleWalletAPI.exceptions;

import org.springframework.http.HttpStatus;

public class TransactionHistoryNotFoundException extends BaseServiceException{
    public TransactionHistoryNotFoundException(String content) {
        super("No transaction history found for " + content, HttpStatus.NOT_FOUND.value());
    }
}
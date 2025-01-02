package com.wallet.SimpleWalletAPI.exceptions;

import org.springframework.http.HttpStatus;

public class WalletNotFoundException extends BaseServiceException {
    public WalletNotFoundException(String walletCode) {
        super("Wallet with code " + walletCode + " not found or access denied", HttpStatus.NOT_FOUND.value());
    }
}
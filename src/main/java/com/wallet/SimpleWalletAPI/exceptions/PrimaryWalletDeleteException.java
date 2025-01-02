package com.wallet.SimpleWalletAPI.exceptions;

import org.springframework.http.HttpStatus;

public class PrimaryWalletDeleteException extends BaseServiceException {
    public PrimaryWalletDeleteException() {
        super("Cannot delete the primary wallet", HttpStatus.FORBIDDEN.value());
    }
}
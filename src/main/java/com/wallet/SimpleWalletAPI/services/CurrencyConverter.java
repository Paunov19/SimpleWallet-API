package com.wallet.SimpleWalletAPI.services;

import java.math.BigDecimal;

public interface CurrencyConverter {
    BigDecimal convert(BigDecimal amount, String from, String to);
}
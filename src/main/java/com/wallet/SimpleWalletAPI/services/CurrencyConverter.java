package com.wallet.SimpleWalletAPI.services;

import com.wallet.SimpleWalletAPI.models.Currency;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;

@Service
public class CurrencyConverter {
    private static final BigDecimal BGN_TO_EUR = BigDecimal.valueOf(0.51);
    private static final BigDecimal BGN_TO_USD = BigDecimal.valueOf(0.57);
    private static final BigDecimal EUR_TO_BGN = BigDecimal.valueOf(1.96);
    private static final BigDecimal EUR_TO_USD = BigDecimal.valueOf(1.12);
    private static final BigDecimal USD_TO_BGN = BigDecimal.valueOf(1.75);
    private static final BigDecimal USD_TO_EUR = BigDecimal.valueOf(0.89);

    public BigDecimal convert(BigDecimal amount, Currency from, Currency to) {
        if (from == to) {
            return amount;
        }

        switch (from) {
            case BGN:
                if (to == Currency.EURO) return amount.multiply(BGN_TO_EUR);
                if (to == Currency.USD) return amount.multiply(BGN_TO_USD);
                break;
            case EURO:
                if (to == Currency.BGN) return amount.multiply(EUR_TO_BGN);
                if (to == Currency.USD) return amount.multiply(EUR_TO_USD);
                break;
            case USD:
                if (to == Currency.BGN) return amount.multiply(USD_TO_BGN);
                if (to == Currency.EURO) return amount.multiply(USD_TO_EUR);
                break;
        }

        throw new IllegalArgumentException("Unsupported currency conversion: " + from + " to " + to);
    }
}
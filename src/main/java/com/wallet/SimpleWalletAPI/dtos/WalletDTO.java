package com.wallet.SimpleWalletAPI.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class WalletDTO {

    private String walletName;
    private String walletCode;
    private BigDecimal balance;
    private String currency;
    private boolean isPrimary;
}
package com.wallet.SimpleWalletAPI.dtos;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.math.BigDecimal;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class TransactionHistoryDTO {

    private BigDecimal amount;
    private String currency;
    private String transactionType;
    private String timestamp;
    private String walletName;
    private String walletCode;
    private String userName;
    private String description;
}
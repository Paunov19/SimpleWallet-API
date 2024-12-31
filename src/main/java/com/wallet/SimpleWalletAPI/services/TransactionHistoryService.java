package com.wallet.SimpleWalletAPI.services;

import com.wallet.SimpleWalletAPI.models.TransactionHistory;

import java.util.List;

public interface TransactionHistoryService {
    List<TransactionHistory> getAllTransactionsHistoryForCurrentUser();
    List<TransactionHistory> getAllTransactionsHistoryForWallet(String walletCode);
}
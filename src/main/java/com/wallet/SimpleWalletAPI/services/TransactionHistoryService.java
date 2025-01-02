package com.wallet.SimpleWalletAPI.services;

import com.wallet.SimpleWalletAPI.dtos.TransactionHistoryDTO;

import java.util.List;

public interface TransactionHistoryService {
    List<TransactionHistoryDTO> getAllTransactionsHistoryForCurrentUser();
    List<TransactionHistoryDTO> getAllTransactionsHistoryForWallet(String walletCode);
}
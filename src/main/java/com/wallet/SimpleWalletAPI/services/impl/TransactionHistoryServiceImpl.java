package com.wallet.SimpleWalletAPI.services.impl;

import com.wallet.SimpleWalletAPI.models.TransactionHistory;
import com.wallet.SimpleWalletAPI.models.User;
import com.wallet.SimpleWalletAPI.models.Wallet;
import com.wallet.SimpleWalletAPI.repositories.TransactionHistoryRepository;
import com.wallet.SimpleWalletAPI.repositories.WalletRepository;
import com.wallet.SimpleWalletAPI.services.TransactionHistoryService;
import com.wallet.SimpleWalletAPI.services.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TransactionHistoryServiceImpl implements TransactionHistoryService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;

    @Override
    public List<TransactionHistory> getAllTransactionsHistoryForCurrentUser() {
        User user = userService.getCurrentAuthenticatedUser();
        return transactionHistoryRepository.findByUser(user);
    }

    @Override
    public List<TransactionHistory> getAllTransactionsHistoryForWallet(String walletCode) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet wallet = walletRepository.findByWalletCodeAndUser(walletCode, user)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return transactionHistoryRepository.findByWallet(wallet);
    }
}

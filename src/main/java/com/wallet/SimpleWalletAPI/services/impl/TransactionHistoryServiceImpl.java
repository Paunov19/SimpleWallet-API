package com.wallet.SimpleWalletAPI.services.impl;

import com.wallet.SimpleWalletAPI.dtos.TransactionHistoryDTO;
import com.wallet.SimpleWalletAPI.exceptions.TransactionHistoryNotFoundException;
import com.wallet.SimpleWalletAPI.exceptions.WalletNotFoundException;
import com.wallet.SimpleWalletAPI.mappers.TransactionHistoryMapper;
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
import java.util.stream.Collectors;

@Service
public class TransactionHistoryServiceImpl implements TransactionHistoryService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;

    @Autowired
    private TransactionHistoryMapper transactionHistoryMapper;

    @Override
    public List<TransactionHistoryDTO> getAllTransactionsHistoryForCurrentUser() {
        User user = userService.getCurrentAuthenticatedUser();
        List<TransactionHistory> transactionHistoryList = transactionHistoryRepository.findByUser(user);

        if (transactionHistoryList == null ||transactionHistoryList.isEmpty()) {
            throw new TransactionHistoryNotFoundException("the current user: " + user.getEmail());
        }

        return transactionHistoryList.stream()
                .map(transactionHistoryMapper::entityToDto)
                .collect(Collectors.toList());
    }

    @Override
    public List<TransactionHistoryDTO> getAllTransactionsHistoryForWallet(String walletCode) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet wallet = walletRepository.findByWalletCodeAndUser(walletCode, user)
                .orElseThrow(() -> new WalletNotFoundException(walletCode));

        List<TransactionHistory> transactionHistoryList = transactionHistoryRepository.findByWallet(wallet);

        if (transactionHistoryList == null || transactionHistoryList.isEmpty()) {
            throw new TransactionHistoryNotFoundException("wallet with code: " + walletCode);
        }

        return transactionHistoryList.stream()
                .map(transactionHistoryMapper::entityToDto)
                .collect(Collectors.toList());
    }
}
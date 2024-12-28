package com.wallet.SimpleWalletAPI.services;

import com.wallet.SimpleWalletAPI.models.TransactionHistory;
import com.wallet.SimpleWalletAPI.models.Wallet;
import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    Wallet createWallet(String walletName);
    Wallet getWalletDetails(String walletCode);
    List<Wallet> getAllWallets();
    Wallet transferMoneyToAnotherUser(String toWalletCode, BigDecimal amount);
    Wallet transferBetweenOwnWallets(String fromWalletCode, String toWalletCode, BigDecimal amount);
    Wallet depositToPrimaryWallet(BigDecimal amount);
    Wallet withdrawFromPrimaryWallet(BigDecimal amount);
    void deleteWallet(String walletCode);
    List<TransactionHistory> getAllTransactionsHistoryForCurrentUser();
    List<TransactionHistory> getTransactionsHistoryForWallet(String walletCode);
}
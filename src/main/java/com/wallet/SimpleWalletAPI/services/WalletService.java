package com.wallet.SimpleWalletAPI.services;

import com.wallet.SimpleWalletAPI.models.Wallet;
import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    Wallet createWallet(String walletName);
    Wallet getWalletDetails(String walletCode);
    List<Wallet> getAllWallets();
    Wallet depositToPrimaryWallet(BigDecimal amount, String currency);
    Wallet withdrawFromPrimaryWallet(BigDecimal amount, String currency);
    Wallet transferMoneyToAnotherUser(String toWalletCode, BigDecimal amount, String currency);
    Wallet transferBetweenOwnWallets(String fromWalletCode, String toWalletCode, BigDecimal amount, String currency);
    Wallet convertWalletCurrency(String walletCode, String targetCurrency);
    void deleteWallet(String walletCode);
}
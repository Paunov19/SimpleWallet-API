package com.wallet.SimpleWalletAPI.services;

import com.wallet.SimpleWalletAPI.dtos.WalletDTO;
import java.math.BigDecimal;
import java.util.List;

public interface WalletService {
    WalletDTO createWallet(String walletName);
    WalletDTO getWalletDetails(String walletCode);
    List<WalletDTO> getAllWallets();
    WalletDTO depositToPrimaryWallet(BigDecimal amount, String currency);
    WalletDTO withdrawFromPrimaryWallet(BigDecimal amount, String currency);
    WalletDTO transferMoneyToAnotherUser(String toWalletCode, BigDecimal amount, String currency);
    WalletDTO transferBetweenOwnWallets(String fromWalletCode, String toWalletCode, BigDecimal amount, String currency);
    WalletDTO convertWalletCurrency(String walletCode, String targetCurrency);
    void deleteWallet(String walletCode);
}
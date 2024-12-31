package com.wallet.SimpleWalletAPI.services.impl;

import com.wallet.SimpleWalletAPI.models.*;
import com.wallet.SimpleWalletAPI.repositories.TransactionHistoryRepository;
import com.wallet.SimpleWalletAPI.repositories.WalletRepository;
import com.wallet.SimpleWalletAPI.services.CurrencyConverter;
import com.wallet.SimpleWalletAPI.services.UserService;
import com.wallet.SimpleWalletAPI.services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;

    @Autowired
    private CurrencyConverter currencyConverter;

    @Override
    public Wallet createWallet(String walletName) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletName(walletName);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setPrimary(false);
        wallet.setCurrency(Currency.BGN);
        String walletCode = UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").substring(0, 10);
        wallet.setWalletCode(walletCode);
        walletRepository.save(wallet);

        TransactionHistory transaction = new TransactionHistory(null,
                wallet.getBalance(), wallet.getCurrency(), TransactionType.CREATED, LocalDateTime.now(), wallet, user,
                "Created wallet: " + wallet.getWalletName()
        );
        transactionHistoryRepository.save(transaction);

        return wallet;
    }

    @Override
    public Wallet getWalletDetails(String walletCode) {
        User user = userService.getCurrentAuthenticatedUser();
        return walletRepository.findByWalletCodeAndUser(walletCode, user)
                .orElseThrow(() -> new RuntimeException("Wallet not found or access denied"));
    }

    @Override
    public List<Wallet> getAllWallets() {
        User user = userService.getCurrentAuthenticatedUser();
        return user.getWallets();
    }

    @Override
    public Wallet depositToPrimaryWallet(BigDecimal amount, String currency) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet primaryWallet = getUserPrimaryWallet(user);
        if (!primaryWallet.isPrimary()) {
            throw new RuntimeException("Operation allowed only on primary wallet");
        }

        BigDecimal convertedAmount = currencyConverter.convert(amount, currency, primaryWallet.getCurrency().toString());
        primaryWallet.setBalance(primaryWallet.getBalance().add(convertedAmount));

        TransactionHistory transaction = new TransactionHistory(null,
                amount, primaryWallet.getCurrency(), TransactionType.DEPOSIT, LocalDateTime.now(), primaryWallet, user,
                "Deposit to : " + primaryWallet.getWalletName() + " | amount: " + amount + primaryWallet.getCurrency());
        transactionHistoryRepository.save(transaction);

        return walletRepository.save(primaryWallet);
    }

    @Override
    public Wallet withdrawFromPrimaryWallet(BigDecimal amount, String currency) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet primaryWallet = getUserPrimaryWallet(user);
        if (!primaryWallet.isPrimary()) {
            throw new RuntimeException("Operation allowed only on primary wallet");
        }
        BigDecimal convertedAmount = currencyConverter.convert(amount, primaryWallet.getCurrency().toString(), currency);

        if (primaryWallet.getBalance().compareTo(convertedAmount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        primaryWallet.setBalance(primaryWallet.getBalance().subtract(convertedAmount));

        TransactionHistory transaction = new TransactionHistory(null,
                amount, primaryWallet.getCurrency(), TransactionType.WITHDRAW, LocalDateTime.now(), primaryWallet, user,
                "Withdrawal from : " + primaryWallet.getWalletName() + " | amount: " + amount + primaryWallet.getCurrency());
        transactionHistoryRepository.save(transaction);
        return walletRepository.save(primaryWallet);
    }

    @Override
    public Wallet transferMoneyToAnotherUser(String toWalletCode, BigDecimal amount, String currency) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet senderPrimaryWallet = getUserPrimaryWallet(user);

        Wallet targetWallet = walletRepository.findByWalletCode(toWalletCode)
                .orElseThrow(() -> new RuntimeException("Recipient wallet not found"));
        User recipientUser = targetWallet.getUser();
        Wallet recipientPrimaryWallet = getUserPrimaryWallet(recipientUser);

        BigDecimal convertedAmount = currencyConverter.convert(amount, senderPrimaryWallet.getCurrency().toString(), currency);

        if (senderPrimaryWallet.getBalance().compareTo(convertedAmount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        senderPrimaryWallet.setBalance(senderPrimaryWallet.getBalance().subtract(convertedAmount));
        recipientPrimaryWallet.setBalance(recipientPrimaryWallet.getBalance()
                .add(currencyConverter.convert(convertedAmount, senderPrimaryWallet.getCurrency().toString(), recipientPrimaryWallet.getCurrency().toString())));

        walletRepository.save(senderPrimaryWallet);
        walletRepository.save(recipientPrimaryWallet);
//TODO
        TransactionHistory transactionFrom = new TransactionHistory(null,
                amount, senderPrimaryWallet.getCurrency(), TransactionType.TRANSFER_TO_USER, LocalDateTime.now(), senderPrimaryWallet, user,
                "Transferred to user: " + recipientPrimaryWallet.getUser().getName() + " (Wallet: " + recipientPrimaryWallet.getWalletName() + ")"
                        + " | amount: " + amount + senderPrimaryWallet.getCurrency());
        transactionHistoryRepository.save(transactionFrom);

        TransactionHistory transactionTo = new TransactionHistory(null,
                amount, recipientPrimaryWallet.getCurrency(), TransactionType.TRANSFER_FROM_USER, LocalDateTime.now(), recipientPrimaryWallet, recipientPrimaryWallet.getUser(),
                "Received from user: " + senderPrimaryWallet.getUser() + " (Wallet: " + senderPrimaryWallet.getWalletName() + ")"
                        + " | amount: " + amount + recipientPrimaryWallet.getCurrency());
        transactionHistoryRepository.save(transactionTo);

        return recipientPrimaryWallet;
    }

    @Override
    public Wallet transferBetweenOwnWallets(String fromWalletCode, String toWalletCode, BigDecimal amount, String currency) {
        User user = userService.getCurrentAuthenticatedUser();

        Wallet fromWallet = walletRepository.findByWalletCodeAndUser(fromWalletCode, user)
                .orElseThrow(() -> new RuntimeException("Source wallet not found or access denied"));
        Wallet toWallet = walletRepository.findByWalletCodeAndUser(toWalletCode, user)
                .orElseThrow(() -> new RuntimeException("Target wallet not found or access denied"));

        BigDecimal convertedAmount = currencyConverter.convert(amount, fromWallet.getCurrency().toString(), currency);

        if (fromWallet.getBalance().compareTo(convertedAmount) < 0) {
            throw new RuntimeException("Insufficient funds in source wallet");
        }

        fromWallet.setBalance(fromWallet.getBalance().subtract(convertedAmount));
        toWallet.setBalance(toWallet.getBalance()
                .add(currencyConverter.convert(convertedAmount, fromWallet.getCurrency().toString(), toWallet.getCurrency().toString())));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);
//TODO
        TransactionHistory transactionFrom = new TransactionHistory(null,
                amount, fromWallet.getCurrency(), TransactionType.OWN_TRANSFER, LocalDateTime.now(), fromWallet, user,
                "Own transfer from wallet: " + fromWallet.getWalletName() + " (" + fromWallet.getWalletCode() + ")" + " -> "
                        + toWallet.getWalletName() + " (" + toWallet.getWalletCode() + ")" + " | amount: " + amount + fromWallet.getCurrency()
        );
        transactionHistoryRepository.save(transactionFrom);

        TransactionHistory transactionTo = new TransactionHistory(null,
                amount, toWallet.getCurrency(), TransactionType.OWN_TRANSFER, LocalDateTime.now(), toWallet, user,
                "Own transfer from wallet: " + fromWallet.getWalletName() + " (" + fromWallet.getWalletCode() + ")" + " -> "
                        + toWallet.getWalletName() + " (" + toWallet.getWalletCode() + ")" + " | amount: " + amount + toWallet.getCurrency()
        );
        transactionHistoryRepository.save(transactionTo);

        return toWallet;
    }

    @Override
    public Wallet convertWalletCurrency(String walletCode, String targetCurrency) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet wallet = walletRepository.findByWalletCodeAndUser(walletCode, user)
                .orElseThrow(() -> new RuntimeException("Wallet not found or access denied"));

        BigDecimal convertedAmount = currencyConverter.convert(
                wallet.getBalance(),
                wallet.getCurrency().toString(),
                targetCurrency);

        wallet.setBalance(convertedAmount);
        wallet.setCurrency(Currency.valueOf(targetCurrency));

        walletRepository.save(wallet);

        TransactionHistory conversionTransaction = new TransactionHistory(null,
                wallet.getBalance(), wallet.getCurrency(), TransactionType.CURRENCY_CONVERSION, LocalDateTime.now(), wallet, user,
                "Converted wallet balance from " + wallet.getCurrency() + " to " + targetCurrency);
        transactionHistoryRepository.save(conversionTransaction);

        return wallet;
    }

    @Override
    public void deleteWallet(String walletCode) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet wallet = walletRepository.findByWalletCodeAndUser(walletCode, user)
                .orElseThrow(() -> new RuntimeException("Wallet not found or access denied"));

        if (wallet.isPrimary()) {
            throw new RuntimeException("Cannot delete primary wallet");
        }

        if (wallet.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            Wallet primaryWallet = getUserPrimaryWallet(user);
            BigDecimal convertedBalance = currencyConverter.convert(
                    wallet.getBalance(),
                    wallet.getCurrency().toString(),
                    primaryWallet.getCurrency().toString());

            primaryWallet.setBalance(primaryWallet.getBalance().add(convertedBalance));
            wallet.setBalance(BigDecimal.ZERO);
            walletRepository.save(primaryWallet);
        }
        walletRepository.delete(wallet);

        TransactionHistory deleteTransaction = new TransactionHistory(null,
                BigDecimal.ZERO, wallet.getCurrency(), TransactionType.DELETED, LocalDateTime.now(), wallet, user,
                "Deleted wallet: " + wallet.getWalletName() + " (" + wallet.getWalletCode() + ")");
        transactionHistoryRepository.save(deleteTransaction);
    }

    public Wallet getUserPrimaryWallet(User user) {
        if (user == null || user.getWallets() == null || user.getWallets().isEmpty()) {
            return null;
        }
        for (Wallet wallet : user.getWallets()) {
            if (wallet.isPrimary()) {
                return wallet;
            }
        }
        return null;
    }
}
package com.wallet.SimpleWalletAPI.services.impl;

import com.wallet.SimpleWalletAPI.dtos.WalletDTO;
import com.wallet.SimpleWalletAPI.exceptions.BaseServiceException;
import com.wallet.SimpleWalletAPI.exceptions.InsufficientFundsException;
import com.wallet.SimpleWalletAPI.exceptions.PrimaryWalletDeleteException;
import com.wallet.SimpleWalletAPI.exceptions.WalletNotFoundException;
import com.wallet.SimpleWalletAPI.mappers.WalletMapper;
import com.wallet.SimpleWalletAPI.models.*;
import com.wallet.SimpleWalletAPI.repositories.TransactionHistoryRepository;
import com.wallet.SimpleWalletAPI.repositories.WalletRepository;
import com.wallet.SimpleWalletAPI.services.CurrencyConverter;
import com.wallet.SimpleWalletAPI.services.UserService;
import com.wallet.SimpleWalletAPI.services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;
import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;
import java.util.stream.Collectors;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserService userService;

    @Autowired
    private WalletMapper walletMapper;

    @Autowired
    private TransactionHistoryRepository transactionHistoryRepository;

    @Autowired
    private CurrencyConverter currencyConverter;

    @Override
    public WalletDTO createWallet(String walletName) {
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
                "Created wallet: " + wallet.getWalletName() + " (Wallet code: " + wallet.getWalletCode() + ")"
        );
        transactionHistoryRepository.save(transaction);

        return walletMapper.entityToDto(wallet);
    }

    @Override
    public WalletDTO getWalletDetails(String walletCode) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet wallet = walletRepository.findByWalletCodeAndUser(walletCode, user).filter(w -> !w.isDeleted())
                .orElseThrow(() -> new WalletNotFoundException(walletCode));
        return walletMapper.entityToDto(wallet);
    }

    @Override
    public List<WalletDTO> getAllWallets() {
        User user = userService.getCurrentAuthenticatedUser();
        List<Wallet> wallets = user.getWallets().stream().filter(wallet -> !wallet.isDeleted()).toList();
        return wallets.stream().map(walletMapper::entityToDto).collect(Collectors.toList());
    }

    @Override
    public WalletDTO depositToPrimaryWallet(BigDecimal amount, String currency) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet primaryWallet = getUserPrimaryWallet(user);
        if (!primaryWallet.isPrimary()) {
            throw new BaseServiceException("Operation allowed only on primary wallet", HttpStatus.BAD_REQUEST.value());
        }

        String normalizedCurrency = getNormalizedCurrency(currency);

        if (!isValidCurrency(normalizedCurrency)) {
            throw new BaseServiceException("Invalid target currency: " + currency, HttpStatus.BAD_REQUEST.value());
        }

        BigDecimal convertedAmount = currencyConverter.convert(amount, normalizedCurrency, primaryWallet.getCurrency().toString()).setScale(2, RoundingMode.HALF_DOWN);
        primaryWallet.setBalance(primaryWallet.getBalance().add(convertedAmount));
        walletRepository.save(primaryWallet);

        StringBuilder transactionMessage = new StringBuilder("Deposit to: " + primaryWallet.getWalletName() +
                " | amount: " + amount + Currency.valueOf(normalizedCurrency));

        if (!convertedAmount.equals(amount)) {
            transactionMessage.append(" (converted to ").append(convertedAmount).append(primaryWallet.getCurrency()).append(")");
        }

        TransactionHistory transaction = new TransactionHistory(null,
                amount, Currency.valueOf(normalizedCurrency), TransactionType.DEPOSIT, LocalDateTime.now(), primaryWallet, user,
                transactionMessage.toString());
        transactionHistoryRepository.save(transaction);

        return walletMapper.entityToDto(primaryWallet);
    }

    @Override
    public WalletDTO withdrawFromPrimaryWallet(BigDecimal amount, String currency) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet primaryWallet = getUserPrimaryWallet(user);
        if (!primaryWallet.isPrimary()) {
            throw new BaseServiceException("Operation allowed only on primary wallet", HttpStatus.BAD_REQUEST.value());
        }

        String normalizedCurrency = getNormalizedCurrency(currency);
        if (!isValidCurrency(normalizedCurrency)) {
            throw new BaseServiceException("Invalid target currency: " + currency, HttpStatus.BAD_REQUEST.value());
        }

        BigDecimal convertedAmount = currencyConverter.convert(amount, normalizedCurrency, primaryWallet.getCurrency().toString()).setScale(2, RoundingMode.HALF_DOWN);

        if (primaryWallet.getBalance().compareTo(convertedAmount) < 0) {
            throw new InsufficientFundsException(primaryWallet.getWalletCode());
        }

        primaryWallet.setBalance(primaryWallet.getBalance().subtract(convertedAmount));
        walletRepository.save(primaryWallet);

        StringBuilder transactionMessage = new StringBuilder("Withdrawal from: " + primaryWallet.getWalletName() +
                " | amount: " + amount + Currency.valueOf(normalizedCurrency));

        if (!convertedAmount.equals(amount)) {
            transactionMessage.append(" (converted to ").append(convertedAmount).append(primaryWallet.getCurrency()).append(")");
        }

        TransactionHistory transaction = new TransactionHistory(null,
                amount, Currency.valueOf(normalizedCurrency), TransactionType.WITHDRAW, LocalDateTime.now(), primaryWallet, user,
                transactionMessage.toString());
        transactionHistoryRepository.save(transaction);

        return walletMapper.entityToDto(primaryWallet);
    }

    @Override
    public WalletDTO transferMoneyToAnotherUser(String toWalletCode, BigDecimal amount, String currency) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet senderPrimaryWallet = getUserPrimaryWallet(user);

        Wallet targetWallet = walletRepository.findByWalletCode(toWalletCode)
                .orElseThrow(() -> new WalletNotFoundException(toWalletCode));

        if (targetWallet.isDeleted()) {
            throw new WalletNotFoundException(toWalletCode);
        }
        User recipientUser = targetWallet.getUser();
        Wallet recipientPrimaryWallet = getUserPrimaryWallet(recipientUser);

        String normalizedCurrency = getNormalizedCurrency(currency);

        if (!isValidCurrency(normalizedCurrency)) {
            throw new BaseServiceException("Invalid target currency: " + currency, HttpStatus.BAD_REQUEST.value());
        }

        BigDecimal convertedAmount = currencyConverter.convert(amount, normalizedCurrency, senderPrimaryWallet.getCurrency().toString()).setScale(2, RoundingMode.HALF_DOWN);

        if (senderPrimaryWallet.getBalance().compareTo(convertedAmount) < 0) {
            throw new InsufficientFundsException(senderPrimaryWallet.getWalletCode());
        }

        senderPrimaryWallet.setBalance(senderPrimaryWallet.getBalance().subtract(convertedAmount));
        BigDecimal receivedAmount = currencyConverter.convert(amount, normalizedCurrency, recipientPrimaryWallet.getCurrency().toString()).setScale(2, RoundingMode.HALF_DOWN);
        recipientPrimaryWallet.setBalance(recipientPrimaryWallet.getBalance().add(receivedAmount));

        walletRepository.save(senderPrimaryWallet);
        walletRepository.save(recipientPrimaryWallet);

        StringBuilder senderTransactionMessage = new StringBuilder("Transferred to user: " + recipientPrimaryWallet.getUser().getName() +
                " (Wallet code: " + recipientPrimaryWallet.getWalletCode() + " )"
                + " | amount: " + convertedAmount + senderPrimaryWallet.getCurrency());

        if (!convertedAmount.equals(amount)) {
            senderTransactionMessage.append(" (converted to ").append(amount).append(Currency.valueOf(normalizedCurrency)).append(")");
        }

        TransactionHistory transactionFrom = new TransactionHistory(null,
                amount, Currency.valueOf(normalizedCurrency), TransactionType.TRANSFER_TO_USER, LocalDateTime.now(), senderPrimaryWallet, user,
                senderTransactionMessage.toString());
        transactionHistoryRepository.save(transactionFrom);

        StringBuilder recipientTransactionMessage = new StringBuilder("Received from user: " + senderPrimaryWallet.getUser().getName() +
                " (Wallet code: " + senderPrimaryWallet.getWalletCode() + " )"
                + " | amount: " + amount + Currency.valueOf(normalizedCurrency));

        if (!receivedAmount.equals(amount)) {
            recipientTransactionMessage.append(" (converted to ").append(receivedAmount).append(recipientPrimaryWallet.getCurrency()).append(")");
        }

        TransactionHistory transactionTo = new TransactionHistory(null,
                amount, Currency.valueOf(normalizedCurrency), TransactionType.TRANSFER_FROM_USER, LocalDateTime.now(), recipientPrimaryWallet, recipientPrimaryWallet.getUser(),
                recipientTransactionMessage.toString());
        transactionHistoryRepository.save(transactionTo);

        return walletMapper.entityToDto(senderPrimaryWallet);
    }

    @Override
    public WalletDTO transferBetweenOwnWallets(String fromWalletCode, String toWalletCode, BigDecimal amount, String currency) {
        User user = userService.getCurrentAuthenticatedUser();

        Wallet fromWallet = walletRepository.findByWalletCodeAndUser(fromWalletCode, user)
                .orElseThrow(() -> new WalletNotFoundException(fromWalletCode));
        Wallet toWallet = walletRepository.findByWalletCodeAndUser(toWalletCode, user)
                .orElseThrow(() -> new WalletNotFoundException(toWalletCode));

        if (fromWallet.isDeleted()) {
            throw new WalletNotFoundException(fromWalletCode);
        }
        if (toWallet.isDeleted()) {
            throw new WalletNotFoundException(toWalletCode);
        }

        String normalizedCurrency = getNormalizedCurrency(currency);

        if (!isValidCurrency(normalizedCurrency)) {
            throw new BaseServiceException("Invalid target currency: " + currency, HttpStatus.BAD_REQUEST.value());
        }

        BigDecimal convertedAmount = currencyConverter.convert(amount, normalizedCurrency, fromWallet.getCurrency().toString()).setScale(2, RoundingMode.HALF_DOWN);

        if (fromWallet.getBalance().compareTo(convertedAmount) < 0) {
            throw new InsufficientFundsException(fromWallet.getWalletCode());
        }

        fromWallet.setBalance(fromWallet.getBalance().subtract(convertedAmount));

        BigDecimal receivedAmount = currencyConverter.convert(amount, normalizedCurrency, toWallet.getCurrency().toString()).setScale(2, RoundingMode.HALF_DOWN);
        toWallet.setBalance(toWallet.getBalance().add(receivedAmount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        StringBuilder senderTransactionMessage = new StringBuilder("Own transfer to wallet: " + toWallet.getWalletName() +
                " (code: " + toWallet.getWalletCode() + " )" + " | amount: " + convertedAmount + fromWallet.getCurrency());

        if (!convertedAmount.equals(amount)) {
            senderTransactionMessage.append(" (converted to ").append(amount).append(Currency.valueOf(normalizedCurrency)).append(")");
        }

        TransactionHistory transactionFrom = new TransactionHistory(null,
                amount, Currency.valueOf(normalizedCurrency), TransactionType.OWN_TRANSFER, LocalDateTime.now(), fromWallet, user, senderTransactionMessage.toString());
        transactionHistoryRepository.save(transactionFrom);

        StringBuilder recipientTransactionMessage = new StringBuilder("Own transfer from wallet: " + fromWallet.getWalletName() +
                " (code: " + fromWallet.getWalletCode() + " )" + " | amount: " + amount + Currency.valueOf(normalizedCurrency));

        if (!receivedAmount.equals(amount)) {
            recipientTransactionMessage.append(" (converted to ").append(receivedAmount).append(toWallet.getCurrency()).append(")");
        }

        TransactionHistory transactionTo = new TransactionHistory(null,
                amount, Currency.valueOf(normalizedCurrency), TransactionType.OWN_TRANSFER, LocalDateTime.now(), toWallet, user, recipientTransactionMessage.toString());
        transactionHistoryRepository.save(transactionTo);

        return walletMapper.entityToDto(toWallet);
    }

    @Override
    public WalletDTO convertWalletCurrency(String walletCode, String targetCurrency) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet wallet = walletRepository.findByWalletCodeAndUser(walletCode, user)
                .orElseThrow(() -> new WalletNotFoundException(walletCode));

        if (wallet.isDeleted()) {
            throw new WalletNotFoundException(walletCode);
        }

        String oldWalletCurrency = wallet.getCurrency().toString();
        String normalizedTargetCurrency = getNormalizedCurrency(targetCurrency);

        if (!isValidCurrency(normalizedTargetCurrency)) {
            throw new BaseServiceException("Invalid target currency: " + targetCurrency, HttpStatus.BAD_REQUEST.value());
        }

        if (oldWalletCurrency.equalsIgnoreCase(normalizedTargetCurrency)) {
            throw new BaseServiceException("Conversion not needed. The wallet is already in " + normalizedTargetCurrency, HttpStatus.BAD_REQUEST.value());
        }

        BigDecimal convertedAmount = currencyConverter.convert(
                wallet.getBalance(),
                wallet.getCurrency().toString(),
                normalizedTargetCurrency).setScale(2, RoundingMode.HALF_DOWN);

        wallet.setBalance(convertedAmount);
        wallet.setCurrency(Currency.valueOf(normalizedTargetCurrency));
        walletRepository.save(wallet);

        TransactionHistory conversionTransaction = new TransactionHistory(null,
                convertedAmount, Currency.valueOf(normalizedTargetCurrency), TransactionType.CURRENCY_CONVERSION, LocalDateTime.now(), wallet, user,
                "Converted wallet balance from " + oldWalletCurrency + " to " + normalizedTargetCurrency + " | Balance: " + convertedAmount + normalizedTargetCurrency);
        transactionHistoryRepository.save(conversionTransaction);

        return walletMapper.entityToDto(wallet);
    }

    @Override
    public void deleteWallet(String walletCode) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet wallet = walletRepository.findByWalletCodeAndUser(walletCode, user)
                .orElseThrow(() -> new WalletNotFoundException(walletCode));

        if (wallet.isDeleted()) {
            throw new WalletNotFoundException(walletCode);
        }

        if (wallet.isPrimary()) {
            throw new PrimaryWalletDeleteException();
        }

        if (wallet.getBalance().compareTo(BigDecimal.ZERO) > 0) {
            Wallet primaryWallet = getUserPrimaryWallet(user);
            BigDecimal convertedBalance = currencyConverter.convert(wallet.getBalance(), wallet.getCurrency().toString(),
                    primaryWallet.getCurrency().toString()).setScale(2, RoundingMode.HALF_DOWN);

            primaryWallet.setBalance(primaryWallet.getBalance().add(convertedBalance));
            wallet.setBalance(BigDecimal.ZERO);
            walletRepository.save(primaryWallet);
        }

        TransactionHistory deleteTransaction = new TransactionHistory(null,
                BigDecimal.ZERO, wallet.getCurrency(), TransactionType.DELETED, LocalDateTime.now(), wallet, user,
                "Deleted wallet: " + wallet.getWalletName() + " (" + wallet.getWalletCode() + ")");

        transactionHistoryRepository.save(deleteTransaction);
        wallet.setDeleted(true);
        walletRepository.save(wallet);
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

    private boolean isValidCurrency(String currency) {
        try {
            Currency.valueOf(currency);
            return true;
        } catch (IllegalArgumentException e) {
            return false;
        }
    }

    private static String getNormalizedCurrency(String currency) {
        return currency.replaceAll("\\s+", "").toUpperCase();
    }
}
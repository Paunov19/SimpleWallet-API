package com.wallet.SimpleWalletAPI.services;

import com.wallet.SimpleWalletAPI.models.TransactionHistory;
import com.wallet.SimpleWalletAPI.models.TransactionType;
import com.wallet.SimpleWalletAPI.models.User;
import com.wallet.SimpleWalletAPI.models.Wallet;
import com.wallet.SimpleWalletAPI.repositories.TransactionHistoryRepository;
import com.wallet.SimpleWalletAPI.repositories.WalletRepository;
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

    @Override
    public Wallet createWallet(String walletName) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet wallet = new Wallet();
        wallet.setUser(user);
        wallet.setWalletName(walletName);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setPrimary(false);
        String walletCode = UUID.randomUUID().toString().substring(0, 10);
        wallet.setWalletCode(walletCode);

        TransactionHistory transaction = new TransactionHistory(null,
                BigDecimal.ZERO, TransactionType.CREATED, LocalDateTime.now(), wallet, user, "Created wallet: " + wallet.getWalletName()
        );
        transactionHistoryRepository.save(transaction);

        return walletRepository.save(wallet);
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
    public Wallet transferMoneyToAnotherUser(String toWalletCode, BigDecimal amount) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet senderPrimaryWallet = user.getPrimaryWallet();

        Wallet targetWallet = walletRepository.findByWalletCode(toWalletCode)
                .orElseThrow(() -> new RuntimeException("Recipient wallet not found"));
        User recipientUser = targetWallet.getUser();
        Wallet recipientPrimaryWallet = recipientUser.getPrimaryWallet();

        if (senderPrimaryWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        senderPrimaryWallet.setBalance(senderPrimaryWallet.getBalance().subtract(amount));
        recipientPrimaryWallet.setBalance(recipientPrimaryWallet.getBalance().add(amount));

        walletRepository.save(senderPrimaryWallet);
        walletRepository.save(recipientPrimaryWallet);

        TransactionHistory transactionFrom = new TransactionHistory(null,
                amount, TransactionType.TRANSFER_TO_USER, LocalDateTime.now(), senderPrimaryWallet, user,
                "Transferred to user: " + recipientPrimaryWallet.getUser().getName() + " (Wallet: " + recipientPrimaryWallet.getWalletName() + ")"
        );
        transactionHistoryRepository.save(transactionFrom);

        // Record transaction for the receiver
        TransactionHistory transactionTo = new TransactionHistory(null,
                amount, TransactionType.TRANSFER_FROM_USER, LocalDateTime.now(), recipientPrimaryWallet, recipientPrimaryWallet.getUser(),
                "Received from user: " + user.getName() + " (Wallet: " + senderPrimaryWallet.getWalletName() + ")"
        );
        transactionHistoryRepository.save(transactionTo);

        return recipientPrimaryWallet;
    }

    @Override
    public Wallet transferBetweenOwnWallets(String fromWalletCode, String toWalletCode, BigDecimal amount) {
        User user = userService.getCurrentAuthenticatedUser();

        Wallet fromWallet = walletRepository.findByWalletCodeAndUser(fromWalletCode, user)
                .orElseThrow(() -> new RuntimeException("Source wallet not found or access denied"));
        Wallet toWallet = walletRepository.findByWalletCodeAndUser(toWalletCode, user)
                .orElseThrow(() -> new RuntimeException("Target wallet not found or access denied"));

        if (fromWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds in source wallet");
        }

        fromWallet.setBalance(fromWallet.getBalance().subtract(amount));
        toWallet.setBalance(toWallet.getBalance().add(amount));

        walletRepository.save(fromWallet);
        walletRepository.save(toWallet);

        TransactionHistory transactionFrom = new TransactionHistory(null,
                amount, TransactionType.OWN_TRANSFER, LocalDateTime.now(), fromWallet, user,
                "Transferred from wallet: " + fromWallet.getWalletName() + " (" + fromWallet.getWalletCode() + ")"
        );
        transactionHistoryRepository.save(transactionFrom);

        TransactionHistory transactionTo = new TransactionHistory(null,
                amount, TransactionType.OWN_TRANSFER, LocalDateTime.now(), toWallet, user,
                "Transferred to wallet: " + toWallet.getWalletName() + " (" + toWallet.getWalletCode() + ")"
        );
        transactionHistoryRepository.save(transactionTo);

        return toWallet;
    }

    @Override
    public Wallet depositToPrimaryWallet(BigDecimal amount) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet primaryWallet = user.getPrimaryWallet();
        if (!primaryWallet.isPrimary()) {
            throw new RuntimeException("Operation allowed only on primary wallet");
        }

        TransactionHistory transaction = new TransactionHistory(null,
                amount, TransactionType.DEPOSIT, LocalDateTime.now(), primaryWallet, user, "Deposit to primary wallet: " + primaryWallet.getWalletName());
        transactionHistoryRepository.save(transaction);
        primaryWallet.setBalance(primaryWallet.getBalance().add(amount));
        return walletRepository.save(primaryWallet);
    }

    @Override
    public Wallet withdrawFromPrimaryWallet(BigDecimal amount) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet primaryWallet = user.getPrimaryWallet();
        if (!primaryWallet.isPrimary()) {
            throw new RuntimeException("Operation allowed only on primary wallet");
        }

        if (primaryWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

        TransactionHistory transaction = new TransactionHistory(null,
                amount, TransactionType.WITHDRAW, LocalDateTime.now(), primaryWallet, user, "Withdrawal from primary wallet: " + primaryWallet.getWalletName());
        transactionHistoryRepository.save(transaction);
        primaryWallet.setBalance(primaryWallet.getBalance().subtract(amount));
        return walletRepository.save(primaryWallet);
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
            Wallet primaryWallet = user.getPrimaryWallet();
            primaryWallet.setBalance(primaryWallet.getBalance().add(wallet.getBalance()));
            wallet.setBalance(BigDecimal.ZERO);
            walletRepository.save(primaryWallet);
        }

        TransactionHistory deleteTransaction = new TransactionHistory(null,
                BigDecimal.ZERO, TransactionType.DELETED, LocalDateTime.now(), wallet, user,
                "Wallet deleted: " + wallet.getWalletName() + " (" + wallet.getWalletCode() + ")");
        transactionHistoryRepository.save(deleteTransaction);
        walletRepository.delete(wallet);
    }

    @Override
    public List<TransactionHistory> getAllTransactionsHistoryForCurrentUser() {
        User user = userService.getCurrentAuthenticatedUser();
        return transactionHistoryRepository.findByUser(user);
    }

    @Override
    public List<TransactionHistory> getTransactionsHistoryForWallet(String walletCode) {
        Wallet wallet = walletRepository.findByWalletCode(walletCode)
                .orElseThrow(() -> new RuntimeException("Wallet not found"));
        return transactionHistoryRepository.findByWallet(wallet);
    }
}
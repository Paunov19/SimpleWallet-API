package com.wallet.SimpleWalletAPI.services;

import com.wallet.SimpleWalletAPI.models.User;
import com.wallet.SimpleWalletAPI.models.Wallet;
import com.wallet.SimpleWalletAPI.repositories.UserRepository;
import com.wallet.SimpleWalletAPI.repositories.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.math.BigDecimal;
import java.util.List;
import java.util.UUID;

@Service
public class WalletServiceImpl implements WalletService {

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private UserService userService;

    @Override
    public Wallet createWallet(String walletName) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet wallet = new Wallet();
        wallet.setWalletName(walletName);
        wallet.setBalance(BigDecimal.ZERO);
        wallet.setUser(user);
        wallet.setPrimary(false);
        String walletCode = UUID.randomUUID().toString().substring(0, 10);
        wallet.setWalletCode(walletCode);

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
        Wallet primaryWallet = user.getPrimaryWallet();

        Wallet targetWallet = walletRepository.findByWalletCode(toWalletCode)
                .orElseThrow(() -> new RuntimeException("Recipient wallet not found"));

        if (!(primaryWallet.getBalance().subtract(amount).signum() >= 0)) {
            throw new RuntimeException("Insufficient funds");
        }

        primaryWallet.setBalance(primaryWallet.getBalance().subtract(amount));
        targetWallet.setBalance(targetWallet.getBalance().add(amount));

        walletRepository.save(primaryWallet);
        walletRepository.save(targetWallet);

        return targetWallet;
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

        return toWallet;
    }

    @Override
    public Wallet depositToPrimaryWallet(BigDecimal amount) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet primaryWallet = user.getPrimaryWallet();

        primaryWallet.setBalance(primaryWallet.getBalance().add(amount));
        return walletRepository.save(primaryWallet);
    }

    @Override
    public Wallet withdrawFromPrimaryWallet(BigDecimal amount) {
        User user = userService.getCurrentAuthenticatedUser();
        Wallet primaryWallet = user.getPrimaryWallet();

        if (primaryWallet.getBalance().compareTo(amount) < 0) {
            throw new RuntimeException("Insufficient funds");
        }

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

        walletRepository.delete(wallet);
    }
}
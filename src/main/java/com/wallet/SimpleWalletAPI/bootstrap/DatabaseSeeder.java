package com.wallet.SimpleWalletAPI.bootstrap;

import com.wallet.SimpleWalletAPI.models.Currency;
import com.wallet.SimpleWalletAPI.models.User;
import com.wallet.SimpleWalletAPI.models.Wallet;
import com.wallet.SimpleWalletAPI.repositories.UserRepository;
import com.wallet.SimpleWalletAPI.repositories.WalletRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.CommandLineRunner;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Component;

import java.math.BigDecimal;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.UUID;

@Component
public class DatabaseSeeder implements CommandLineRunner {

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private WalletRepository walletRepository;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    public void run(String... args) {
        if (userRepository.count() == 0) {
            seedUsersAndWallets();
        }
    }

    private void seedUsersAndWallets() {
        List<User> users = new ArrayList<>();
        User user1 = new User();
        user1.setName("Test User1");
        user1.setEmail("user1@test.com");
        user1.setPassword(passwordEncoder.encode("password1"));
        users.add(user1);

        User user2 = new User();
        user2.setName("Test User2");
        user2.setEmail("user2@test.com");
        user2.setPassword(passwordEncoder.encode("password2"));
        users.add(user2);

        User user3 = new User();
        user3.setName("Test User3");
        user3.setEmail("user3@test.com");
        user3.setPassword(passwordEncoder.encode("password3"));
        users.add(user3);

        userRepository.saveAll(users);

        for (User user : users) {
            Wallet primaryWallet = new Wallet();
            primaryWallet.setWalletName("Primary Wallet");
            primaryWallet.setBalance(BigDecimal.valueOf(1000));
            primaryWallet.setWalletCode(UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").substring(0, 10));
            primaryWallet.setPrimary(true);
            primaryWallet.setUser(user);
            primaryWallet.setCurrency(Currency.BGN);

            Wallet secondaryWallet = new Wallet();
            secondaryWallet.setWalletName("Savings Wallet");
            secondaryWallet.setBalance(BigDecimal.valueOf(500));
            secondaryWallet.setWalletCode(UUID.randomUUID().toString().replaceAll("[^a-zA-Z0-9]", "").substring(0, 10));
            secondaryWallet.setPrimary(false);
            secondaryWallet.setUser(user);
            secondaryWallet.setCurrency(Currency.BGN);

            user.setWallets(Arrays.asList(primaryWallet, secondaryWallet));

            walletRepository.save(primaryWallet);
            walletRepository.save(secondaryWallet);
        }

        System.out.println("Database seeded with initial data.");
    }
}
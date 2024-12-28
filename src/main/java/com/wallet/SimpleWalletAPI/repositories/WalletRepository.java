package com.wallet.SimpleWalletAPI.repositories;

import com.wallet.SimpleWalletAPI.models.User;
import com.wallet.SimpleWalletAPI.models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface WalletRepository extends JpaRepository<Wallet, Long> {
    Optional<Wallet> findByWalletCode(String walletCode);
    Optional<Wallet> findByWalletCodeAndUser(String walletCode, User user);
}

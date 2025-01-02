package com.wallet.SimpleWalletAPI.repositories;

import com.wallet.SimpleWalletAPI.models.TransactionHistory;
import com.wallet.SimpleWalletAPI.models.User;
import com.wallet.SimpleWalletAPI.models.Wallet;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface TransactionHistoryRepository extends JpaRepository<TransactionHistory, Long> {
    List<TransactionHistory> findByUser(User user);
    List<TransactionHistory> findByWallet(Wallet wallet);
}
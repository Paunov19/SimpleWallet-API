package com.wallet.SimpleWalletAPI.controllers;

import com.wallet.SimpleWalletAPI.models.TransactionHistory;
import com.wallet.SimpleWalletAPI.models.Wallet;
import com.wallet.SimpleWalletAPI.services.WalletService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.math.BigDecimal;
import java.util.List;

@RestController
@RequestMapping("/api/wallets")
public class WalletController {

    @Autowired
    private WalletService walletService;

    /**
     * Endpoint to create a new wallet.
     *
     * @param walletName The name of the new wallet.
     * @return The created wallet details.
     */
    @PostMapping("/create-wallet")
    public ResponseEntity<Wallet> createWallet(@RequestParam String walletName) {
        Wallet wallet = walletService.createWallet(walletName);
        return new ResponseEntity<>(wallet, HttpStatus.CREATED);
    }

    /**
     * Endpoint to get details of a specific wallet by its code.
     *
     * @param walletCode The wallet code.
     * @return The wallet details.
     */
    @GetMapping("/{walletCode}")
    public ResponseEntity<Wallet> getWalletDetails(@PathVariable String walletCode) {
        Wallet wallet = walletService.getWalletDetails(walletCode);
        return ResponseEntity.ok(wallet);
    }

    /**
     * Endpoint to get a list of all wallets owned by the current user.
     *
     * @return A list of wallets.
     */
    @GetMapping
    public ResponseEntity<List<Wallet>> getAllWallets() {
        List<Wallet> wallets = walletService.getAllWallets();
        return ResponseEntity.ok(wallets);
    }

    /**
     * Endpoint to deposit money into the primary wallet.
     *
     * @param amount The amount to deposit.
     * @return The updated primary wallet details.
     */
    @PostMapping("/deposit")
    public ResponseEntity<Wallet> depositToPrimaryWallet(@RequestParam BigDecimal amount) {
        Wallet wallet = walletService.depositToPrimaryWallet(amount);
        return ResponseEntity.ok(wallet);
    }

    /**
     * Endpoint to withdraw money from the primary wallet.
     *
     * @param amount The amount to withdraw.
     * @return The updated primary wallet details.
     */
    @PostMapping("/withdraw")
    public ResponseEntity<Wallet> withdrawFromPrimaryWallet(@RequestParam BigDecimal amount) {
        Wallet wallet = walletService.withdrawFromPrimaryWallet(amount);
        return ResponseEntity.ok(wallet);
    }

    /**
     * Endpoint to transfer money to another user's primary wallet.
     *
     * @param toWalletCode The recipient's primary wallet code.
     * @param amount       The amount to transfer.
     * @return The recipient's updated primary wallet details.
     */
    @PostMapping("/transfer-to-user")
    public ResponseEntity<Wallet> transferToAnotherUser(@RequestParam String toWalletCode,
                                                        @RequestParam BigDecimal amount) {
        Wallet wallet = walletService.transferMoneyToAnotherUser(toWalletCode, amount);
        return ResponseEntity.ok(wallet);
    }

    /**
     * Endpoint to transfer money between the user's own wallets.
     *
     * @param fromWalletCode The source wallet code.
     * @param toWalletCode   The destination wallet code.
     * @param amount         The amount to transfer.
     * @return The updated destination wallet details.
     */
    @PostMapping("/transfer-between-own")
    public ResponseEntity<Wallet> transferBetweenOwnWallets(@RequestParam String fromWalletCode,
                                                            @RequestParam String toWalletCode,
                                                            @RequestParam BigDecimal amount) {
        Wallet wallet = walletService.transferBetweenOwnWallets(fromWalletCode, toWalletCode, amount);
        return ResponseEntity.ok(wallet);
    }

    /**
     * Endpoint to delete a wallet (except the primary wallet).
     *
     * @param walletCode The wallet code of the wallet to delete.
     * @return A response indicating the deletion status.
     */
    @DeleteMapping("/{walletCode}")
    public ResponseEntity<String> deleteWallet(@PathVariable String walletCode) {
        walletService.deleteWallet(walletCode);
        return ResponseEntity.ok("Wallet deleted successfully");
    }

    /**
     * Endpoint to show all transactions by user.
     *
     * @return A response indicating the deletion status.
     */
    @GetMapping("/transaction-history")
    public ResponseEntity<List<TransactionHistory>> getAllTransactionsHistoryForUser() {
        List<TransactionHistory> transactionHistory = walletService.getAllTransactionsHistoryForCurrentUser();
        return ResponseEntity.ok(transactionHistory);
    }

    /**
     * Endpoint to show all transactions by user.
     *
     * @param walletCode The wallet code.
     * @return A response indicating the deletion status.
     */
    @GetMapping("/wallet-transaction-history/{walletCode}")
    public ResponseEntity<List<TransactionHistory>> getTransactionsHistoryForWallet(@PathVariable String walletCode) {
        List<TransactionHistory> transactionHistory = walletService.getTransactionsHistoryForWallet(walletCode);
        return ResponseEntity.ok(transactionHistory);
    }
}
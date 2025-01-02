package com.wallet.SimpleWalletAPI.controllers;

import com.wallet.SimpleWalletAPI.dtos.TransactionHistoryDTO;
import com.wallet.SimpleWalletAPI.services.TransactionHistoryService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;

@RestController
@RequestMapping("/api/transaction-history")
public class TransactionHistoryController {

    @Autowired
    private TransactionHistoryService transactionHistoryService;

    @Operation(
            summary = "Get all transaction history for the current user",
            description = "This endpoint retrieves the full transaction history for the authenticated user. " +
                    "It includes all transactions related to the user's wallets."
    )
    @GetMapping("/")
    public ResponseEntity<List<TransactionHistoryDTO>> getAllTransactionsHistoryForUser() {
        List<TransactionHistoryDTO> transactionHistory = transactionHistoryService.getAllTransactionsHistoryForCurrentUser();
        return ResponseEntity.ok(transactionHistory);
    }

    @Operation(
            summary = "Get transaction history for a specific wallet",
            description = "This endpoint retrieves the transaction history for a specific wallet identified by its wallet code. " +
                    "User can use the wallet code to track the transactions for that particular wallet."
    )
    @GetMapping("/{walletCode}")
    public ResponseEntity<List<TransactionHistoryDTO>> getTransactionsHistoryForWallet(@PathVariable @Parameter(description = "The wallet code to retrieve wallet transactions") String walletCode) {
        List<TransactionHistoryDTO> transactionHistory = transactionHistoryService.getAllTransactionsHistoryForWallet(walletCode);
        return ResponseEntity.ok(transactionHistory);
    }
}
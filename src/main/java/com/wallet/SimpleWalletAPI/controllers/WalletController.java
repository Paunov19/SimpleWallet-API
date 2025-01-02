package com.wallet.SimpleWalletAPI.controllers;

import com.wallet.SimpleWalletAPI.dtos.WalletDTO;
import com.wallet.SimpleWalletAPI.services.WalletService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.Parameter;
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

    @Operation(summary = "Create a new wallet",
            description = "This endpoint allows user to create a new wallet with a given name.")
    @PostMapping("/create-wallet")
    public ResponseEntity<WalletDTO> createWallet(@RequestParam @Parameter(description = "Name of the wallet to be created") String walletName) {
        WalletDTO wallet = walletService.createWallet(walletName);
        return new ResponseEntity<>(wallet, HttpStatus.CREATED);
    }

    @Operation(summary = "Get a list of all user's wallets",
            description = "This endpoint retrieves all wallets for the authenticated user.")
    @GetMapping
    public ResponseEntity<List<WalletDTO>> getAllWallets() {
        List<WalletDTO> wallets = walletService.getAllWallets();
        return ResponseEntity.ok(wallets);
    }

    @Operation(summary = "Get wallet details by wallet code",
            description = "This endpoint retrieves details of a wallet by its unique wallet code.")
    @GetMapping("/{walletCode}")
    public ResponseEntity<WalletDTO> getWalletDetails(@PathVariable @Parameter(description = "The wallet code to retrieve wallet details") String walletCode) {
        WalletDTO wallet = walletService.getWalletDetails(walletCode);
        return ResponseEntity.ok(wallet);
    }

    @Operation(summary = "Deposit to the primary wallet",
            description = "This endpoint allows user to deposit a specified amount only to the primary wallet.")
    @PostMapping("/deposit")
    public ResponseEntity<WalletDTO> depositToPrimaryWallet(@RequestParam @Parameter(description = "Amount to deposit into the primary wallet") BigDecimal amount,
                                                         @RequestParam(required = false, defaultValue = "BGN") @Parameter(description = "Currency of the deposit. Supported currencies: BGN, EURO, USD. Currency is optional")
                                                         String currency) {
        WalletDTO wallet = walletService.depositToPrimaryWallet(amount, currency);
        return ResponseEntity.ok(wallet);
    }

    @Operation(summary = "Withdraw from the primary wallet",
            description = "This endpoint allows user to withdraw a specified amount only from the primary wallet.")
    @PostMapping("/withdraw")
    public ResponseEntity<WalletDTO> withdrawFromPrimaryWallet(@RequestParam @Parameter(description = "Amount to withdraw from the primary wallet") BigDecimal amount,
                                                            @RequestParam(required = false, defaultValue = "BGN") @Parameter(description = "Currency of the withdrawal. Supported currencies: BGN, EURO, USD. Currency is optional")
                                                            String currency) {
        WalletDTO wallet = walletService.withdrawFromPrimaryWallet(amount, currency);
        return ResponseEntity.ok(wallet);
    }

    @Operation(summary = "Transfer funds to another user wallet",
            description = "This endpoint allows user to transfer funds from it's own primary wallet to another user's primary wallet using their wallet code.")
    @PostMapping("/transfer-to-user")
    public ResponseEntity<WalletDTO> transferToAnotherUser(@RequestParam @Parameter(description = "The wallet code of the recipient user") String toWalletCode,
                                                        @RequestParam @Parameter(description = "Amount to transfer") BigDecimal amount,
                                                        @RequestParam(required = false, defaultValue = "BGN") @Parameter(description = "Currency of the transfer. Supported currencies: BGN, EURO, USD. Currency is optional")
                                                        String currency) {
        WalletDTO wallet = walletService.transferMoneyToAnotherUser(toWalletCode, amount, currency);
        return ResponseEntity.ok(wallet);
    }

    @Operation(summary = "Transfer funds between own wallets",
            description = "This endpoint allows user to transfer funds between user's own wallets using their respective wallet codes.")
    @PostMapping("/transfer-between-own")
    public ResponseEntity<WalletDTO> transferBetweenOwnWallets(@RequestParam @Parameter(description = "The wallet code from which the funds are transferred") String fromWalletCode,
                                                            @RequestParam @Parameter(description = "The wallet code to which the funds are transferred") String toWalletCode,
                                                            @RequestParam @Parameter(description = "Amount to transfer between wallets") BigDecimal amount,
                                                            @RequestParam(required = false, defaultValue = "BGN") @Parameter(description = "Currency of the transfer. Supported currencies: BGN, EURO, USD. Currency is optional") String currency) {
        WalletDTO wallet = walletService.transferBetweenOwnWallets(fromWalletCode, toWalletCode, amount, currency);
        return ResponseEntity.ok(wallet);
    }

    @Operation(summary = "Convert the currency of a wallet",
            description = "This endpoint allows user to convert the currency of a specific wallet to the target currency.")
    @PostMapping("/convert-currency")
    public ResponseEntity<WalletDTO> convertWalletCurrency(@RequestParam @Parameter(description = "The wallet code of the wallet to convert") String walletCode,
                                                        @RequestParam @Parameter(description = "The target currency to convert the wallet's balance to. Supported currencies: BGN, EURO, USD.") String targetCurrency) {
        WalletDTO updatedWallet = walletService.convertWalletCurrency(walletCode, targetCurrency);
        return ResponseEntity.ok(updatedWallet);
    }

    @Operation(summary = "Delete a wallet by wallet code",
            description = "This endpoint allows user to delete a wallet using its wallet code. Primary wallet cannot be deleted.")
    @DeleteMapping("/{walletCode}")
    public ResponseEntity<String> deleteWallet(@PathVariable @Parameter(description = "The wallet code of the wallet to be deleted") String walletCode) {
        walletService.deleteWallet(walletCode);
        return ResponseEntity.ok("Wallet deleted successfully");
    }
}
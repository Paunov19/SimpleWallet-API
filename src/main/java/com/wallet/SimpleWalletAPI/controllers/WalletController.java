package com.wallet.SimpleWalletAPI.controllers;

import com.wallet.SimpleWalletAPI.models.Wallet;
import com.wallet.SimpleWalletAPI.services.UserService;
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

    @Autowired
    private UserService userService;

    @PostMapping("/create")
    public ResponseEntity<Wallet> createWallet(@RequestParam String walletName) {
        Wallet wallet = walletService.createWallet(walletName);
        return new ResponseEntity<>(wallet, HttpStatus.CREATED);
    }

}


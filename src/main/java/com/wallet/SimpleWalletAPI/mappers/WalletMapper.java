package com.wallet.SimpleWalletAPI.mappers;

import com.wallet.SimpleWalletAPI.dtos.WalletDTO;
import com.wallet.SimpleWalletAPI.models.Wallet;
import org.springframework.stereotype.Component;

@Component
public class WalletMapper {

    public WalletDTO entityToDto(Wallet wallet) {
        WalletDTO walletDTO = new WalletDTO();
        walletDTO.setWalletName(wallet.getWalletName());
        walletDTO.setWalletCode(wallet.getWalletCode());
        walletDTO.setBalance(wallet.getBalance());
        walletDTO.setCurrency(String.valueOf(wallet.getCurrency()));
        walletDTO.setPrimary(wallet.isPrimary());
        return walletDTO;
    }
}
package com.wallet.SimpleWalletAPI.services;

import com.wallet.SimpleWalletAPI.models.User;
import com.wallet.SimpleWalletAPI.models.Wallet;
import com.wallet.SimpleWalletAPI.templates.UserRequest;

import java.util.List;

public interface UserService {
    User register(UserRequest userRequest);
    User getUserById(Long userId);
    List<Wallet> getUserWallets(Long userId);
    Wallet getPrimaryWallet(Long userId);
    User getCurrentAuthenticatedUser();
}
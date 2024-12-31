package com.wallet.SimpleWalletAPI.services;

import com.wallet.SimpleWalletAPI.models.User;
import com.wallet.SimpleWalletAPI.payload.UserRequest;

public interface UserService {
    User register(UserRequest userRequest);
    User getCurrentAuthenticatedUser();
}
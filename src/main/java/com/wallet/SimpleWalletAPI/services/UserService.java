package com.wallet.SimpleWalletAPI.services;

import com.wallet.SimpleWalletAPI.dtos.UserDTO;
import com.wallet.SimpleWalletAPI.models.User;
import com.wallet.SimpleWalletAPI.payload.UserRequest;

public interface UserService {
    UserDTO register(UserRequest userRequest);
    User getCurrentAuthenticatedUser();
}
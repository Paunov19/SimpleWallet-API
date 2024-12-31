package com.wallet.SimpleWalletAPI.payload;

public record UserRequest(String email, String name, String password) {
}

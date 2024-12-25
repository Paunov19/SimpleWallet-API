package com.wallet.SimpleWalletAPI;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
public class SimpleWalletApiApplication {

	public static void main(String[] args) {
		SpringApplication.run(SimpleWalletApiApplication.class, args);
	}

}
//TODO:
//1. database management
//2. authentication of user
//3. user has one general wallet, deposit only to general then distribute into other wallets,
// can withdraw only from general, add money in other wallet only from general,
// get money from other user only in general wallet
//4. wallets have iban code to get money and code to send money
//5. history of every wallet one by one, general have: deposit, withdraw, send to user, get from user, send to wallet, get from wallet;
//wallets have: get from general/wallet, send to general/wallet; history by date or by service;
//6. send money to other user by iban code or phone number of the user
//7.support BGN, EUR, USD
//8.caching and better code refactoring
//9.
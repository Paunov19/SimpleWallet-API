package com.wallet.SimpleWalletAPI.services;

import com.wallet.SimpleWalletAPI.models.User;
import com.wallet.SimpleWalletAPI.models.Wallet;
import com.wallet.SimpleWalletAPI.repositories.UserRepository;
import com.wallet.SimpleWalletAPI.security.services.UserDetailsImpl;
import com.wallet.SimpleWalletAPI.templates.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.ArrayList;
import java.util.UUID;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;

    @Override
    @Transactional
    public User register(UserRequest userRequest) {
        if (userRepository.existsByEmail(userRequest.email())) {
            throw new RuntimeException("Email already exists");
        }

        User user = new User();
        user.setName(userRequest.name());
        user.setEmail(userRequest.email());
        user.setPassword(passwordEncoder.encode(userRequest.password()));

        if (user.getWallets() == null) {
            user.setWallets(new ArrayList<>());
        }
        
        Wallet primaryWallet = new Wallet();
        primaryWallet.setWalletName("Primary Wallet");
        primaryWallet.setPrimary(true);
        primaryWallet.setUser(user);
        String walletCode = UUID.randomUUID().toString().substring(0, 10);
        primaryWallet.setWalletCode(walletCode);

        user.getWallets().add(primaryWallet);
        userRepository.save(user);
        return user;
    }

    @Override
    public User getCurrentAuthenticatedUser() {
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        if (authentication == null || !authentication.isAuthenticated()) {
            throw new SecurityException("No user currently authenticated");
        }
        Object principal = authentication.getPrincipal();
        if (!(principal instanceof UserDetailsImpl)) {
            throw new SecurityException("Current principal is not a UserDetailsImpl");
        }
        UserDetailsImpl userDetails = (UserDetailsImpl) principal;
        String email = userDetails.getUsername();
        return userRepository.findByEmail(email).orElseThrow(() -> new SecurityException("Authenticated user not found in database"));
    }
}

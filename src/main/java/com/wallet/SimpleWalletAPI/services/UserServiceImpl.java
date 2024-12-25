package com.wallet.SimpleWalletAPI.services;

import com.wallet.SimpleWalletAPI.models.User;
import com.wallet.SimpleWalletAPI.models.Wallet;
import com.wallet.SimpleWalletAPI.repositories.UserRepository;
import com.wallet.SimpleWalletAPI.repositories.WalletRepository;
import com.wallet.SimpleWalletAPI.security.services.UserDetailsImpl;
import com.wallet.SimpleWalletAPI.templates.UserRequest;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.web.server.ResponseStatusException;

import java.util.List;

@Service
public class UserServiceImpl implements UserService {

    @Autowired
    private UserRepository userRepository;
    @Autowired
    private PasswordEncoder passwordEncoder;
    @Autowired
    private WalletRepository walletRepository;

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

        Wallet primaryWallet = new Wallet();
        primaryWallet.setWalletName("Primary Wallet");
        primaryWallet.setPrimary(true);
        primaryWallet.setUser(user);

        user.setPrimaryWallet(primaryWallet);
        userRepository.save(user);
        return user;
    }

    @Override
    public User getUserById(Long userId) {
        return userRepository.findById(userId)
                .orElseThrow(() -> new ResponseStatusException(HttpStatus.NOT_FOUND, "User not found"));
    }

    @Override
    public List<Wallet> getUserWallets(Long userId) {
        User user = getUserById(userId);
        return user.getWallets();
    }

    @Override
    public Wallet getPrimaryWallet(Long userId) {
        User user = getUserById(userId);
        return user.getPrimaryWallet();
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

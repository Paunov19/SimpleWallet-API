package com.wallet.SimpleWalletAPI.controllers;

import com.wallet.SimpleWalletAPI.models.User;
import com.wallet.SimpleWalletAPI.payload.LoginRequest;
import com.wallet.SimpleWalletAPI.payload.UserRequest;
import com.wallet.SimpleWalletAPI.repositories.UserRepository;
import com.wallet.SimpleWalletAPI.security.jwt.JwtResponse;
import com.wallet.SimpleWalletAPI.security.jwt.JwtUtils;
import com.wallet.SimpleWalletAPI.security.services.UserDetailsImpl;
import com.wallet.SimpleWalletAPI.services.UserService;
import io.swagger.v3.oas.annotations.Operation;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.authentication.AuthenticationManager;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.AuthenticationException;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/users")
public class UserController {

    @Autowired
    private UserService userService;
    @Autowired
    private UserRepository userRepository;
    @Autowired
    private AuthenticationManager authenticationManager;
    @Autowired
    private JwtUtils jwtUtils;

    @Operation(
            summary = "Register a new user",
            description = "This endpoint allows a new user to register by providing their details such as name, email, and password. " +
                    "The user will be registered and can then log in to the system."
    )
    @PostMapping("/register")
    public ResponseEntity<User> registerUser(@RequestBody UserRequest userRequest) {
        User user = userService.register(userRequest);
        return new ResponseEntity<>(user, HttpStatus.CREATED);
    }

    @Operation(
            summary = "Login an existing user",
            description = "This endpoint allows an existing user to log in by providing their email and password. " +
                    "If the credentials are correct, the user will receive a JWT token to authenticate future requests."
    )
    @PostMapping("/login")
    public ResponseEntity<?> login(@RequestBody LoginRequest loginRequest) {
        try {
            Authentication authentication = authenticationManager.authenticate(
                    new UsernamePasswordAuthenticationToken(loginRequest.email(), loginRequest.password()));

            SecurityContextHolder.getContext().setAuthentication(authentication);
            String jwt = jwtUtils.generateJwtToken(authentication);

            UserDetailsImpl userDetails = (UserDetailsImpl) authentication.getPrincipal();

            return ResponseEntity.ok(new JwtResponse(jwt,
                    userDetails.getId(),
                    userDetails.getEmail(),
                    userDetails.getName()));
        } catch (AuthenticationException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Wrong email or password");
        }
    }
}
package com.wallet.SimpleWalletAPI.mappers;

import com.wallet.SimpleWalletAPI.dtos.UserDTO;
import com.wallet.SimpleWalletAPI.dtos.WalletDTO;
import com.wallet.SimpleWalletAPI.models.User;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class UserMapper {

    public UserDTO entityToDTO(User user) {
        UserDTO userDTO = new UserDTO();
        userDTO.setName(user.getName());
        userDTO.setEmail(user.getEmail());

        List<WalletDTO> walletsDTO = user.getWallets().stream()
                .map(wallet -> new WalletDTO(wallet.getWalletName(), wallet.getWalletCode(), wallet.getBalance(),
                        wallet.getCurrency().toString(), wallet.isPrimary()))
                .collect(Collectors.toList());

        userDTO.setWallets(walletsDTO);

        return userDTO;
    }
}
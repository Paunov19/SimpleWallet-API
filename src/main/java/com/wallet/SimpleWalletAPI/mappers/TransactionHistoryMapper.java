package com.wallet.SimpleWalletAPI.mappers;

import com.wallet.SimpleWalletAPI.dtos.TransactionHistoryDTO;
import com.wallet.SimpleWalletAPI.models.TransactionHistory;
import org.springframework.stereotype.Component;

import java.time.format.DateTimeFormatter;

@Component
public class TransactionHistoryMapper {

    private static final DateTimeFormatter TIMESTAMP_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd'  'HH:mm:ss");

    public TransactionHistoryDTO entityToDto(TransactionHistory transactionHistory) {
        TransactionHistoryDTO dto = new TransactionHistoryDTO();
        dto.setAmount(transactionHistory.getAmount());
        dto.setCurrency(transactionHistory.getCurrency().toString());
        dto.setTransactionType(transactionHistory.getTransactionType().toString());
        dto.setTimestamp(transactionHistory.getTimestamp().format(TIMESTAMP_FORMATTER));
        dto.setWalletName(transactionHistory.getWallet().getWalletName());
        dto.setWalletCode(transactionHistory.getWallet().getWalletCode());
        dto.setUserName(transactionHistory.getUser().getName());
        dto.setDescription(transactionHistory.getDescription());
        return dto;
    }
}
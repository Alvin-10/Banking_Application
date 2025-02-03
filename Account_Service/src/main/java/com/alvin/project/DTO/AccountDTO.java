package com.alvin.project.DTO;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@AllArgsConstructor
@NoArgsConstructor
public class AccountDTO {
	private Long userId;
	private String senderAccountNumber;
	private String receiverAccountNumber;
	private double amount;
}
package com.alvin.project.Controller;

import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

import com.alvin.project.DTO.AccountDTO;
import com.alvin.project.Model.Account;
import com.alvin.project.Service.AccountService;

import lombok.AllArgsConstructor;

@RestController
@RequestMapping("/accounts")
@AllArgsConstructor
public class AccountController {

	private AccountService accountService;

	@PostMapping("/create")
	public ResponseEntity<Account> createAccount(@RequestBody AccountDTO accountDTO) {
		Account account = accountService.createAccountForUser(accountDTO);
		return ResponseEntity.ok(account);
	}

	@PostMapping("/send")
	public ResponseEntity<Account> sendMoney(@RequestBody AccountDTO accountDTO) {
		Account account = accountService.sendMoney(accountDTO);
		return ResponseEntity.ok(account);
	}

	@PostMapping("/add")
	public ResponseEntity<Account> addMoney(@RequestBody AccountDTO accountDTO) {
		Account account = accountService.addMoney(accountDTO);
		return ResponseEntity.ok(account);
	}

	@GetMapping("/balance/{accountNumber}")
	public ResponseEntity<Double> viewBalance(@PathVariable String accountNumber) {
		double balance = accountService.viewBalance(accountNumber);
		return ResponseEntity.ok(balance);
	}
}
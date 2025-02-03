package com.alvin.project.Service;

import com.alvin.project.DTO.AccountDTO;
import com.alvin.project.DTO.TransactionDTO;
import com.alvin.project.Model.Account;
import com.alvin.project.Repository.AccountRepository;
import com.alvin.project.exception.AccountNotFoundException;
import com.alvin.project.exception.InsufficientBalanceException;

import lombok.AllArgsConstructor;

import org.springframework.http.HttpEntity;
import org.springframework.http.HttpHeaders;
import org.springframework.http.MediaType;
import org.springframework.stereotype.Service;
import org.springframework.web.client.RestTemplate;

@Service
@AllArgsConstructor
public class AccountServiceImpl implements AccountService {

   
    private AccountRepository accountRepository;

  
    private RestTemplate restTemplate;

    private static final String USER_SERVICE_URL = "http://localhost:8081/users";
    private static final String TRANSACTION_SERVICE_URL = "http://localhost:8083/transactions";

    @Override
    public Account sendMoney(AccountDTO accountDTO) {
        Account senderAccount = accountRepository.findByAccountNumber(accountDTO.getSenderAccountNumber());
        Account receiverAccount = accountRepository.findByAccountNumber(accountDTO.getReceiverAccountNumber());

        if (senderAccount == null) {
            throw new AccountNotFoundException("Sender account not found with account number: " + accountDTO.getSenderAccountNumber());
        }
        if (receiverAccount == null) {
            throw new AccountNotFoundException("Receiver account not found with account number: " + accountDTO.getReceiverAccountNumber());
        }
        if (senderAccount.getBalance() < accountDTO.getAmount()) {
            throw new InsufficientBalanceException("Insufficient balance in sender account: " + accountDTO.getSenderAccountNumber());
        }

        senderAccount.setBalance(senderAccount.getBalance() - accountDTO.getAmount());
        receiverAccount.setBalance(receiverAccount.getBalance() + accountDTO.getAmount());
        accountRepository.save(senderAccount);
        accountRepository.save(receiverAccount);

        // Save transaction for sender
        TransactionDTO senderTransaction = new TransactionDTO(accountDTO.getSenderAccountNumber(), accountDTO.getAmount(), "debit", String.valueOf(System.currentTimeMillis()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TransactionDTO> senderRequest = new HttpEntity<>(senderTransaction, headers);
        restTemplate.postForObject(TRANSACTION_SERVICE_URL + "/add", senderRequest, Void.class);

        // Save transaction for receiver
        TransactionDTO receiverTransaction = new TransactionDTO(accountDTO.getReceiverAccountNumber(), accountDTO.getAmount(), "credit", String.valueOf(System.currentTimeMillis()));
        HttpEntity<TransactionDTO> receiverRequest = new HttpEntity<>(receiverTransaction, headers);
        restTemplate.postForObject(TRANSACTION_SERVICE_URL + "/add", receiverRequest, Void.class);

        return senderAccount;
    }

    @Override
    public double viewBalance(String accountNumber) {
        Account account = accountRepository.findByAccountNumber(accountNumber);
        if (account == null) {
            throw new AccountNotFoundException("Account not found with account number: " + accountNumber);
        }
        return account.getBalance();
    }

    @Override
    public Account addMoney(AccountDTO accountDTO) {
        Account account = accountRepository.findByAccountNumber(accountDTO.getSenderAccountNumber());
        if (account == null) {
            throw new AccountNotFoundException("Account not found with account number: " + accountDTO.getSenderAccountNumber());
        }
        account.setBalance(account.getBalance() + accountDTO.getAmount());
        accountRepository.save(account);

        // Save transaction
        TransactionDTO transactionDTO = new TransactionDTO(accountDTO.getSenderAccountNumber(), accountDTO.getAmount(), "credit", String.valueOf(System.currentTimeMillis()));
        HttpHeaders headers = new HttpHeaders();
        headers.setContentType(MediaType.APPLICATION_JSON);
        HttpEntity<TransactionDTO> request = new HttpEntity<>(transactionDTO, headers);
        restTemplate.postForObject(TRANSACTION_SERVICE_URL + "/add", request, Void.class);

        return account;
    }

    @Override
    public Account createAccountForUser(AccountDTO accountDTO) {
        Account account = new Account();
        account.setAccountNumber(accountDTO.getSenderAccountNumber());
        account.setUserId(accountDTO.getUserId());
        account.setBalance(0.0); // Initialize balance to 0
        return accountRepository.save(account);
    }

    private AccountDTO getAccountDTOFromUserService(Long userId) {
        return restTemplate.getForObject(USER_SERVICE_URL + "/accountNumber/" + userId, AccountDTO.class);
    }
}
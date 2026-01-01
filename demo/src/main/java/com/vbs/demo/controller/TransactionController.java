package com.vbs.demo.controller;

import com.vbs.demo.dto.TransactionDto;
import com.vbs.demo.dto.TransferDto;
import com.vbs.demo.models.Notification;
import com.vbs.demo.models.Transaction;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.NotificationRepo;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class TransactionController {
    @Autowired
    TransactionRepo transactionRepo;
    @Autowired
    UserRepo userRepo;

    @Autowired
    NotificationRepo notificationRepo;


    @PostMapping("/deposit")
    public String deposit(@RequestBody TransactionDto obj){
        User user = userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("Wrong id"));
        double newBalance = user.getBalance() +obj.getAmount();
        user.setBalance(newBalance);

        userRepo.save(user);

        Transaction t = new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newBalance);
        t.setDescription("Rs. "+obj.getAmount()+" Deposit Successful");
        t.setUserId(obj.getId());
        transactionRepo.save(t);
        return "Deposit Successful";
    }

    @PostMapping("/withdraw")
    public String withdraw(@RequestBody TransactionDto obj){
        User user = userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("Wrong id"));
        double newBalance = user.getBalance() - obj.getAmount();
        user.setBalance(newBalance);
        if(newBalance<0){
            return "Insufficient Balance";
        }
        userRepo.save(user);
        if (newBalance <= 0.01) {
            Notification n = new Notification();
            n.setUserId(obj.getId());
            n.setUsername(user.getUsername());
            n.setMessage("Your account balance is zero. Please deposit funds.");
            notificationRepo.save(n);
        }


        Transaction t = new Transaction();
        t.setAmount(obj.getAmount());
        t.setCurrBalance(newBalance);
        t.setDescription("Rs. "+obj.getAmount()+" Withdrawal Successful");
        t.setUserId(obj.getId());
        transactionRepo.save(t);
        return "Withdrawal Successful";
    }

    @PostMapping("/transfer")
    public String transfer(@RequestBody TransferDto obj) {

        User sender = userRepo.findById(obj.getId())
                .orElseThrow(() -> new RuntimeException("Invalid sender ID"));


        User rec = userRepo.findByUsername(obj.getUsername());
        if(rec==null){

           double sbalance = sender.getBalance() - obj.getAmount();
           sender.setBalance(sbalance);
           userRepo.save(sender);

            Transaction t1 = new Transaction();
            t1.setAmount(obj.getAmount());
            t1.setCurrBalance(sbalance);
            t1.setDescription("Rs. "+obj.getAmount()+" Sent to user"+obj.getUsername());
            t1.setUserId(obj.getId());
            transactionRepo.save(t1);

            return "Transaction Successful";
        }

        if(sender.getId()== rec.getId()){
            return "Self transaction not allowed";
        }
        if(obj.getAmount()<1){
            return "Invalid Amount";
        }
        double sbalance = sender.getBalance() - obj.getAmount();
        if(sbalance<0){
            return "Insufficient Balance";
        }
        double rbalance = rec.getBalance() + obj.getAmount();
        sender.setBalance(sbalance);
        rec.setBalance(rbalance);
        userRepo.save(sender);
        userRepo.save(rec);
        if (sender.getBalance() <= 0.01) {
            Notification n = new Notification();
            n.setUserId(obj.getId());
            n.setUsername(sender.getUsername());
            n.setMessage("Your account balance is zero after transfer.");
            notificationRepo.save(n);
        }



        Transaction t1 = new Transaction();
        t1.setAmount(obj.getAmount());
        t1.setCurrBalance(sbalance);
        t1.setDescription("Rs. "+obj.getAmount()+" Sent to user"+obj.getUsername());
        t1.setUserId(obj.getId());
        transactionRepo.save(t1);

        Transaction t2 = new Transaction();
        t2.setAmount(obj.getAmount());
        t2.setCurrBalance(rbalance);
        t2.setDescription("Rs. "+obj.getAmount()+" Received from "+sender.getUsername());
        t2.setUserId(rec.getId());
        transactionRepo.save(t2);

        return "Transfer Done Successfully";
    }

    @GetMapping("/passbook/{id}")
    public List<Transaction> getPassbook(@PathVariable int id){
        return transactionRepo.findAllByUserId(id);
    }


}




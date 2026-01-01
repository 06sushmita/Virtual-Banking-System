package com.vbs.demo.controller;

import com.vbs.demo.dto.DisplayDto;
import com.vbs.demo.dto.LoginDto;
import com.vbs.demo.dto.UpdateDto;
import com.vbs.demo.models.History;
import com.vbs.demo.models.Notification;
import com.vbs.demo.models.User;
import com.vbs.demo.repositories.HistoryRepo;
import com.vbs.demo.repositories.NotificationRepo;
import com.vbs.demo.repositories.TransactionRepo;
import com.vbs.demo.repositories.UserRepo;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.data.domain.Sort;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@CrossOrigin(origins = "*")
public class UserController {

    @Autowired
    UserRepo userRepo;

    @Autowired
    TransactionRepo transactionRepo;

    @Autowired
    HistoryRepo historyRepo;

    @Autowired
    NotificationRepo notificationRepo;

    @PostMapping("/register")
    public String register(@RequestBody User user) {

        History history=new History();
        history.setDescription("User Self Created : "+user.getUsername());
        historyRepo.save(history);


        userRepo.save(user);

        return "Signup Successful";
    }

    @PostMapping("/login")
    public String login(@RequestBody LoginDto u){
        User user = userRepo.findByUsername(u.getUsername());

        if(user == null){
            return "User not found";
        }
        if(!u.getPassword().equals(user.getPassword())){
            return "Password incorrect";
        }
        if(!u.getRole().equals(user.getRole())){
            return "Role incorrect";
        }
        if (user.getBalance() == 0) {
            Notification n = new Notification();
            n.setUserId(user.getId());
            n.setUsername(user.getUsername());
            n.setMessage("Your account balance is zero after transfer.");

            notificationRepo.save(n);
        }
        return String.valueOf(user.getId());
    }

    @GetMapping("/get-details/{id}")
    public DisplayDto display(@PathVariable int id)
    {
        User user = userRepo.findById(id).orElseThrow(()->new RuntimeException("User not found"));

        DisplayDto displayDto = new DisplayDto();
        displayDto.setUsername(user.getUsername());
        displayDto.setBalance(user.getBalance());
        return displayDto;

    }

    @PostMapping("/update")
    public String update(@RequestBody UpdateDto obj){
        User user = userRepo.findById(obj.getId()).orElseThrow(()->new RuntimeException("Not Found"));
        History h1 = new History();

        if(obj.getKey().equalsIgnoreCase("name")){
            if(user.getName().equalsIgnoreCase(obj.getValue())) return "Cannot  be Same";


            h1.setDescription("User changed from"+user.getName()+"to"+obj.getValue());
            user.setName(obj.getValue());

        } else if (obj.getKey().equalsIgnoreCase("password")) {
            if(user.getPassword().equalsIgnoreCase(obj.getValue())) return "Cannot  be Same";
            h1.setDescription("User changed password"+user.getUsername());
            user.setPassword(obj.getValue());
        }
        else if(obj.getKey().equalsIgnoreCase("email")){
            User existing = userRepo.findByEmail(obj.getValue());
            if (existing!=null && existing.getId() != user.getId()) {
                return "Email Already Exist";
            }
            h1.setDescription("User changed from"+user.getEmail()+"to"+obj.getValue());

            user.setEmail(obj.getValue());
        }
        else{
            return "Invalid";
        }

        historyRepo.save(h1);
        userRepo.save(user);
        return  "Update Done Successfully";
    }

    @PostMapping("/add/{adminId}")
    public String add(@RequestBody User user,@PathVariable int adminId){

        History history= new History();
        history.setDescription("Added "+user.getUsername()+" By "+adminId);
        historyRepo.save(history);
        userRepo.save(user);
        return "Successfully added";
    }

    @GetMapping("/users")
    public List<User> getAllUsers(@RequestParam String sortBy,@RequestParam String order){
        Sort sort;
        if(order.equalsIgnoreCase("desc")){
            sort = Sort.by(sortBy).descending();
        }
        else{
            sort = Sort.by(sortBy).ascending();
        }
        return userRepo.findAllByRole("customer",sort);
    }

   @DeleteMapping("/delete-user/{userId}/admin/{adminId}")
   public String deleteUser(@PathVariable int userId,@PathVariable int adminId){
        User user = userRepo.findById(userId).orElseThrow(()->new RuntimeException("Not found"));
        if(user.getBalance()>0){
            return "Balance should be zero";
        }
        userRepo.delete(user);
       History history=new History();
       history.setDescription("Deleted"+user.getUsername()+" By "+adminId);
       historyRepo.save(history);

        return "User Deleted Successfully";
   }


    @GetMapping("/users/{keyword}")
    public List<User> getUsers(@PathVariable String keyword){
        return userRepo.findByUsernameContainingIgnoreCaseAndRole(keyword,"customer");
    }

}
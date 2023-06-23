package com.reddit.controller;


import com.reddit.entity.User;
import com.reddit.service.ProfilePhotoService;
import com.reddit.service.UserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.multipart.MultipartFile;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

@Controller
public class UserController {
   @Autowired
   private UserService userService;
   @Autowired
   private ProfilePhotoService profilePhotoService;
    private static Logger log =  LoggerFactory.getLogger(UserController.class);
   public static String uploadDirectory = System.getProperty("user.dir") + "/uploads";

    @GetMapping("/login")
    public String userLogin(Model model){
        System.out.println("its working");
        return "LoginPage.html";
    }
    @GetMapping("/chuck-user")
    public String checkLoginUser(@RequestParam("username")String username,@RequestParam("password")String password,Model model){
        System.out.println("username="+username+" password="+password);

        User user=userService.isUsernameAndPasswordCorrect(username,password);
        model.addAttribute("user",user);
        if(user != null){
            return "UserProfile";

        }
        return "LoginPage.html";
    }
    @PostMapping("/addUser")
    public String addUser(@ModelAttribute("user")User user,Model model){
        System.out.println("username= "+user.getUsername());
        System.out.println("email= "+user.getEmail());
        userService.saveUser(user);
       //User displayUser=userService.getUserByID(userResult.getUserId());
       // model.addAttribute("user",userResult);
        return "redirect:/login";
    }
    @GetMapping("/newRegister")
    public String registerPage(Model model){
        System.out.println("user added");
        model.addAttribute("user",new User());
        return "RegistrationPage.html";
    }

    @PostMapping("/upload")
    public String createEmployee(@RequestParam("userId")String userIdString, @RequestParam("file") MultipartFile file, Model model) {
        Long userId = Long.parseLong(userIdString);
         User newUser=userService.getUserByID(userId);
          String previousFilePath=null;

             if(newUser.getProfilePhoto() != null){
                  previousFilePath=newUser.getProfilePhoto().getFileName();

             }
        System.out.println("previousFilePath "+previousFilePath);


        try {
            String fileName = file.getOriginalFilename();
            String randomId = UUID.randomUUID().toString();
            System.out.println("UUID.randomUUID().toString() =" + UUID.randomUUID().toString());
            String fileName1 = randomId.concat(fileName.substring(fileName.lastIndexOf(".")));
            System.out.println("randomId.concat(name.substring(name.lastIndexOf('.'))" + fileName1);
            String filePath = Paths.get(uploadDirectory, fileName1).toString();
            BufferedOutputStream stream = new BufferedOutputStream(new FileOutputStream(new File(filePath)));
            stream.write(file.getBytes());
            stream.close();
            profilePhotoService.saveProfilePhoto(fileName1, userId);

            if (previousFilePath != null) {
                Path previousFilePathObj = Paths.get(uploadDirectory, previousFilePath);
                if (Files.exists(previousFilePathObj)) {
                    try {
                        Files.delete(previousFilePathObj);
                        System.out.println("Previous file deleted successfully.");
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                }
            }
        }
        catch (Exception e) {
            e.printStackTrace();
        }
            User user = userService.getUserByID(userId);
            model.addAttribute("user", user);
        return "UserProfile";
    }

    @GetMapping("/selectPhoto")
    public String selectPhoto(@RequestParam("userId")String userIdString,Model model){
        Long userId=Long.parseLong(userIdString);
        model.addAttribute("userId",userId);
        return "ChooseProfilePhoto";
    }

    @GetMapping("/change_password_email")
    public String changePasswordAndEmail(@RequestParam("userId")Long userId,Model model){
        System.out.println(userId);
        model.addAttribute("userId",userId);
        return "user-setting";
    }
    @PostMapping("/save_password")
    public String saveNewPassword(@RequestParam("userId")Long userId,@RequestParam("password")String password,@RequestParam("confirm-password")String confirmPassword,Model model){
        System.out.println("userId"+userId);
        System.out.println("password="+password);
        System.out.println("confirmPassword"+confirmPassword);
        model.addAttribute("userId",userId);
        if(password.equals(confirmPassword)){
            User user=userService.getUserByID(userId);
            user.setPassword(confirmPassword);
            userService.saveUser(user);
            User savedUser=userService.getUserByID(userId);
            model.addAttribute("user",savedUser);
            return "UserProfile";
        }
        return "user-setting";
    }

    @PostMapping("/save_email")
    public String saveNewEmail(@RequestParam("userId")Long userId,@RequestParam("email")String email,Model model){
        model.addAttribute("userId",userId);
        System.out.println("email ="+email);
        User user=userService.getUserByID(userId);
        user.setEmail(email);
        userService.saveUser(user);
        User savedUser=userService.getUserByID(userId);
        model.addAttribute("user",savedUser);
        return "UserProfile";
    }

    @GetMapping("/cancel_changes")
    public String cancelChanges(@RequestParam("userId")Long userId,Model model){
        User user=userService.getUserByID(userId);
        model.addAttribute("user",user);
        return "UserProfile";
    }
    @GetMapping("/view-profile")
    public String profileView(@RequestParam("userId") Long userId,Model model){
        User user=userService.getUserByID(userId);
        model.addAttribute("user",user);
        return "UserProfile";
    }
}

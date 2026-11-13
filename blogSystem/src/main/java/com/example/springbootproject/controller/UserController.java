package com.example.springbootproject.controller;

import com.example.springbootproject.pojo.Result;
import com.example.springbootproject.pojo.UserDTO;
import com.example.springbootproject.utils.UserHolder;
import com.example.springbootproject.pojo.loginForm;
import com.example.springbootproject.service.IUserService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

import javax.servlet.http.HttpServletRequest;

@RequestMapping("/user")
@RestController
public class UserController {
    private static final Logger log = LoggerFactory.getLogger(UserController.class);
    @Autowired
    private IUserService userService;
    @PostMapping("/code")
    public Result sendCode(@RequestParam("phone") String phone) {

        return userService.sendCode(phone);
    }
  @PostMapping("/login")
    public Result login(@RequestBody loginForm loginForm) {
     return userService.login(loginForm);
  }

  @PostMapping("/register")
public Result register(@RequestBody loginForm loginForm) {
        return userService.register(loginForm);
  }

  // ==================== 邮箱认证 ====================

  @PostMapping("/email/code")
  public Result sendEmailCode(@RequestParam("email") String email) {
      return userService.sendEmailCode(email);
  }

  @PostMapping("/email/login")
  public Result emailLogin(@RequestBody loginForm loginForm) {
      return userService.emailLogin(loginForm);
  }

  @PostMapping("/email/register")
  public Result emailRegister(@RequestBody loginForm loginForm) {
      return userService.emailRegister(loginForm);
  }

  @PostMapping("/email/forget")
  public Result emailForget(@RequestBody loginForm loginForm) {
      return userService.emailForget(loginForm);
  }

  @PostMapping("/updatePassword")
    public Result updatePassword(@RequestBody loginForm loginForm) {
        return  userService.updatePassword(loginForm);
  }

  @PostMapping("/forget")
    public Result forget(@RequestBody loginForm loginForm) {
        return userService.forget(loginForm);
  }

  @GetMapping("/info")
    public Result info() {
      UserDTO dto = UserHolder.getUser();
      if(dto==null){
          log.info("未登录，请登录！！！");
          return Result.fail("未登录，请登录！！！");
      }
      Long id = dto.getId();
      return userService.getUserInfo(id);
  }


  @GetMapping("/{id}")
    public Result getUserProfile(@PathVariable Long id) {
        return userService.getUserProfileById(id);
    }

  @PutMapping("/profile")
    public Result updateProfile(@RequestBody UserDTO userDTO, HttpServletRequest request) {
        String token = extractToken(request);
        return userService.updateProfile(userDTO, token);
  }

  private String extractToken(HttpServletRequest request) {
      String authHeader = request.getHeader("Authorization");
      if (authHeader != null && authHeader.startsWith("Bearer ")) {
          return authHeader.substring(7);
      }
      return null;
  }

  @PostMapping("/logout")
    public Result logout(HttpServletRequest request) {
        String authHeader = request.getHeader("Authorization");
        if (authHeader == null || !authHeader.startsWith("Bearer ")) {
            return Result.fail("未登录");
        }
        String token = authHeader.substring(7);
        return userService.logout(token);
  }

}

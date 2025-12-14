package com.mianzi.showticketsystem.controller;

import com.mianzi.showticketsystem.model.entity.User;
import com.mianzi.showticketsystem.service.UserService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.*;

/**
 * 用户接口控制器
 * 接收来自前端的HTTP请求，并调用Service层（UserService）的业务逻辑
 */
//这是一个组合注解，等同于 @Controller 和 @ResponseBody
//@Controller：标记此类为一个 Spring MVC 控制器
//@ResponseBody：表示该类所有方法的返回值将直接作为 HTTP 响应体（通常是 JSON 或字符串）
//返回给浏览器/前端
@RestController
//定义了该控制器下所有接口的 根路径
//意味着在这个类中定义的任何接口，其完整路径都将以 /api/user 开头
@RequestMapping("/api/user")
public class UserController {

    //依赖注入: 自动注入 UserService 接口的实现类（即 UserServiceImpl）
    //以便调用业务逻辑
    @Autowired
    //创建Service对象调用业务方法
    private UserService userService;

    /**
     * 用户注册接口
     * 完整请求路径: POST /api/user/register
     */
    @PostMapping("/register")
    //@RequestParam: 告诉 Spring MVC
    //从 HTTP 请求的 查询参数 (Query Parameter) 或 表单数据 (Form Data) 中
    //获取 username 和 password 的值
    //返回值是 String，意味着该接口返回一个简单的文本消息给前端
    public String register(@RequestParam String username, @RequestParam String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return "注册失败：用户名或密码不能为空。";
        }

        //传递给 Service 层的 register 方法，执行核心业务逻辑（检查用户名、插入数据库等）
        boolean success = userService.register(username, password);

        //根据 Service 层返回的布尔值返回不同的文本信息
        if (success) {
            return "注册成功！";
        } else {
            return "注册失败：用户名已被使用。";
        }
    }

    /**
     * 用户登录接口 (返回用户身份信息，不返回 Token)
     * 完整请求路径: POST /api/user/login
     */
    //标记该方法处理POST请求
    @PostMapping("/login")
    //参数: 用户名和密码
    public String login(@RequestParam String username, @RequestParam String password) {
        if (username == null || username.isEmpty() || password == null || password.isEmpty()) {
            return "登录失败：用户名或密码不能为空。";
        }

        //调用业务逻辑: 调用 Service 层的 login 方法，尝试登录
        //Service 层负责查询数据库和比对密码
        User user = userService.login(username, password);

        if (user != null) {
            //返回用户 ID 或欢迎信息
            return "登录成功！欢迎，" + user.getUsername() + "。您的ID是: " + user.getId();
        } else {
            return "登录失败：用户名或密码错误。";
        }
    }
}

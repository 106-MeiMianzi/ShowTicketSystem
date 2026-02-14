package com.mianzi.showticketsystem;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * Spring Boot 应用程序主类
 * 
 * 这是整个应用的入口点，Spring Boot 会从这个类开始启动整个应用
 * 
 * 作用：
 * 1. 标识这是一个 Spring Boot 应用
 * 2. 启动内嵌的 Web 服务器（如 Tomcat）
 * 3. 自动扫描并加载 Spring Bean（组件）
 * 4. 读取 application.properties 配置文件
 */
@SpringBootApplication
/**
 * @SpringBootApplication 注解说明：
 * 
 * 这是一个组合注解，包含了以下三个注解的功能：
 * 
 * 1. @SpringBootConfiguration
 *    - 标识这是一个 Spring Boot 配置类
 *    - 等价于 @Configuration（Spring 的配置类注解）
 * 
 * 2. @EnableAutoConfiguration
 *    - 启用 Spring Boot 的自动配置功能
 *    - Spring Boot 会根据 classpath 中的依赖自动配置相应的组件
 *    - 例如：检测到 spring-boot-starter-web，就自动配置 Web 服务器
 *    - 例如：检测到 MySQL 驱动，就自动配置数据源
 * 
 * 3. @ComponentScan
 *    - 自动扫描当前包及其子包下的所有组件
 *    - 扫描带有 @Component、@Service、@Repository、@Controller 等注解的类
 *    - 并将它们注册为 Spring Bean，供依赖注入使用
 * 
 * 默认扫描范围：当前类所在的包（com.mianzi.showticketsystem）及其所有子包
 */
@EnableScheduling
/**
 * @EnableScheduling 注解说明：
 * 
 * 启用 Spring 的定时任务功能
 * 
 * 作用：
 * - 允许在项目中使用 @Scheduled 注解来创建定时任务
 * - 例如：定时清理过期订单、定时发送邮件、定时更新数据等
 * 
 * 使用示例：
 * @Component
 * public class ScheduledTasks {
 *     @Scheduled(fixedRate = 5000)  // 每5秒执行一次
 *     public void doSomething() {
 *         // 执行定时任务
 *     }
 * }
 */
public class ShowTicketSystemApplication {
    /**
     * main 方法：Java 应用程序的入口点
     * 
     * 当运行这个类时，JVM 会首先执行这个方法
     * 
     * @param args 命令行参数（String 数组）
     *             可以通过命令行传递参数，如：java -jar app.jar --server.port=9090
     */
    public static void main(String[] args) {
        /**
         * SpringApplication.run() 方法说明：
         * 
         * 这是 Spring Boot 启动应用的核心方法
         * 
         * 参数1：ShowTicketSystemApplication.class
         *        - 主配置类的 Class 对象
         *        - Spring Boot 会从这个类开始扫描组件和配置
         * 
         * 参数2：args
         *        - 命令行参数数组
         *        - 可以覆盖 application.properties 中的配置
         *        - 例如：--server.port=9090 会覆盖配置文件中的端口设置
         * 
         * 执行流程：
         * 1. 创建 Spring 应用上下文（ApplicationContext）
         * 2. 加载并解析 application.properties 配置文件
         * 3. 扫描并注册所有 Spring Bean（@Component、@Service、@Controller 等）
         * 4. 启动内嵌的 Web 服务器（默认是 Tomcat，端口 8080）
         * 5. 等待 HTTP 请求
         * 
         * 返回值：ConfigurableApplicationContext
         *        - 可以用于获取应用上下文，进行一些自定义操作
         *        - 在这个简单示例中，我们没有接收返回值
         */
        SpringApplication.run(ShowTicketSystemApplication.class, args);
    }

}

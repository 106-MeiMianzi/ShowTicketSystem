package com.mianzi.showticketsystem.config;

import com.mianzi.showticketsystem.filter.JwtAuthenticationFilter;
import com.mianzi.showticketsystem.interceptor.RateLimitInterceptor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

/**
 * Web配置类
 * 
 * 作用：
 * - 配置Web相关的组件（如过滤器、拦截器等）
 * - 注册JWT认证过滤器
 * - 配置过滤器的拦截路径和顺序
 */
@Configuration
/**
 * @Configuration 注解说明：
 * 
 * 标识这是一个Spring配置类
 * 
 * 作用：
 * - Spring Boot会自动扫描这个类
 * - 类中带有@Bean注解的方法会被执行，创建Bean并注册到Spring容器
 * - 配置类通常用于配置第三方组件或自定义组件
 */
public class WebConfig implements WebMvcConfigurer {

    /**
     * 注入JWT认证过滤器
     * 
     * @Autowired 注解说明：
     * - 自动注入JwtAuthenticationFilter的实例
     * - Spring会从容器中找到JwtAuthenticationFilter并注入
     * - JwtAuthenticationFilter必须标注@Component或@Bean才能被注入
     */
    @Autowired
    private JwtAuthenticationFilter jwtAuthenticationFilter;

    /**
     * 注入限流拦截器
     */
    @Autowired
    private RateLimitInterceptor rateLimitInterceptor;

    /**
     * 注册JWT过滤器
     * 
     * @Bean 注解说明：
     * - 标识这是一个Bean定义方法
     * - Spring会执行这个方法，将返回值注册为Bean
     * - 方法名（jwtFilter）就是Bean的名称
     * 
     * @return FilterRegistrationBean对象，包含过滤器的配置信息
     * 
     * 配置说明：
     * - setFilter()：设置要注册的过滤器
     * - addUrlPatterns()：设置过滤器拦截的URL模式
     * - setOrder()：设置过滤器的执行顺序（数字越小越先执行）
     */
    @Bean
    public FilterRegistrationBean<JwtAuthenticationFilter> jwtFilter() {
        /**
         * 创建FilterRegistrationBean对象
         * 
         * FilterRegistrationBean是Spring Boot提供的过滤器注册类
         * 用于将Servlet Filter注册到Servlet容器中
         */
        FilterRegistrationBean<JwtAuthenticationFilter> registrationBean = new FilterRegistrationBean<>();
        
        /**
         * 设置要注册的过滤器
         * 
         * jwtAuthenticationFilter是JWT认证过滤器的实例
         * 这个过滤器会拦截请求并验证JWT Token
         */
        registrationBean.setFilter(jwtAuthenticationFilter);
        
        /**
         * 设置过滤器拦截的URL模式
         * 
         * "/api/*"：拦截所有以/api/开头的请求
         * 
         * 说明：
         * - 所有API请求都需要经过JWT认证
         * - 过滤器内部会排除登录等不需要认证的接口
         * - 可以使用多个模式，如：addUrlPatterns("/api/*", "/admin/*")
         */
        registrationBean.addUrlPatterns("/api/*");
        
        /**
         * 设置过滤器的执行顺序
         * 
         * order = 1：执行顺序为1（数字越小越先执行）
         * 
         * 说明：
         * - 如果有多个过滤器，order值小的先执行
         * - JWT认证过滤器应该在其他业务过滤器之前执行
         * - 确保在业务逻辑执行前完成身份认证
         */
        registrationBean.setOrder(1);
        
        /**
         * 返回配置好的FilterRegistrationBean
         * 
         * Spring Boot会自动将这个Bean注册到Servlet容器中
         * 过滤器会在请求到达Controller之前执行
         */
        return registrationBean;
    }

    /**
     * 注册拦截器
     * 
     * @param registry 拦截器注册表
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // TODO：这里可以选择哪些路径需要被排除在拦截器之外，不需要你在过滤器中设置EXCLUDE_PATHS
        registry.addInterceptor(rateLimitInterceptor)
                .addPathPatterns("/api/**"); // 拦截所有API请求
    }
}

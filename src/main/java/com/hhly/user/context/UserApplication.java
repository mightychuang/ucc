package com.hhly.user.context;
import com.hhly.user.config.WebConfiguration;
import com.hhly.common.spring.BaseConfiguration;
import com.hhly.common.spring.ServiceClientConfiguration;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Import;


@SpringBootApplication
@Import({BaseConfiguration.class,
        ServiceClientConfiguration.class,
        WebConfiguration.class})
public class UserApplication {

    public static void main(String[] args) {
        SpringApplication application = new SpringApplication(UserApplication.class);
        application.run(args);
    }

//    @Bean
//    public InitBindProducer initBindProducer() {
//
//        InitBindProducer initBindProducer = new InitBindProducer();
////        initBindProducer.addPreInitializeProducers(EventType.USER_CREATED);
//        return initBindProducer;
//    }
/*
    @Bean
    public ServletRegistrationBean druidServlet() {
        ServletRegistrationBean reg = new ServletRegistrationBean();
        reg.setServlet(new StatViewServlet());
        reg.addUrlMappings("/druid*//*");
        //白名单：
        //reg.addInitParameter("allow","127.0.0.1");
        //IP黑名单 (存在共同时，deny优先于allow) : 如果满足deny的话提示:Sorry, you are not permitted to view this page.
        //reg.addInitParameter("deny","192.168.1.73");
        reg.addInitParameter("loginUsername", "admin");
        reg.addInitParameter("loginPassword", "admin");
        reg.addInitParameter("logSlowSql", "true");
        //是否能够重置数据.
        reg.addInitParameter("resetEnable","false");
        return reg;
    }

    @Bean
    public FilterRegistrationBean filterRegistrationBean() {
        FilterRegistrationBean filterRegistrationBean = new FilterRegistrationBean();
        filterRegistrationBean.setFilter(new WebStatFilter());
        filterRegistrationBean.addUrlPatterns("*//*");
        filterRegistrationBean.addInitParameter("exclusions", "*.js,*.gif,*.jpg,*.png,*.css,*.ico,/druid*//*");
        filterRegistrationBean.addInitParameter("profileEnable", "true");
        return filterRegistrationBean;
    }*/


}
package com.hhly.user.config;

import com.github.miemiedev.mybatis.paginator.domain.PageBounds;
import com.hhly.common.Const;
import com.hhly.user.interceptor.GolbalInterceptor;
import org.apache.commons.lang3.math.NumberUtils;
import org.springframework.context.annotation.Configuration;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurerAdapter;

import java.util.List;

/**
* @author wangxianchen
* @create 2017-08-24
* @desc 添加自定义拦截器
*/
@Configuration
public class WebConfiguration extends WebMvcConfigurerAdapter {

    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        registry.addInterceptor(new GolbalInterceptor()).addPathPatterns("/**");
        super.addInterceptors(registry);
    }
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> argumentResolvers) {
        // 如果 Controller 方法中的参数有 PageBounds 则从前台获取数据组装, 如果没有传递则给设置一个默认值
        argumentResolvers.add(new HandlerMethodArgumentResolver() {
            @Override
            public boolean supportsParameter(MethodParameter parameter) {
                return PageBounds.class.isAssignableFrom(parameter.getParameterType());
            }
            @Override
            public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer,
                                          NativeWebRequest request, WebDataBinderFactory factory) throws Exception {
                // 判断数据是否合理, 不合理就给定默认值
                int page = NumberUtils.toInt(request.getParameter(Const.GLOBAL_PAGE));
                if (page <= 0) page = Const.DEFAULT_PAGE_NO;
                int limit = NumberUtils.toInt(request.getParameter(Const.GLOBAL_LIMIT));
                if (limit <= 0) limit = Const.DEFAULT_LIMIT;
                return new PageBounds(page, limit);
            }
        });
    }
}

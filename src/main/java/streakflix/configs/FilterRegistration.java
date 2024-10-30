package streakflix.configs;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import streakflix.filters.CorsFilter;

@Configuration
public class FilterRegistration {

    @Autowired
    private CorsFilter securityFilter;

    @Bean
    public FilterRegistrationBean<CorsFilter> loggingFilter(){
        FilterRegistrationBean<CorsFilter> registrationBean
                = new FilterRegistrationBean<>();

        registrationBean.setFilter(securityFilter);
        registrationBean.addUrlPatterns("/api/*");
        registrationBean.setOrder(2);

        return registrationBean;
    }
}

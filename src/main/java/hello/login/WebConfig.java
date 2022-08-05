package hello.login;

import hello.login.web.filter.LogFilter;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import javax.servlet.Filter;

@Configuration
public class WebConfig {

    // 스프링 부트를 이용한 필터 등록 : FilterRegistrationBean
    @Bean
    public FilterRegistrationBean logFilter(){
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        // setFilter() : 등록할 필터를 지정
        filterRegistrationBean.setFilter(new LogFilter());
        // setOrder() : 필터는 체인으로 등록함. 그래서 순서가 필요하므로 지정. 낮을수록 먼저 동작
        filterRegistrationBean.setOrder(1);
        // addUrlPatterns() : 필터를 적용할 URL 패턴 지정. 여러 패턴 지정 가능
        filterRegistrationBean.addUrlPatterns("/*"); // /* : 모든 요청에 대한 필터 적용
        // 참고 : URL 패턴에 대한 룰은 서블릿과 동일
        // 참고 : 다음으로 필터 등록이 가능하지만, 순서 지정이 불가능 하므로 FilterRegistrationBean 을 이용하자.
        // -> @ServletComponentScan @WebFilter(filterName = "logFilter", urlPatterns = "/*")

        // 참고 : 실무에서 HTTP 요청시 같은 요청의 로그에 모두 같은 식별자를 자동으로 남기는 방법은 logback.mdc 참조

        return filterRegistrationBean;
    }
}

package hello.login;

import hello.login.web.argumentresolver.LoginMemberArgumentResolver;
import hello.login.web.filter.LogFilter;
import hello.login.web.filter.LoginCheckFilter;
import hello.login.web.interceptor.LogInterceptor;
import hello.login.web.interceptor.LoginCheckInterceptor;
import org.springframework.boot.web.servlet.FilterRegistrationBean;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.servlet.config.annotation.InterceptorRegistry;
import org.springframework.web.servlet.config.annotation.WebMvcConfigurer;

import javax.servlet.Filter;
import java.util.List;

@Configuration
public class WebConfig implements WebMvcConfigurer {

    // 스프링 부트를 이용한 필터 등록 : FilterRegistrationBean
//    @Bean
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

    /**
     * 로그인 체크 필터
     */
//    @Bean
    public FilterRegistrationBean loginCheckFilter(){
        FilterRegistrationBean<Filter> filterRegistrationBean = new FilterRegistrationBean<>();
        // 로그인 체크 필터 추가
        filterRegistrationBean.setFilter(new LoginCheckFilter());
        // 2번 순서 (1번인 로그 필터 다음으로 실행)
        filterRegistrationBean.setOrder(2);
        // "/*" : 모든 요청에 로그인 체크 필터 적용
        filterRegistrationBean.addUrlPatterns("/*");
        return filterRegistrationBean;
    }

    /**
     * 인터셉터 등록
     * WebMvcConfigurer 가 제공하는 addInterceptors() 이용하여 등록
     */
    @Override
    public void addInterceptors(InterceptorRegistry registry) {
        // 인터셉터와 필터가 중복되지 않도록 필터를 등록하기 위한 logFilter() 의 @Bean 을 주석처리

        registry.addInterceptor(new LogInterceptor()) // 인터셉터 등록
                .order(1) // 순서 지정
                .addPathPatterns("/**") // 인터셉터 적용할 URL 패턴 지정
                .excludePathPatterns("/css/**", "/*.ico", "/error"); // 인터셉터에서 제외할 패턴 지정

        // 필터와의 차이 : addPathPatterns(), excludePathPatterns() 가 각각 제공되므로, 세밀하게 URL 패턴을 지정할 수 있다.

        /* 스프링의 URL 경로 */
        // 스프링이 제공하는 URL 경로는 서블릿 기술이 제공하는 URL 경로와 완전히 다르다.
        // 보다 더 자세하고, 세밀하게 설정할 수 있다.
        // -- 대표적인 표현
        // ? : 문자 1개 일치
        // * : 경로(/) 안에서 0개 이상의 문자 일치
        // ** : 경로 끝까지 0개 이상의 경로(/) 일치

        // 인터셉터와 필터가 중복되지 않도록 필터를 등록하기 위한 loginCheckFilter() 의 @Bean 을 주석처리
        registry.addInterceptor(new LoginCheckInterceptor())
                .order(2) // 2번째 순서 지정
                .addPathPatterns("/**") // 기본적으로 모든 경로에 대하여 인터셉터 적용하되,
                .excludePathPatterns("/", "/members/add", "/login", "/logout", "/css/**", "/*.ico", "/error");
        // 홈(/) ,회원가입(/members/add), 로그인(/login), 로그아웃(/logout), 리소스 조회(/css/**), 에러(/error) 등의
        // 페이지는 로그인 체크 인터셉터를 적욯하지 않도록 excludePathPatterns()에 지정해준다.
    }

    /**
     * LoginMemberArgumentResolver 등록
     */
    @Override
    public void addArgumentResolvers(List<HandlerMethodArgumentResolver> resolvers) {
        resolvers.add(new LoginMemberArgumentResolver());
        // 실행해보면 결과는 동일하지만, 더 편리하게 로그인 정보를 조회할 수 있다.
        // ArgumentResolver 를 활용하면 공통 작업이 필요할 때 컨트롤러를 더욱 편리하게 사용 가능
    }
}
/* 서블릿 필터와 스프링 인터셉터 정리 */
// 서블릿 필터와 스프링 인터셉터는 웹과 관련된 공통 관심자를 해결하기 위한 기술이다.
// 서블릿 필터와 비교해서 스프링 인터셉터가 개발자 입장에서 훨씬 편리하므로 특별한 문제가 없다면 인터셉터를 사용하는 것이 좋다.

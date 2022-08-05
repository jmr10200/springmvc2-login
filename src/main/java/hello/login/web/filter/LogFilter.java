package hello.login.web.filter;

import lombok.extern.slf4j.Slf4j;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import java.io.IOException;
import java.util.UUID;

@Slf4j
public class LogFilter implements Filter {
    // Filter 는 인터페이스이므로 구현해야 한다.

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
        log.info("log filter init");
    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // HTTP 요청이 오면 doFilter() 가 호출된다.
        // ServletRequest 는 HTTP 요청이 아닌 경우까지 고려해서 만든 인터페이스이다.
        // HTTP 를 사용 하기위해 아래와같이 다운 캐스팅 하면 된다.
        HttpServletRequest httpRequest = (HttpServletRequest) request;
        String requestURI = httpRequest.getRequestURI();

        // HTTP 요청을 구분하기 위해 요청당 임의의 uuid 를 생성한다.
        String uuid = UUID.randomUUID().toString();
        try{
            log.info("REQUEST [{}][{}]", uuid, requestURI);
            // 가장 중요
            // 다음 필터가 있으면 필터를 호출하고, 없으면 서블릿을 호출한다.
            // 만약 이 로직을 호출하지 않으면 다음 단계로 진행되지 않는다.
            chain.doFilter(request, response);
        } catch(Exception e) {
            throw e;
        } finally {
            log.info("RESPONSE [{}][{}]", uuid, requestURI);
        }

    }

    @Override
    public void destroy() {
        log.info("log filter destroy");
    }

}
/* 공통 관심사 */
// 로그인 하지 않은 유저에게는 상품 관리 버튼이 안보이지만, 직접 URL 을 호출하면 상품 관리 화면에 들어갈 수 있으면 문제가 된다.
// 상품 관리 컨트롤러에서 로그인 여부를 매번 체크하면 되지만, 번거롭고 공통화 할 수 있을 것 같다.

// 이런 여러 로직에서 공통으로 관심이 있는 것을 "공통 관심사(cross-cutting concern)"라 한다.

// 공통 관심사는 스프링의 AOP 로도 해결할 수 있지만,
// 웹과 관련된 공통 관심사는 서블릿 필터 또는 스프링 인터셉터를 사용하는 것이 좋다.
// 웹의 경우 HTTP 헤더나 URL 의 정보들이 필요한데, 서블릿 필터나 스프링 인터셉터는 HttpServletRequest 를 제공하다.

/* 서블릿 필터 */
// 필터는 서블릿이 지원하는 수문장 이다.

// 필터 흐름
// HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러
// 필터를 적용하면 필터가 호출된 다음에 서블릿이 호출된다.
// 그래서 모든 고객의 요청 로그를 남기는 요구사항이 있다면 필터를 사용하면 된다.
// 참고1: 필터는 특정 URL 패턴에 적용할 수 있다. (/* : 모든 요청에 필터 적용)
// 참고2: 스프링을 사용하는 경우, 여기서 말하는 서블릿은 스프링의 디스패치 서블릿으로 생각하자.

// 필터제한
// ・로그인 유저 : HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 컨트롤러
// ・비로그인 유저 : HTTP 요청 -> WAS -> 필터(적절하지 않은 요청으로 판단, 서블릿 호출 X)
// 필터에서 적절하지 않은 요청으로 판단하면 서블릿을 호출 않하고 끝낼 수 있다. 로그인 여부 체크하기 좋다.

// 필터체인
// HTTP 요청 -> WAS -> 필터1 -> 필터2 -> 필터3 -> 서블릿 -> 컨트롤러
// 필터는 체인으로 구성되므로 필터는 자유롭게 추가 가능하다.
// 예를들어 필터1에서 로그를 남기고, 필터2에서 로그인 여부를 체크하도록 할 수 있다.
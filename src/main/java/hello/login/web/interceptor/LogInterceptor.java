package hello.login.web.interceptor;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.UUID;

/**
 * 요청 로그 인터셉터
 */
@Slf4j
public class LogInterceptor implements HandlerInterceptor {

    public static final String LOG_ID = "logId";

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {

        String requestURI = request.getRequestURI();

        // 요청 로그 구분하기 위해 UUID 생성
        String uuid = UUID.randomUUID().toString();
        // 서블릿 필터의 경우 지역변수로 해결이 가능하지만, 스프링 인터셉터는 호출 시점이 완전히 분리되어 있다.
        // 즉 preHandle 에서 지정한 값을 postHandle, afterCompletion 에서 함께 사용하려면 어딘가에 담아둬야 한다.
        // LogInterceptor 도 싱글톤 처럼 사용되기 때문에 멤버변수를 사용하면 위험하다.
        // 따라서 request 에 담아두었다. 이 값은 afterCompletion 에서 request.getAttribute(LOG_ID) 로 찾아서 사용한다.
        request.setAttribute(LOG_ID, uuid);

        // @RequestMapping: HandleMethod
        // 정적 리소스 : ResourceHttpRequestHandler
        if (handler instanceof HandlerMethod) {
            // 호출할 컨트롤러 메소드의 모든 정보가 포함되어 있음
            HandlerMethod handlerMethod = (HandlerMethod) handler;
            /* HandlerMethod */
            // 핸들러 정보는 어떤 핸들러 매핑을 사용하는가에 따라 달라진다.
            // 스프링을 사용하면 일반적으로 @Controller, @RequestMapping 을 활용한 핸들러 매핑을 사용하는데,
            // 이 경우 핸들러 정보로 HandlerMethod 가 넘어온다.

            /* ResourceHttpRequestHandler */
            // @Controller 가 아니라 /resource/static 와 같은 정적 리소스가 호출 되는 경우
            // ResourceHttpRequestHandler 가 핸들러 정보로 넘어오기 때문에 타입에 따라서 처리가 필요하다

        }

        log.info("REQUEST [{}][{}][{}]", uuid, requestURI, handler);
        // true 정상 호출, 다음 인터셉터나 컨트롤러가 호출된다.
        return true; // false 로 설정하면 진행 x
    }

    @Override
    public void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, ModelAndView modelAndView) throws Exception {
        log.info("postHandle [{}]", modelAndView);
    }

    @Override
    public void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, Exception ex) throws Exception {
        String requestURI = request.getRequestURI();
        String logId = (String) request.getAttribute(LOG_ID);
        // 종료로그
        // postHandle 이 아니라 afterCompletion 에서 실행하는 이유는, 예외가 발생한 경우 postHandle 은 호출되지 않기 때문이다.
        log.info("RESPONSE [{}][{}]", logId, requestURI);
        if (ex != null) {
            log.error("afterCompletion error!!", ex);

        }
    }
}

/* 스프링 인터셉터 */
// 서블릿 필터와 같이 웹과 관련된 공통 관심 사항을 효과적으로 해결할 수 있는 기능이다.
// 서블릿 필터는 서블릿이 제공하는 기술이라면, 스프링 인터셉터는 스프링 MVC 가 제공하는 기술이다.
// 둘다 웹과 관련된 공통 관심 사항을 처리하지만, 적용 순서와 범위, 사용방법이 다르다.

// 스프링 인터셉터 흐름
// HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터 -> 컨트롤러
// 디스패처 서블릿과 컨트롤러 사이에서 컨트롤러 호출 직전에 호출된다.
// 스프링 MVC 가 제공하는 기능이기 때문에 결국 디스패처 서블릿 이후에 등장한다.
// 스프링 MVC 의 시작점이 디스패치 서블릿이라고 생각해보면 이해하기 편하다.
// 스프링 인터셉터에도 URL 패턴을 적용할 수 있는데, 서블릿 URL 패턴과는 다르고, 매우 정밀하게 설정 가능하다.


// 스프링 인터셉터 제한
// 로그인 사용자 : HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터 -> 컨트롤러
// 비 로그인 사용자 : HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 스프링 인터셉터 (적절하지 않은 요청 판단, 컨트롤러 호출 X)
// 인터셉터에서 적절하지 않은 요청이라고 판단하면 거기에서 끝낼수도 있다. 그래서 로그인 여부를 체크하기에 좋다.

// 스프링 인터셉터 체인
// HTTP 요청 -> WAS -> 필터 -> 서블릿 -> 인터셉터1 -> 인터셉터2 -> 컨트롤러
// 스프링 인터셉터는 체인으로 구성되는데, 중간에 인터셉터를 자유롭게 추가할 수 있다.
// 인터셉터 1에 로그를 남기고, 인터셉터2로 로그인 여부를 체크하면 된다.

// 지금까지의 내용으로 보면 서블릿 필터과 호출 되는 순서만 다르고, 제공하는 기능은 비슷해 보인다.
// 스프링 인터셉터는 서블릿 필터보다 편리하고, 다양한 기능을 제공한다.

// 스프링 인터셉터 인터페이스 : HandlerInterceptor
// 컨트롤러 호출 전
// default boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {...}
// 컨트롤러 호출 후
// default void postHandle(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable ModelAndView modelAndView) throws Exception {...}
// 요청 완료 이후
// default void afterCompletion(HttpServletRequest request, HttpServletResponse response, Object handler, @Nullable Exception ex) throws Exception {...}
// 서블릿 필터는 단순하게 doFilter() 만 제공되는 것과 비교하면 단계적으로 세분화 되어 있다.
// 서블릿 필터는 단순히 request, response 만 제공했지만,
// 인터셉터는 어떤 컨트롤러(Handler)가 호출되는지 호출 정보도 받을 수 있다.
// 또한, 어떤 ModelAndView 가 반환되는지 응답 정보도 받을 수 있다.

/* 스프링 인터셉터 호출 흐름 */
// HTTP 요청 -> DispatcherServlet -> 1.preHandle() -> 2.handle(handler) -> 핸들러 어댑터 -> 핸들러(컨트롤러)
// -> DispatcherServlet 로 3.ModelAndView 반환 -> 4.postHandle() -> 5.render(model) 호출 -> 6.afterCompletion()
// preHandle() : 컨트롤러 호출 전 (더 정확히는 핸들러 어댑터 호출 전)
//               preHandle 응답값이 true 이면 다음 진행, false 이면 끝
//               false 인 경우, 나머지 인터셉터는 물론이고, 핸들러 어댑터도 호출 x
// postHandle() : 컨트롤러 호출 후 (더 정확히는 핸들러 어댑터 호출 후)
// afterCompletion() : 뷰가 렌더링 된 이후에 호출

/* 스프링 인터셉터 Exception */
// 스프링 인터셉터 예외가 발생하면
// preHandle : 컨트롤러 호출 전에 호출된다.
// postHandle : 컨트롤러에서 예외가 발생하면 postHandle 은 호출되지 않는다.
// afterCompletion : 항상 호출. 예외를 파라미터로 받아서 어떤 예외가 발생했는지 로그로 출력가능하다.

// afterCompletion() 은 예외가 발생해도 호출되므로 예외와 무관하게 공통 처리를 할떄 사용하자.
// 예외가 발생하면 afterCompletion() 에 예외정보를 포함해서 호출된다.

// 정리하면,
// 인터셉터는 스프링 MVC 구조에 특화된 필터 기능을 제공한다고 이해하면 된다.
// 스프링 MVC 를 사용하고, 특별히 필터를 꼭 사용해야 하는 상황이 아니라면 인터셉터를 사용하는것이 편리하다.

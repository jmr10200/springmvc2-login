package hello.login.web.interceptor;

import hello.login.web.SessionConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.servlet.HandlerInterceptor;
import org.springframework.web.servlet.ModelAndView;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

@Slf4j
public class LoginCheckInterceptor implements HandlerInterceptor {

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        String requestURI = request.getRequestURI();

        log.info("인증체크 인터셉터 실행 {}", requestURI);
        HttpSession session = request.getSession(false);
        if (session == null || session.getAttribute(SessionConstant.LOGIN_MEMBER) == null) {
            log.info("미인증 사용자 요청");
            // 로그인 화면 redirect
            response.sendRedirect("/login?redirectURL=" + requestURI);
            return false;
        }
        return true;
    }

    // 서블릿 필터와 다른점은, 인터셉터의 메소드들은 default 이므로 모두 Override 하지 않아도 된다.
    // 즉, 컨트롤러 호출 전에만, preHandle() 만 구현하면 된다.
}

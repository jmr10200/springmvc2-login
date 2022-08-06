package hello.login.web.filter;

import hello.login.web.SessionConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.util.PatternMatchUtils;
import org.thymeleaf.util.PatternUtils;

import javax.servlet.*;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;

@Slf4j
public class LoginCheckFilter implements Filter {

    // 인증 필터를 적용해도 홈, 회원가입, 로그인 화면, css 등과 같은 리소스에는 접근할 수 있어야 한다.
    // 화이트 리스트 경로는 인증과 무관하게 항상 허용한다.
    // 화이트 리소스를 제외한 나머지 모든 경로에는 인증체크 로직을 적용한다.
    private static final String[] whiteList = {"/", "/members/add", "/login", "/logout", "/css/*"};

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        // ServletRequest 다운 캐스팅
        HttpServletRequest httpServletRequest = (HttpServletRequest) request;
        String requestURI = httpServletRequest.getRequestURI();

        // ServletResponse 다운 캐스팅
        HttpServletResponse httpServletResponse = (HttpServletResponse) response;

        try {
            log.info("인증체크 필터 시작 {}", requestURI);

            // 화이트 리스트가 아니면 인증체크 로직을 실행
            if (isLoginCheckPath(requestURI)){
                log.info("인증체크 로직 실행 {}", requestURI);
                HttpSession session = httpServletRequest.getSession(false);

                if (session == null || session.getAttribute(SessionConstant.LOGIN_MEMBER) == null ){
                    log.info("미인증 유저 요청 {}", requestURI);
                    // 미인증 유저는 로그인으로 redirect
                    // 로그인 이후 다시 홈으로 이동하면 원하는 경로를 다시 찾아가야 하는 번거로움이 있다.
                    // 예를들어 상품관리화면을 보려고 들어갔다가 로그인화면이 뜨면, 로그인 이후 상품관리화면을 표시하는게 좋다.
                    // 이러한 기능을 위해 현재 요청한 경로인 requestURI 를 /login 에 쿼리 파라미터로 함께 전달한다.
                    // 또 /login 컨트롤러에서 로그인 성공시 해당 경로로 이동하는 기능을 추가해야한다.
                    httpServletResponse.sendRedirect("/login?redirectURL=" + requestURI);

                    // 중요 포인트 : 미인증 유저는 다음으로 진행하지않고 리턴
                    // redirect 했으므로 redirect 가 응답으로 적용되고 요청은 끝난다.
                    return;
                }
            }

            chain.doFilter(request, response);

        } catch (Exception e) {
            // Exception 로깅 가능하지만, 톰캣까지 Exception 을 보내줘야 함
            throw e;
        } finally {
            log.info("인증체크 필터 종료 {}", requestURI);
        }

    }

    /**
     * 화이트 리스트인 경우 인증 체크 X
     */
    private boolean isLoginCheckPath(String requestURI) {
        return !PatternMatchUtils.simpleMatch(whiteList, requestURI);
    }

}

/* 서블릿 필터 - 인증 체크 */
// 미로그인 유저는 상품 관리 페이지 등에는 접속하지 못하도록 한다.

// 서블릿 필터를 이렇게 사용하면 로그인 하지 않은 유저는 지정 경로 외에는 접근하지 못하게 된다.
// 공통 관심사를 서블릿 필터를 사용해서 해결한 덕분에 향후 로그인 관련 정책이 변경되더라도 이 부분만 변경하면 된다.

// 참고
// 필터에는 스프링 인터셉터에서는 제공하지 않는 아주 강력한 기능이 있다.
// chain.doFilter(request, response); 를 호출해서 다음 필터 또는 서블릿을 호출할 때,
// request, response 를 다른 책체로 바꿀 수 있다.
// ServletRequest, ServletResponse 를 구현한 다른 객체를 만들어서 넘기면 해당 객체가 다음 필터 또는 서블릿에서 사용된다.
// 잘 사용하는 기능은 아니므로 참고해두자!
package hello.login.web.login;

import hello.login.domain.login.LoginService;
import hello.login.domain.member.Member;
import hello.login.web.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;
    private final SessionManager sessionManager;

    @GetMapping("/login")
    public String loginForm(@ModelAttribute("loginForm") LoginForm loginForm) {
        return "login/loginForm";
    }

//    @PostMapping("/login")
    public String login(@Valid @ModelAttribute LoginForm loginForm, BindingResult bindingResult, HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            log.error("login validation error");
            return "login/loginFrom";
        }

        Member loginMember = loginService.login(loginForm.getLoginId(), loginForm.getPassword());
        log.info("login? {}", loginMember);

        if (loginMember == null) {
            // 로그인 실패시, reject() 로 객체에러 (ObjectError) 생성
            bindingResult.reject("loginFail", "ID 또는 패스워드가 맞지 않습니다.");
            // 로그인입력화면 표시
            return "login/loginForm";
        }

        // 로그인 성공처리
        // 세션 쿠키 : 쿠키에 시간 정보를 생략, 브라우저 종료시 모두 종료
        // 쿠키 이름은 memberId, 값은 id
        Cookie idCookie = new Cookie("memberId", String.valueOf(loginMember.getId()));
        // 생성된 쿠키를 HttpServletResponse 에 설정
        response.addCookie(idCookie); // 브라우저 개발자모드에서 확인가능

        return "redirect:/";
    }

    @PostMapping("/login")
    public String loginV2(@Valid @ModelAttribute LoginForm loginForm, BindingResult bindingResult, HttpServletResponse response) {

        if (bindingResult.hasErrors()) {
            log.error("login validation error");
            return "login/loginFrom";
        }

        Member loginMember = loginService.login(loginForm.getLoginId(), loginForm.getPassword());
        log.info("login? {}", loginMember);

        if (loginMember == null) {
            // 로그인 실패시, reject() 로 객체에러 (ObjectError) 생성
            bindingResult.reject("loginFail", "ID 또는 패스워드가 맞지 않습니다.");
            // 로그인입력화면 표시
            return "login/loginForm";
        }

        // 로그인 성공처리
        // 세션 관리자 이용해서 세션 생성하고 , 유저 데이터 보관
        sessionManager.createSession(loginMember, response);

        return "redirect:/";
    }

//    @PostMapping("/logout")
    public String logout(HttpServletResponse response) {
        // 세션쿠키 이므로 웹 브라우저 종료시
        expireCookie(response, "memberId");
        return "redirect:/";
    }

    @PostMapping("/logout")
    public String logoutV2(HttpServletRequest request) {
        // 세션 관리자 이용해서 세션 종료
        sessionManager.expire(request);
        return "redirect:/";
    }

    private void expireCookie(HttpServletResponse response, String cookieName) {
        Cookie cookie = new Cookie(cookieName, null);
        // 서버에서 해당 쿠키의 종료날짜를 0으로 지정
        cookie.setMaxAge(0);
        response.addCookie(cookie);
    }
}

// 요구사항 : 로그인이 되면, 홈 화면에 유저 이름이 표시되어야 한다.

/* 쿠키 사용 */
// 로그인 상태 유지하기
// 서버에서 로그인에 성공하면 HTTP 응답에 쿠키를 담아 브라우저에 전달하면 브라우저는 해당 쿠키를 지속해서 보내준다.

// 영속 쿠키 : 만료 날짜를 입력하면 해당 날짜까지 유지
// 세션 쿠키 : 만료 날짜를 입력하지 않으면 브라우저 종료시까지 유지

/* 쿠키와 보안 문제 */
// 쿠키를 사용해서 로그인 ID 를 전달하여 로그인을 유지할 수 있지만 심각한 보안 문제가 발생한다.
// 1. 쿠키 값은 임의로 변경이 가능하다
// ・클라이언트가 쿠키를 강제로 변경하면 다른 사용자가 된다.
// ・웹 브라우저 개발자모드 -> Application -> Cookie 변경 으로 확인 가능
// 2. 쿠키에 보관된 정보는 훔쳐갈 수 있다.
// ・만약 쿠키에 개인정보나 카드정보 등이 담겨있다면? 웹 브라우저에도 보관되고, 네트워크 요청시마다 클라이언트에서 서버로 전달된다.
// ・쿠키의 정보가 로컬 PC 에서 도난당할 수 있고, 네트워크 전송 구간에서 도난당할 수 있다.
// 3. 해커가 쿠키를 한번 훔쳐가면 평생 사용 할 수 있다.
// ・즉, 해커가 쿠키를 이용해 악의적인 요청을 계속 시도할 수 있다.

// 대안
// 쿠키에 중요한 값을 노출하지 않는다.
// 유저별로 예측 불가능한 임의의 토큰(UUID)을 노출하고, 서버에서 토큰과 ID 를 매핑해서 처리하며, 서버에서 토큰을 관리한다.
// 토큰이 해킹당해도 일정 시간이 지나면 사용할 수 없도록 서버에서 토큰의 만료시간을 짧게(예: 30분) 유지한다.
// 해킹이 의심되면 서버에서 해당 토큰을 강제로 제거하도록 한다.
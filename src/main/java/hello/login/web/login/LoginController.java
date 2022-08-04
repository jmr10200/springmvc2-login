package hello.login.web.login;

import hello.login.domain.login.LoginService;
import hello.login.domain.member.Member;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletResponse;
import javax.validation.Valid;

@Slf4j
@Controller
@RequiredArgsConstructor
public class LoginController {

    private final LoginService loginService;

    @GetMapping("/login")
    public String loginForm(@ModelAttribute("loginForm") LoginForm loginForm) {
        return "login/loginForm";
    }

    @PostMapping("/login")
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
}

// 요구사항 : 로그인이 되면, 홈 화면에 유저 이름이 표시되어야 한다.

/* 쿠키 사용 */
// 로그인 상태 유지하기
// 서버에서 로그인에 성공하면 HTTP 응답에 쿠키를 담아 브라우저에 전달하면 브라우저는 해당 쿠키를 지속해서 보내준다.

// 영속 쿠키 : 만료 날짜를 입력하면 해당 날짜까지 유지
// 세션 쿠키 : 만료 날짜를 입력하지 않으면 브라우저 종료시까지 유지
package hello.login.web;

import hello.login.domain.member.Member;
import hello.login.domain.member.MemberRepository;
import hello.login.web.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberRepository memberRepository;
    private final SessionManager sessionManager;

    //    @GetMapping("/")
    public String home() {
        return "home";
    }

//    @GetMapping("/")
    public String homeLogin(@CookieValue(name = "memberId", required = false) Long memberId, Model model) {
        // @CookieValue : 쿠키 조회 가능
        // 로그인 사용자도 홈에 접근할 수 있기 때문에 required = false 설정

        if (memberId == null) {
            // 로그인 쿠키가 없으면 기존 홈 화면 표시
            return "home";
        }

        // 로그인
        Member loginMember = memberRepository.findById(memberId);
        if (loginMember == null) {
            // 쿠키가 있어도 회원이 없으면 기존 홈 화면 표시
            return "home";
        }

        // 쿠키가 있으면 로그인 사용자 홈화면 loginHome.html 표시
        // 로그인 정보를 화면에 표시하기 위해 model 에 member 데이터를 추가한다.
        model.addAttribute("member", loginMember);
        return "loginHome";
    }

//    @GetMapping("/")
    public String homeLoginV2(HttpServletRequest request, Model model) {

        // 세션 관리자에 의해 저장된 회원 정보 조회
        Member member = (Member) sessionManager.getSession(request);
        // 정보 확인
        if (member == null) {
            // 정보가 없으면 쿠키나 세션이 없는 것이므로 로그인 되지 않은 것
            return "home";
        }

        // 로그인 정보를 화면에 표시하기 위해 model 에 member 데이터를 추가한다.
        model.addAttribute("member", member);
        return "loginHome";
        // 세션을 이해하기위해 세션 만들었지만, 단지 쿠키를 이용하는, 서버에서 데이터를 유지하는 방법일 뿐이다.
        // 이렇게 매번 세션정보를 만드는 것은 번거롭기 때문에 서블릿이 이를 지원한다.
        // 서블릿은 추가로 일정시간 사용하지않으면 해당 세션을 삭제하는 기능도 제공한다.
    }

    @GetMapping("/")
    public String homeLoginV3(HttpServletRequest request, Model model) {

        // 세션이 없으면 home
        HttpSession session = request.getSession(false);
        // getSession(true) 이면, 로그인 하지 않는 사용자도 의미없이 세션이 생성되어 버리므로
        // 세션을 사용하는 시점에서는 false 옵션을 사용하여 생성되지 않도록 한다.
        if (session == null) {
            return "home";
        }

        // 세션에 저장된 회원 정보 조회
        Member loginMember = (Member) session.getAttribute(SessionConstant.LOGIN_MEMBER);

        // 회원 없으면 home
        if (loginMember == null) {
            return "home";
        }

        // 세션이 유지되면 로그인으로 이동
        // 로그인 정보를 화면에 표시하기 위해 model 에 member 데이터를 추가한다.
        model.addAttribute("member", loginMember);
        return "loginHome";
    }
}

/* 도메인이 가장 중요하다 */
// 도메인
//  : 시스템이 구현해야하는 핵심 비즈니스 업무 영역
//  : 화면, UI, 기술 인프라 등의 영역은 제외
//  향후 web 을 다른 기술로 바꿔도 도메인은 그대로 유지할 수 있어야 한다.
//  -> web 은 domain 을 의존하지만, domain 은 web 을 의존하지 않아야 한다.
//  (=> domain 은 web 을 참조하면 안된다.)
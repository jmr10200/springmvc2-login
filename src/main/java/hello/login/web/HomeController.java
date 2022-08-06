package hello.login.web;

import hello.login.domain.member.Member;
import hello.login.domain.member.MemberRepository;
import hello.login.web.argumentresolver.Login;
import hello.login.web.session.SessionManager;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.SessionAttribute;

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

//    @GetMapping("/")
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

//    @GetMapping("/")
    public String homeLoginV3Spring(@SessionAttribute(name = SessionConstant.LOGIN_MEMBER, required = false) Member loginMember, Model model) {

        // 스프링은 세션을 더 편리하게 사용할 수 있도록 @SessionAttribute 를 제공한다.
        // 이미 로그인 된 사용자를 찾을 때는 다음과 같이 사용하면 된다. 이 기능은 세션을 생성하지 않는다.
        // @SessionAttribute(name = SessionConstant.LOGIN_MEMBER, required = false) Member loginMember
        // 세션을 찾고, 세션에 들어있는 데이터를 찾는 번거로운 과정을 스프링이 한번에 편리하게 처리해준다.

        // 세션이 없으면 home
        if (loginMember == null) {
            return "home";
        }

        // 세션이 유지되면 로그인으로 이동
        // 로그인 정보를 화면에 표시하기 위해 model 에 member 데이터를 추가한다.
        model.addAttribute("member", loginMember);
        return "loginHome";
    }

    @GetMapping("/")
    public String homeLoginV3ArgumentResolver(@Login Member loginMember, Model model) {

        // ArgumentResolver 활용 : @Login 어노테이션을 생성

        // 세션에 없으면 home 화면
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

/* TrackingModes */
// 처음 로그인을 시도하면 URL 이 다음과 같이 jsessionid 를 포함하고 있다.
// http://localhost:8080/;jsessionid=F59911518B921DF62D09F0DF8F83F872
// 이는 브라우저가 쿠키를 지원하지 않을 때 쿠키 대신 URL 을 통해서 세션을 유지하는 방법이다.
// 이 방법을 사용하려면 URL 에 이 값을 계속 포함해서 전달해야 한다.
// 타임리프 같은 템플릿은 엔진을 통해 링크를 걸면 jsessionid 를 URL 에 자동 포함해준다.
// 서버는 브라우저의 쿠키 지원여부를 최초에 판단할 수 없으므로, 쿠키 값도 전달하고 URL 로도 전달한다.
// URL 전달 방식을 끄고 항상 쿠키를 통해서만 세션을 유지하고 싶다면 다음을 추가하면 된다.
// application.properties : server.servlet.session.tracking-modes=cookie

/* ArgumentResolver 활용 */
// 어노테이션 기반 컨트롤러를 처리하는 RequestMappingHandlerAdaptor 는 이 ArgumentResolver 를 호출해서
// 컨트롤러(핸들러)가 필요로하는 다양한 파라미터의 값(객체)을 생성한다. 파라미터 값이 모두 준비되면 컨트롤러가 호출하면서 값을 넘겨준다.
//
package hello.login.web;

import hello.login.domain.member.Member;
import hello.login.domain.member.MemberRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.CookieValue;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
@RequiredArgsConstructor
public class HomeController {

    private final MemberRepository memberRepository;

    //    @GetMapping("/")
    public String home() {
        return "home";
    }

    @GetMapping("/")
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
        return "loginHome.html";
    }
}

/* 도메인이 가장 중요하다 */
// 도메인
//  : 시스템이 구현해야하는 핵심 비즈니스 업무 영역
//  : 화면, UI, 기술 인프라 등의 영역은 제외
//  향후 web 을 다른 기술로 바꿔도 도메인은 그대로 유지할 수 있어야 한다.
//  -> web 은 domain 을 의존하지만, domain 은 web 을 의존하지 않아야 한다.
//  (=> domain 은 web 을 참조하면 안된다.)
package hello.login.web;

import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Controller;
import org.springframework.web.bind.annotation.GetMapping;

@Slf4j
@Controller
public class HomeController {

    @GetMapping("/")
    public String home() {
        return "home";
    }
}

/* 도메인이 가장 중요하다 */
// 도메인
//  : 시스템이 구현해야하는 핵심 비즈니스 업무 영역
//  : 화면, UI, 기술 인프라 등의 영역은 제외
//  향후 web 을 다른 기술로 바꿔도 도메인은 그대로 유지할 수 있어야 한다.
//  -> web 은 domain 을 의존하지만, domain 은 web 을 의존하지 않아야 한다.
//  (=> domain 은 web 을 참조하면 안된다.)
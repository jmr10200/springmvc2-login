package hello.login.web.session;

import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.util.Date;

@Slf4j
@RestController
public class SessionInfoController {

    @GetMapping("/session-info")
    public String sessionInfo(HttpServletRequest request) {
        HttpSession session = request.getSession(false);
        if (session == null) {
            return "세션이 없습니다";
        }

        // 세션 데이터 출력
        session.getAttributeNames().asIterator()
                .forEachRemaining(name -> log.info("session name={}, value={}", name, session.getAttribute(name)));

        // sessionId : 세션 ID, JSESSIONID 의 값
        log.info("sessionId={}", session.getId());
        // manInactiveInterval : 세션의 유효 시간, 예) 1800초 (30분)
        log.info("maxInactiveInterval={}", session.getMaxInactiveInterval());
        // creationTime : 세션 생성 일시
        log.info("creationTime={}", new Date(session.getCreationTime()));
        // lastAccessedTime : 사용자가 최근에 서버에 접근한 시간. 클라이언트에서 서버로 sessionId 를 요청한 경우 갱신됨
        log.info("lastAccessedTime={}", new Date(session.getLastAccessedTime()));
        // isNew : 새로 생성된 세션인지, 기존에 존재했던 세션인지 여부
        log.info("isNew={}", session.isNew());

        return "세션 출력";
    }
}
/* 세션 타임아웃 설정 */
// 세션은 유저가 로그아웃을 해서 session.invalidate() 가 호출 되는 시점에 삭제된다.
// 그러나, 대부분의 유저는 로그아웃을 하지 않고 브라우저를 종료한다.
// 문제는 HTTP 가 비연결성(ConnectionLess) 이므로 서버는 유저가 브라우저를 종료했는지를 알 수 없다.
// 따라서 서버에서 세션 데이터를 언제 삭제해야 하는지 판단하기 어렵다.

// 이 경우 남아있는 세션을 무한정 보관하면 문제가 발생할 수 있다.
// 1. 세션과 관련된 쿠키(JSESSIONID) 를 도난 당했을 경우, 시간이 지나도 해당 쿠키로 악의적인 요청을 할 수 있다.
// 2. 세션은 기본적으로 메모리에 생성된다. 그래서 꼭 필요한 경우만 생성해서 사용해야한다.

// 세션의 종료 시점
// 사용자가 서버에 최근에 요청한 시간을 기준으로 30분 정도를 유지해주는 것이 좋다.
// 사용자가 서비스를 사용하고 있으면, 세션 시간이 30분으로 계속 늘어나게 된다. HttpSession 은 이 방식을 사용한다.

// 스프링부트의 글로벌 설정 (분단위로 설정해야 한다)
// application.properties : session.setMaxInactiveInterval(1800); // default 1800초 (30분)

// 세션 타임아웃 발생
// 세션의 타임아웃 시간은 해당 세션과 관련된 JSESSIONID 를 전달하는 HTTP 요청이 있으면 현재 시간으로 다시 초기화된다.
// session.getLastAccessedTime() : 최근 세션 접근 시간
// LastAccessedTime 이후로 timeout 시간이 지나면, WAS 가 내부에서 해당 세션을 제거한다.

// 정리
// 서블릿의 HttpSession 이 제공하는 타임아웃 기능으로 세션을 안전하고 편리하게 사용할 수 있다.
// 실무상 주의할 점은 세션에는 최소한의 데이터만 보관해야 한다는 점이다.
// 세션은 메모리에 생성되므로, 보관한 데이터 용량 * 유저수 로 세션의 메모리 사용량이 급격하게 늘어나면 장애로 이어질 수 있다.
// 추가로 세션 시간을 너무 길게 가져가면 메모리 사용량이 계속 누적 될 수 있으므로 적당한 시간 설정도 중요하다. (기본 30분)
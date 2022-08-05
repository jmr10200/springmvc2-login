package hello.login.web.session;

import org.springframework.stereotype.Component;

import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.Arrays;
import java.util.Map;
import java.util.UUID;
import java.util.concurrent.ConcurrentHashMap;

/**
 * 세션 관리
 */
@Component
public class SessionManager {

    public static final String SESSION_COOKIE_NAME = "mySessionId";

    // ConcurrentHashMap : HashMap 은 동시 요청에 안전하지 않으므로 ConcurrentHashMap 사용
    private Map<String, Object> sessionStore = new ConcurrentHashMap<>();

    /**
     * 세션 생성
     */
    public void createSession(Object value, HttpServletResponse response) {
        // sessionId 생성, 값을 세션에 저장
        String sessionId = UUID.randomUUID().toString();
        sessionStore.put(sessionId, value);

        // 쿠키 생성
        Cookie mySessionCookie = new Cookie(SESSION_COOKIE_NAME, sessionId);
        response.addCookie(mySessionCookie);
    }

    /**
     * 세션 조회
     */
    public Object getSession(HttpServletRequest request) {
        Cookie sessionCookie = findCookie(request, SESSION_COOKIE_NAME);
        if (sessionCookie == null) {
            return null;
        }
        return sessionStore.get(sessionCookie.getValue());
    }

    /**
     * 세션 만료
     */
    public void expire(HttpServletRequest request) {
        Cookie sessionCookie = findCookie(request, SESSION_COOKIE_NAME);
        if (sessionCookie != null) {
            sessionStore.remove(sessionCookie.getValue());
        }
    }

    private Cookie findCookie(HttpServletRequest request, String cookieName) {
        if (request.getCookies() == null) {
            return null;
        }
        return Arrays.stream(request.getCookies()).filter(cookie -> cookie.getName().equals(cookieName))
                .findAny().orElse(null);
    }

}

// 로그인 처리 - 세션 동작방식
// 쿠키에 중요한 정보를 보관하는 방법은 여러가지 보안 이슈가 발생한다.
// 이 문제를 해결하려면 중요한 정보는 모두 서버에 저장해야 한다.
// 그리고 클라이언트와 서버는 추정 불가능한 임의의 식별자 값으로 연결해야 한다.
// 이렇게 서버에 중요한 정보를 보관하고 연결을 유지하는 방법을 세션이라 한다.

// 세션 동작방식
// 유저가 userId, password 정보를 전달하면 서버에서 해당 유저가 맞는지 확인한다.
// 추정이 불가능한 세션 ID (UUID) 를 생성하고, 생성된 ID 와 세션에 보관할 값(memberA) 을 서버의 세션 저장소에 보관한다.
// 서버는 클라이언트에 mySessionId 라는 이름으로 세션 ID 만 담아서 응답 쿠키로 전달한다.

// 즉, 유저와 관련된 정보는 클라이언트에 전달하지 않고, 오직 추정불가능한 세션 ID 만 쿠키를 통해 클라이언트에 전달한다.

// 클라이언트는 요청시 항상 mySessionId 쿠키를 전달하고,
// 서버는 클라이언트가 전달한 mySessionId 쿠키정보로 세션 저장소를 조회한다.

/* 세션을 이용한 보안문제 해결 */
// 1. 추정 불가능한 세션 ID 를 사용 : 쿠키 값 변조를 방지함
// 2. 세션 ID 가 해킹당해도 중요한 정보가 없다 : 쿠키에 보관된 값은 털릴 가능성이 있다.
// 3. 쿠키 탈취후 사용 방지 : 토큰을 해킹당해도 시간이 지나면 사용할 수 없도록 서버에서 세션 만료시간을(예:30분) 짧게 유지한다.
// 또는, 해킹이 의심되는 경우 서버에서 해당 세션을 강제로 제거하면 된다.

/* 세션 관리 */
// 세션 생성 : 추정 불가능한 랜덤한 값의 sessionId 생성, 세션저장소에 세션 ID 와 보관할 값 저장, 세션 ID 로 응답 쿠키 생성하여 클라이언트에 전달
// 세션 조회 : 클라이언트가 요청한 sessionId 쿠키 값으로, 세션 저장소에 보관한 값 조회
// 세션 만료 : 클라이언트가 요청한 sessionId 쿠키 값으로, 세션 저장소에 보관한 sessionId 와 값 제거
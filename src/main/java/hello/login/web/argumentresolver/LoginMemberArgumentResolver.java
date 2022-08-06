package hello.login.web.argumentresolver;

import hello.login.domain.member.Member;
import hello.login.web.SessionConstant;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.MethodParameter;
import org.springframework.web.bind.support.WebDataBinderFactory;
import org.springframework.web.context.request.NativeWebRequest;
import org.springframework.web.method.support.HandlerMethodArgumentResolver;
import org.springframework.web.method.support.ModelAndViewContainer;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

@Slf4j
public class LoginMemberArgumentResolver implements HandlerMethodArgumentResolver {

    @Override
    public boolean supportsParameter(MethodParameter parameter) {
        log.info("supportsParameter 실행");

        boolean hasLoginAnnotation = parameter.hasParameterAnnotation(Login.class);
        boolean hasMemberType = Member.class.isAssignableFrom(parameter.getParameterType());
        // @Login 어노테이션이 있으면서 Member 타입이면 해당 ArgumentResolver 가 사용됨
        return hasLoginAnnotation && hasMemberType;
    }

    @Override
    public Object resolveArgument(MethodParameter parameter, ModelAndViewContainer mavContainer, NativeWebRequest webRequest, WebDataBinderFactory binderFactory) throws Exception {
        // 컨트롤러 호출 직전에 호출되어서 필요한 파라미터 정보를 생성해줌
        // 세션에 있는 로그인 회원 정보인 member 객체를 찾아서 반환해줌
        // 이후 스프링 MVC 는 컨트롤러 메소드를 호출하면서 여기서 반환된 member 객체를 파라미터에 전달해줌
        log.info("resolverArgument 실행");

        HttpServletRequest request = (HttpServletRequest) webRequest.getNativeRequest();
        HttpSession session = request.getSession(false);

        if (session == null) {
            return null;
        }

        return session.getAttribute(SessionConstant.LOGIN_MEMBER);
    }
}

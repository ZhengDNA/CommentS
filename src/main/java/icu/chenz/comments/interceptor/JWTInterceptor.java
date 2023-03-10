package icu.chenz.comments.interceptor;

import icu.chenz.comments.utils.R;
import icu.chenz.comments.utils.annotation.NoPermission;
import icu.chenz.comments.utils.annotation.OptionalToken;
import icu.chenz.comments.utils.cryption.JWTEncryption;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.http.HttpStatus;
import org.springframework.web.method.HandlerMethod;
import org.springframework.web.servlet.HandlerInterceptor;

import java.io.PrintWriter;

/**
 * @author : Chenz
 * @date : 2023-01-11 20:12
 */

public class JWTInterceptor implements HandlerInterceptor {

    public JWTInterceptor(String secret, Long expireTime) {
        JWTEncryption.initAlgorithm(secret, expireTime);
    }

    @Override
    public boolean preHandle(HttpServletRequest request, HttpServletResponse response, Object handler) throws Exception {
        if (handler instanceof HandlerMethod method) {
            response.setHeader("Content-Type", "application/json;charset=utf8");
            if (method.hasMethodAnnotation(NoPermission.class)) {
                return true;
            }
            boolean optionalToken = method.hasMethodAnnotation(OptionalToken.class);
            if (request.getMethod().equals("GET") && !optionalToken) {
                return true;
            }
            String token = request.getHeader("Authorization");
            if (token == null) {
                if (optionalToken) {
                    return true;
                }
                PrintWriter writer = response.getWriter();
                writer.write(R.fail(HttpStatus.UNAUTHORIZED, "未登录").toString());
                writer.close();
                return false;
            } else {
                request.setAttribute("user", JWTEncryption.verifyToken(token));
            }
        }
        return true;
    }
}

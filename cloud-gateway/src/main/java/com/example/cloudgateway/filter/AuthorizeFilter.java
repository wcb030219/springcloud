package com.example.cloudgateway.filter;

import io.jsonwebtoken.Claims;
import org.example.common.JwtUtil;
import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.core.Ordered;
import org.springframework.http.HttpStatus;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

@Component
public class AuthorizeFilter implements GlobalFilter, Ordered {
    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        ServerHttpRequest request = exchange.getRequest();
        String path = request.getURI().getPath();

        if (path.startsWith("/user/") && path.contains("/v1/login")) {
            return chain.filter(exchange);
        }

        String token = resolveBearerToken(request);
        if (token == null) return unauthorized(exchange);

        Claims claims;
        try {
            claims = JwtUtil.parseClaims(token);
        } catch (Exception e) {
            return unauthorized(exchange);
        }

        String role = JwtUtil.getRole(claims);
        Long userId = JwtUtil.getUserId(claims);
        if (role == null || role.isEmpty() || userId == null) return unauthorized(exchange);

        // 基础权限校验示例
        // 1. 课程服务校验 (以 /course/ 开头)
        if (path.startsWith("/course/")) {
            // 如果是 admin 路径，校验管理员角色 (1)
            if (path.contains("/admin/") && !"1".equals(role)) {
                return forbidden(exchange);
            }
            if (path.contains("/my/") && !"3".equals(role)) {
                return forbidden(exchange);
            }
            if (path.contains("/teacher/") && !"2".equals(role)) {
                return forbidden(exchange);
            }
            // 如果是 select 路径，校验学生角色 (3)
            if (path.contains("/select") && !"3".equals(role)) {
                return forbidden(exchange);
            }
            // 如果是 drop 路径，校验学生角色 (3)
            if (path.contains("/drop") && !"3".equals(role)) {
                return forbidden(exchange);
            }
        } 
        // 2. 评估服务校验 (以 /evaluation/ 开头)
        else if (path.startsWith("/evaluation/")) {
            if (path.contains("/admin/")) {
                if (!"1".equals(role)) return forbidden(exchange);
                return chain.filter(withUserHeaders(exchange, request, userId, role));
            }
            // 如果是学生 (3)
            if ("3".equals(role)) {
                // 学生允许访问：查询成绩、获取评估题目、提交评估
                if (path.contains("/grade/my") || 
                    path.contains("/evaluation/templates") ||
                    path.contains("/evaluation/questions") || 
                    path.contains("/evaluation/submit") ||
                    path.contains("/evaluation/submitAnswers")) {
                    return chain.filter(withUserHeaders(exchange, request, userId, role));
                }
                // 其他接口禁止访问
                return forbidden(exchange);
            }
            
            // 只有教师 (2) 可以访问评估相关功能（如录入成绩）
            if (!"2".equals(role)) return forbidden(exchange);
        }
        // 3. 用户服务校验 (以 /user/ 开头)
        else if (path.startsWith("/user/")) {
            // 已在开头放行 login，这里只要求 token 有效即可
            if (path.contains("/admin/") && !"1".equals(role)) {
                return forbidden(exchange);
            }
        }

        return chain.filter(withUserHeaders(exchange, request, userId, role));
    }

    private Mono<Void> unauthorized(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.UNAUTHORIZED);
        return exchange.getResponse().setComplete();
    }

    private Mono<Void> forbidden(ServerWebExchange exchange) {
        exchange.getResponse().setStatusCode(HttpStatus.FORBIDDEN);
        return exchange.getResponse().setComplete();
    }

    @Override
    public int getOrder() {
        return -1;
    }

    private String resolveBearerToken(ServerHttpRequest request) {
        String auth = request.getHeaders().getFirst("Authorization");
        if (auth == null) return null;
        String v = auth.trim();
        if (v.length() < 8) return null;
        if (v.regionMatches(true, 0, "Bearer ", 0, 7)) {
            String token = v.substring(7).trim();
            return token.isEmpty() ? null : token;
        }
        return null;
    }

    private ServerWebExchange withUserHeaders(ServerWebExchange exchange, ServerHttpRequest request, Long userId, String role) {
        ServerHttpRequest mutated = request.mutate()
                .header("X-User-Id", String.valueOf(userId))
                .header("X-Role", role)
                .build();
        return exchange.mutate().request(mutated).build();
    }
}

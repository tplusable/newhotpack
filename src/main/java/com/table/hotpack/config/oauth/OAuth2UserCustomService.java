package com.table.hotpack.config.oauth;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.table.hotpack.domain.User;
import com.table.hotpack.repository.UserRepository;
import lombok.RequiredArgsConstructor;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.user.DefaultOAuth2User;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import java.util.Map;
import java.util.Optional;
import java.util.Set;

@RequiredArgsConstructor
@Service
public class OAuth2UserCustomService extends DefaultOAuth2UserService {

    private final UserRepository userRepository;

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        // OAuth2 사용자 정보 가져오기
        OAuth2User oAuth2User = super.loadUser(userRequest);
        // 클라이언트 ID를 기준으로 Provider 구분
        String provider = userRequest.getClientRegistration().getRegistrationId();
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // 사용자 저장 또는 업데이트
        User user = saveOrUpdate(oAuth2User, provider);

        if ("naver".equals(provider)) {
            Object response = attributes.get("response");
            if (response == null) {
                throw new IllegalArgumentException("'response' 필드가 없습니다.");
            }
            return new DefaultOAuth2User(
                    user.getAuthorities(),
                    (Map<String, Object>) response,
                    "email" // 주 키 설정 (OAuth2 제공자에 따라 변경 가능)
            );
        }

        // 사용자 정보를 기반으로 DefaultOAuth2User 반환
        return new DefaultOAuth2User(
                user.getAuthorities(),
                oAuth2User.getAttributes(),
                "email" // 주 키 설정 (OAuth2 제공자에 따라 변경 가능)
        );
    }

    private User saveOrUpdate(OAuth2User oAuth2User, String provider) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        // google과 naver의 사용자 정보 매핑
        String email = getEmail(attributes, provider);
        String name = getName(attributes, provider);

        Optional<User> existingUser = userRepository.findByEmail(email);

        User user;
        if (existingUser.isPresent()) {
            // 기존 사용자 이름 업데이트
            user = existingUser.get();
            if (!user.getName().equals(name)) {
                user.setName(name);
            }
        } else {
            // 새 사용자 생성
            user = User.builder()
                    .email(email)
                    .name(name)
                    .roles(Set.of("ROLE_USER")) // 기본 역할
                    .build();
        }
        return userRepository.save(user);
    }

    private String getEmail(Map<String, Object> attributes, String provider) {
        if ("google".equals(provider)) {
            // Google의 경우 최상위 레벨에서 email 추출
            return (String) attributes.get("email");
        } else if ("naver".equals(provider)) {
            // Naver의 경우 response 내부에서 email 추출
            ObjectMapper objectMapper = new ObjectMapper();
            Object responseObj = attributes.get("response");

            if (responseObj == null) {
                throw new IllegalArgumentException("Naver 응답에 'response' 필드가 없습니다.");
            }

            Map<String, Object> response = objectMapper.convertValue(
                    responseObj,
                    new TypeReference<Map<String, Object>>() {}
            );
            return (String) response.get("email");
        }
        throw new IllegalArgumentException("지원하지 않는 Provider입니다");
    }


    private String getName(Map<String, Object> attributes, String provider) {
        if ("google".equals(provider)) {
            return (String) attributes.get("name");
        } else if ("naver".equals(provider)) {
            ObjectMapper objectMapper = new ObjectMapper();
            Map<String, Object> response = objectMapper.convertValue(attributes.get("response"), new TypeReference<Map<String, Object>>() {});
            return (String) response.get("name");
        }
        throw new IllegalArgumentException("지원하지 않는 Provider입니다.");
    }
}
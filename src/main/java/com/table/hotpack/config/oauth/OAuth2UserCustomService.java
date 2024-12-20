package com.table.hotpack.config.oauth;

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

        // 사용자 저장 또는 업데이트
        User user = saveOrUpdate(oAuth2User, userRequest.getClientRegistration().getRegistrationId());

        // 사용자 정보를 기반으로 DefaultOAuth2User 반환
        return new DefaultOAuth2User(
                user.getAuthorities(),
                oAuth2User.getAttributes(),
                "email" // 주 키 설정 (OAuth2 제공자에 따라 변경 가능)
        );
    }

    private User saveOrUpdate(OAuth2User oAuth2User, String provider) {
        Map<String, Object> attributes = oAuth2User.getAttributes();

        String email = (String) attributes.get("email");
        String name = (String) attributes.get("name");

        Optional<User> existingUser = userRepository.findByEmail(email);

        // 기존 사용자 업데이트 또는 새 사용자 생성
        User user = existingUser.orElseGet(() -> User.builder()
                .email(email)
                .name(name)
                .nickname("")
                .password("") // OAuth2 로그인은 비밀번호 필요 없음
                .roles(Set.of("ROLE_USER")) // 기본 역할
                .build());

        return userRepository.save(user);
    }
}
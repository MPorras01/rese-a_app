package com.resenias.reviews.security;

import java.util.Map;

import org.springframework.security.oauth2.client.userinfo.DefaultOAuth2UserService;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserRequest;
import org.springframework.security.oauth2.client.userinfo.OAuth2UserService;
import org.springframework.security.oauth2.core.OAuth2AuthenticationException;
import org.springframework.security.oauth2.core.OAuth2Error;
import org.springframework.security.oauth2.core.user.OAuth2User;
import org.springframework.stereotype.Service;

import com.resenias.reviews.entity.User;
import com.resenias.reviews.repository.UserRepository;

@Service
public class CustomOAuth2UserService implements OAuth2UserService<OAuth2UserRequest, OAuth2User> {

    private final UserRepository userRepository;
    private final DefaultOAuth2UserService delegate = new DefaultOAuth2UserService();

    public CustomOAuth2UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    public OAuth2User loadUser(OAuth2UserRequest userRequest) throws OAuth2AuthenticationException {
        OAuth2User oauth2User = delegate.loadUser(userRequest);
        String registrationId = userRequest.getClientRegistration().getRegistrationId().toLowerCase();

        ProviderProfile profile = extractProfile(registrationId, oauth2User.getAttributes());
        String providerValue = profile.provider().name();

        User user = userRepository
            .findByOauthProviderAndOauthSubject(providerValue, profile.subject())
            .orElseGet(() -> createNewUser(profile));

        user.setName(profile.name());
        user.setAvatarUrl(profile.avatarUrl());
        user.setEmail(profile.email());
        user = userRepository.save(user);

        return new UserPrincipal(user, oauth2User.getAttributes());
    }

    private User createNewUser(ProviderProfile profile) {
        return User.builder()
            .email(profile.email())
            .emailVerified(true)
            .status(User.UserStatus.PENDING_PHONE)
            .oauthProvider(profile.provider())
            .oauthSubject(profile.subject())
            .name(profile.name())
            .avatarUrl(profile.avatarUrl())
            .role(User.Role.USER)
            .build();
    }

    private ProviderProfile extractProfile(String registrationId, Map<String, Object> attributes) {
        if ("google".equals(registrationId)) {
            return new ProviderProfile(
                User.OAuthProvider.GOOGLE,
                getRequired(attributes, "sub"),
                getRequired(attributes, "name"),
                getRequired(attributes, "email"),
                asString(attributes.get("picture"))
            );
        }

        if ("facebook".equals(registrationId)) {
            return new ProviderProfile(
                User.OAuthProvider.FACEBOOK,
                getRequired(attributes, "id"),
                getRequired(attributes, "name"),
                getRequired(attributes, "email"),
                extractFacebookPicture(attributes)
            );
        }

        throw new OAuth2AuthenticationException(new OAuth2Error("unsupported_provider"),
            "Unsupported OAuth2 provider: " + registrationId);
    }

    private String extractFacebookPicture(Map<String, Object> attributes) {
        Object pictureObj = attributes.get("picture");
        if (!(pictureObj instanceof Map<?, ?> pictureMap)) {
            return null;
        }

        Object dataObj = pictureMap.get("data");
        if (!(dataObj instanceof Map<?, ?> dataMap)) {
            return null;
        }

        return asString(dataMap.get("url"));
    }

    private String getRequired(Map<String, Object> attributes, String key) {
        String value = asString(attributes.get(key));
        if (value == null || value.isBlank()) {
            throw new OAuth2AuthenticationException(new OAuth2Error("missing_attribute"),
                "Missing OAuth2 attribute: " + key);
        }
        return value;
    }

    private String asString(Object value) {
        return value == null ? null : String.valueOf(value);
    }

    private record ProviderProfile(User.OAuthProvider provider,
                                   String subject,
                                   String name,
                                   String email,
                                   String avatarUrl) {
    }
}

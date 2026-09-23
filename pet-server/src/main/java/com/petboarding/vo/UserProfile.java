package com.petboarding.vo;

import com.petboarding.entity.User;

public record UserProfile(Long id, String nickname, String avatar, String phone) {
    public static UserProfile from(User user) {
        return new UserProfile(user.getId(), user.getNickname(), user.getAvatar(), user.getPhone());
    }
}

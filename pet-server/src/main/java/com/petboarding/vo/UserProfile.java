package com.petboarding.vo;

import com.fasterxml.jackson.databind.annotation.JsonSerialize;
import com.fasterxml.jackson.databind.ser.std.ToStringSerializer;

import com.petboarding.entity.User;

public record UserProfile(@JsonSerialize(using = ToStringSerializer.class) Long id, String nickname, String avatar, String phone) {
    public static UserProfile from(User user) {
        return new UserProfile(user.getId(), user.getNickname(), user.getAvatar(), user.getPhone());
    }
}

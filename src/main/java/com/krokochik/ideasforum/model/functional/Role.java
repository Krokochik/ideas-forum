package com.krokochik.ideasforum.model.functional;

import lombok.NonNull;
import lombok.ToString;
import org.jetbrains.annotations.Contract;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;

@ToString
public enum Role
{
    USER,
    ADMIN,
    ANONYM;

    @Contract(" -> new")
    public @NonNull GrantedAuthority toAuthority() {
        return new SimpleGrantedAuthority(this.name());
    }
}

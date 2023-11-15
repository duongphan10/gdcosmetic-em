package com.vn.em.security;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.vn.em.domain.entity.User;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.*;

public class UserPrincipal implements UserDetails {

    private final Integer id;

    @JsonIgnore
    private final String username;

    @JsonIgnore
    private final String password;

    private final Collection<? extends GrantedAuthority> authorities;

    public UserPrincipal(String username, String password, Collection<? extends GrantedAuthority> authorities) {
        this(null, username, password, authorities);
    }

    public UserPrincipal(Integer id, String username, String password,
                         Collection<? extends GrantedAuthority> authorities) {
        this.id = id;
        this.username = username;
        this.password = password;

        if (authorities == null) {
            this.authorities = null;
        } else {
            this.authorities = new ArrayList<>(authorities);
        }
    }

    public static UserPrincipal create(User user) {
        List<GrantedAuthority> authorities = new LinkedList<>();
        authorities.add(new SimpleGrantedAuthority(user.getRole().getName()));
        return new UserPrincipal(user.getId(),
                user.getUsername(), user.getPassword(), authorities);
    }

    public Integer getId() {
        return id;
    }

    @Override
    public String getUsername() {
        return username;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities == null ? null : new ArrayList<>(authorities);
    }

    @Override
    public boolean isAccountNonExpired() {
        return true;
    }

    @Override
    public boolean isAccountNonLocked() {
        return true;
    }

    @Override
    public boolean isCredentialsNonExpired() {
        return true;
    }

    @Override
    public boolean isEnabled() {
        return true;
    }

    public boolean equals(Object object) {
        if (this == object)
            return true;
        if (object == null || getClass() != object.getClass())
            return false;
        UserPrincipal that = (UserPrincipal) object;
        return Objects.equals(id, that.id);
    }

    public int hashCode() {
        return Objects.hash(id);
    }

}

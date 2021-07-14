package com.ebooking.BookUrMovie.accounts;

import com.ebooking.BookUrMovie.commons.models.User;
import lombok.NoArgsConstructor;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.authority.SimpleGrantedAuthority;
import org.springframework.security.core.userdetails.UserDetails;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.stream.Collectors;

/**
 * BymUserDetails: Provides core user information.
 * This class simply stores user information which is later encapsulated into
 * authentication objects after the user logs-in successfully.
 * Details are obtained from the User object in constructor and mapped to the respective properties.
 * String of authorities in User are mapped as SimpleGrantedAuthority objects list.
 */
@NoArgsConstructor
public class BymUserDetails implements UserDetails {

    private String email;
    private String password;
    private boolean active;
    private int userId;
    private List<GrantedAuthority> authorities;

    public BymUserDetails(User user) {
        this.email = user.getUserEmail();
        this.password = user.getUserPassword();
        this.active = user.isUserIsActive();
        this.userId = user.getUserId();
        this.authorities = Arrays
            .stream(user.getUserRoles()
                .split(",")).map(String::trim)
            .map(SimpleGrantedAuthority::new)
            .collect(Collectors.toList());
    }

    @Override
    public Collection<? extends GrantedAuthority> getAuthorities() {
        return authorities;
    }

    @Override
    public String getPassword() {
        return password;
    }

    @Override
    public String getUsername() {
        return email;
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
        return active;
    }

}

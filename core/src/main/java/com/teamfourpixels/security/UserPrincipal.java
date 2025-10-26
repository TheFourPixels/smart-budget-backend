//package com.teamfourpixels.security;
//
//import lombok.Getter;
//import org.springframework.security.core.GrantedAuthority;
//import org.springframework.security.core.authority.SimpleGrantedAuthority;
//import org.springframework.security.core.userdetails.UserDetails;
//
//import java.util.Collection;
//import java.util.Collections;
//
//public class UserPrincipal implements UserDetails {
//
//    @Getter
//    private final Long id;
//    private final String email;
//    private final String password;
//    private final Collection<? extends GrantedAuthority> authorities;
//
//    public UserPrincipal(Long id, String email, String password, String role) {
//        this.id = id;
//        this.email = email;
//        this.password = password;
//        this.authorities = Collections.singletonList(new SimpleGrantedAuthority(role));
//    }
//
//    @Override
//    public Collection<? extends GrantedAuthority> getAuthorities() {
//        return authorities;
//    }
//
//    @Override
//    public String getPassword() {
//        return password;
//    }
//
//    @Override
//    public String getUsername() {
//        return email;
//    }
//
//    @Override
//    public boolean isAccountNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isAccountNonLocked() {
//        return true;
//    }
//
//    @Override
//    public boolean isCredentialsNonExpired() {
//        return true;
//    }
//
//    @Override
//    public boolean isEnabled() {
//        return true;
//    }
//}
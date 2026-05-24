package com.teamfourpixels.mapper;

import com.teamfourpixels.dto.RegisterRequest;
import com.teamfourpixels.entity.User;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;

@Mapper(componentModel = "spring")
public abstract class UserMapper {

    @Autowired
    protected PasswordEncoder passwordEncoder;

    @Mapping(target = "id", ignore = true)
    @Mapping(target = "passwordHash", expression = "java(passwordEncoder.encode(request.getPassword()))")
    @Mapping(target = "email", source = "request.email")
    @Mapping(target = "name", source = "request.name")
    public abstract User toEntity(RegisterRequest request);
}

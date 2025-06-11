package com.example.demo.domain.mapper;

import com.example.demo.domain.User;
import com.example.demo.domain.dto.UserDTO;
import org.mapstruct.Mapper;
import org.mapstruct.Mapping;
import org.mapstruct.ReportingPolicy;

import java.util.List;

@Mapper(componentModel = "spring", unmappedTargetPolicy = ReportingPolicy.IGNORE, uses = {PersonMapper.class})
public interface UserMapper {

    @Mapping(source = "person", target = "person")
    UserDTO toDto(User user);

    @Mapping(source = "person", target = "person")
    User toEntity(UserDTO userDTO);

    List<UserDTO> toDtoList(List<User> users);

    List<User> toEntityList(List<UserDTO> userDTOs);
} 
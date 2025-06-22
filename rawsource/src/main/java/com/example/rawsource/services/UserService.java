package com.example.rawsource.services;

import com.example.rawsource.entities.dto.user.UserDto;
import com.example.rawsource.entities.User;
import java.util.List;
import java.util.UUID;

public interface UserService {
    UserDto createUser(UserDto userDTO);
    UserDto getUserById(UUID id);
    UserDto getUserByEmail(String email);
    List<UserDto> getAllUsers();
    UserDto updateUser(UUID id, UserDto userDTO);
    void deleteUser(UUID id);
    UserDto convertToDTO(User user);
    boolean isCurrentUser(UUID id);
}
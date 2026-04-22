package com.minimartph.Minit.mapper;

import com.minimartph.Minit.dto.UserResponse;
import com.minimartph.Minit.entity.User;
import org.springframework.stereotype.Component;

@Component
public class UserMapper {

  public UserResponse toResponse(User user) {
    if (user == null) return null;
    UserResponse response = new UserResponse();
    response.setId(user.getId());
    response.setFirstName(user.getFirstName());
    response.setLastName(user.getLastName());
    response.setFullName(user.getFullName());
    response.setEmail(user.getEmail());
    response.setGender(user.getGender());
    response.setPhoneNumber(user.getPhoneNumber());
    response.setAddress(user.getAddress());
    response.setAge(user.getAge());
    response.setUsername(user.getUsername());
    response.setRole(user.getRole().name());
    response.setActive(user.isActive());
    response.setCreatedDateTime(user.getCreatedDateTime());
    response.setPhotoUrl(user.getPhotoUrl());
    response.setResumeUrl(user.getResumeUrl());
    response.setBarangayClearanceUrl(user.getBarangayClearanceUrl());
    return response;
  }
}

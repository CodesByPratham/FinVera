package com.pratham.finvera.dto;

import java.time.LocalDate;

import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Past;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;

import lombok.Data;

@Data
public class UpdateUserProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @Pattern(regexp = "^$|^\\+?[0-9]{10,15}$", message = "Phone must be a valid international number")
    private String phone;

    @Past(message = "Date of birth must be a past date")
    private LocalDate dob;

    @Pattern(regexp = "^$|^(MALE|FEMALE|OTHER)$", message = "Gender must be either MALE, FEMALE or OTHER")
    private String gender;

    @Size(max = 500, message = "Profile image URL must be at most 500 characters")
    @URL(message = "Profile photo must be a valid image URL")
    private String profilePhotoUrl;
}
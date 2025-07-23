package com.pratham.finvera.dto;

import java.time.LocalDate;

import org.hibernate.validator.constraints.URL;
import org.springframework.format.annotation.DateTimeFormat;

import com.fasterxml.jackson.annotation.JsonFormat;
import com.pratham.finvera.enums.Gender;

import io.micrometer.common.lang.Nullable;
import jakarta.persistence.EnumType;
import jakarta.persistence.Enumerated;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class UpdateUserProfileRequest {

    @NotBlank(message = "Name is required")
    @Size(min = 2, max = 50, message = "Name must be between 2 and 50 characters")
    private String name;

    @Pattern(regexp = "^\\+?[0-9]{10,15}$", message = "Phone must be a valid international number")
    private String phone;

    @Pattern(regexp = "^\\d{4}-(0[1-9]|1[0-2])-(0[1-9]|[12]\\d|3[01])$", message = "DOB must be in the format YYYY-MM-DD and valid")
    private String dob;

    @Pattern(regexp = "^(MALE|FEMALE|OTHER)$", message = "Gender can be either MALE or FEMALE or OTHER")
    private String gender;

    @Size(min = 1, message = "Profile photo URL must not be empty")
    @URL(message = "Profile photo must be a valid image URL")
    private String profilePhotoUrl;
}
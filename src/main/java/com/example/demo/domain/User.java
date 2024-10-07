package com.example.demo.domain;

import jakarta.validation.constraints.Pattern;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import static com.example.demo.constants.RegularExpressions.REGEX_EMAIL;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class User extends AuditablePojo{

    private String username;

    @Pattern(regexp = REGEX_EMAIL)
    private String email;

    private String password;
}

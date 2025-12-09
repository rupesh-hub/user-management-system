package com.alfarays.user.model;

import lombok.AllArgsConstructor;
import lombok.Getter;
import lombok.NoArgsConstructor;
import lombok.Setter;
import org.springframework.data.domain.Sort;

import java.time.LocalDate;

@Getter
@Setter
@NoArgsConstructor
@AllArgsConstructor
public class UserFilterDTO {
    private String query;
    private String firstname;
    private String lastname;
    private String email;
    private String username;
    private Boolean active;
    private LocalDate createdAfter;
    private LocalDate createdBefore;
    private String sortBy = "createdOn";
    private Sort.Direction sortDirection = Sort.Direction.DESC;
}
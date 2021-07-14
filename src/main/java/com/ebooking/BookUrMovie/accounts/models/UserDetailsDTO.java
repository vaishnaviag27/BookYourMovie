package com.ebooking.BookUrMovie.accounts.models;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class UserDetailsDTO {

    private String username;

    private String email;

    private String password;

    private String phone;

    private int age;

    private String gender;

    private String location;

}

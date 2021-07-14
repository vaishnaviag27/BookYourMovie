package com.ebooking.BookUrMovie.commons.models;

import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.CascadeType;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;
import javax.persistence.OneToMany;
import java.util.ArrayList;
import java.util.List;

@Entity
@Data
@NoArgsConstructor
public class User {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int userId;

    @Column(unique = true)
    private String userEmail;

    private String userName;

    private String userPassword;

    private boolean userIsActive = false;

    private String userRoles;

    private int userAge;

    private String userPhone;

    private String userGender;


    private String userLocation;

    @OneToMany(mappedBy = "user", cascade = CascadeType.ALL)
    private List<Booking> bookings = new ArrayList<>();

    public User(String userName, String userEmail, String userPassword, boolean userIsActive, String userRoles, int userAge, String userPhone, String userGender, String userLocation) {
        this.userEmail = userEmail;
        this.userName = userName;
        this.userPassword = userPassword;
        this.userIsActive = userIsActive;
        this.userRoles = userRoles;
        this.userAge = userAge;
        this.userPhone = userPhone;
        this.userGender = userGender;
        this.userLocation = userLocation;
    }
}

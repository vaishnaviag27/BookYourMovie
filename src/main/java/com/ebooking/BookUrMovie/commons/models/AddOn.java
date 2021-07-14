package com.ebooking.BookUrMovie.commons.models;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
@Data
@NoArgsConstructor
@AllArgsConstructor
public class AddOn {

    @Id
    private String addonName;

    private float addonPrice;

    private Boolean addonIsAvailable;

}

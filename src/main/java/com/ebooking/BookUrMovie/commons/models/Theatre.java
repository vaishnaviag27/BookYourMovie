package com.ebooking.BookUrMovie.commons.models;

import com.fasterxml.jackson.annotation.JsonIgnore;
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
public class Theatre {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private int theatreId;

    @Column(unique = true)
    private String theatreName;

    private float theatreRating;

    private String theatreLocation;

    @OneToMany(mappedBy = "theatre", cascade = CascadeType.ALL)
    @JsonIgnore
    List<Slot> slots = new ArrayList<>();

    @OneToMany(mappedBy = "theatre", cascade = CascadeType.ALL)
    List<TheatreRating> theatreRatings = new ArrayList<>();

    public Theatre(String theatreName, float theatreRating, String theatreLocation) {
        this.theatreName = theatreName;
        this.theatreRating = theatreRating;
        this.theatreLocation = theatreLocation;
    }
}

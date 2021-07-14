package com.ebooking.BookUrMovie.accounts.services;

import com.ebooking.BookUrMovie.commons.exceptions.BadDetailsException;
import com.ebooking.BookUrMovie.commons.exceptions.NotFoundException;
import com.ebooking.BookUrMovie.commons.models.Booking;
import com.ebooking.BookUrMovie.commons.models.User;
import com.ebooking.BookUrMovie.accounts.models.UserDetailsDTO;
import com.ebooking.BookUrMovie.accounts.repositories.UserRepository;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

;
import java.security.Principal;
import java.util.List;

@Service
@Slf4j
public class UserServices {

    @Autowired
    UserRepository userRepository;

    BCryptPasswordEncoder passwordEncoder = new BCryptPasswordEncoder();

    public List<User> getAllUsers() throws NotFoundException {
        log.debug("Fetching All Users");
        List<User> users = userRepository.findAll();
        if (users.isEmpty()) {
            log.error("No Users found!");
            throw new NotFoundException("No users found!");
        }
        return users;
    }

    public User getUserById(int userId) throws NotFoundException {
        log.debug("Fetching user by user Id: {}", userId);
        User user = userRepository.findByUserId(userId);
        if (user == null) {
            log.error("user not found with Id: {}", userId);
            throw new NotFoundException("user not found with Id: " + userId);
        }
        return user;
    }

    public User getUserByEmailUser(Principal principal) throws NotFoundException {
        log.debug("Fetching user by user Email");
        User user = userRepository.findByUserEmail(principal.getName()).get();
        if (user == null) {
            log.error("No user found with Email: {}", principal.getName());
            throw new NotFoundException("No user found with Email: " + principal.getName());
        }
        return user;
    }

    public User getUserByEmail(String mail) throws NotFoundException {
        log.debug("Fetching user by user Email");
        User user = userRepository.findByUserEmail(mail).get();
        if (user == null) {
            log.error("No user found with Email: {}", mail);
            throw new NotFoundException("No user found with Email: " + mail);
        }
        return user;
    }

    public User getUserSummaryByEmail(String userEmail) throws NotFoundException {
        log.debug("Fetching user by user Email");
        User user = userRepository.findByUserEmail(userEmail).get();
        if (user == null) {
            log.error("No user found with Email: {}", userEmail);
            throw new NotFoundException("No user found with Email: " + userEmail);
        }
        user.setBookings(null);
        user.setUserPassword("");
        return user;
    }

    public String addUser(UserDetailsDTO newUser) throws IllegalArgumentException, BadDetailsException {
        log.debug("Adding a new user");
        if (userRepository.existsByUserEmail(newUser.getEmail())) {
            log.error("User already exists: {}", newUser.getEmail());
            throw new IllegalArgumentException("User already exists: " + newUser.getEmail());
        }
        String userDetailsValidation = validateUser(newUser, true);
        if (!userDetailsValidation.equals("valid")) {
            log.error("User Details are invalid: {}", userDetailsValidation);
            throw new BadDetailsException("User Details are invalid: " + userDetailsValidation);
        }
        log.debug("User Details Are Valid");
        User user = new User();
        user.setUserEmail(newUser.getEmail());
        user.setUserPassword(passwordEncoder.encode(newUser.getPassword()));
        user.setUserName(newUser.getUsername());
        user.setUserRoles("ROLE_USER");
        user.setUserAge(newUser.getAge());
        user.setUserPhone(newUser.getPhone());
        user.setUserGender(newUser.getGender());
        user.setUserIsActive(true);
        user.setUserLocation(newUser.getLocation());
        userRepository.save(user);
        log.debug("Registered User with Email: {}", newUser.getEmail());
        return "User Registered Successfully!";
    }

    public String updateUser(UserDetailsDTO updatedDetails) throws NotFoundException, BadDetailsException {
        log.debug("Updating User with Email: {}", updatedDetails.getEmail());
        User user = getUserByEmail(updatedDetails.getEmail());
        String userDetailsValidation = validateUser(updatedDetails, false);
        if (!userDetailsValidation.equals("valid")) {
            log.error("User Details are invalid: {}", userDetailsValidation);
            throw new BadDetailsException("User Details are invalid: " + userDetailsValidation);
        }
        log.debug("Valid user details");
        Boolean hasUpdate = false;
        if (updatedDetails.getUsername() != null && !updatedDetails.getUsername().equals(user.getUserName())) {
            user.setUserName(updatedDetails.getUsername());
            hasUpdate = true;
        }
        if (updatedDetails.getPassword() != null && !passwordEncoder.matches(updatedDetails.getPassword(), user.getUserPassword())) {
            user.setUserPassword(passwordEncoder.encode(updatedDetails.getPassword()));
            hasUpdate = true;
        }
        if (updatedDetails.getAge() != 0 && updatedDetails.getAge() != user.getUserAge()) {
            user.setUserAge(updatedDetails.getAge());
            hasUpdate = true;
        }
        if (updatedDetails.getGender() != null && !updatedDetails.getGender().equals(user.getUserGender())) {
            user.setUserGender(updatedDetails.getGender());
            hasUpdate = true;
        }
        if (updatedDetails.getPhone() != null && !updatedDetails.getPhone().equals(user.getUserPhone())) {
            user.setUserPhone(updatedDetails.getPhone());
            hasUpdate = true;
        }
        if (updatedDetails.getLocation() != null && !updatedDetails.getLocation().equals(user.getUserLocation())) {
            user.setUserLocation(updatedDetails.getLocation());
            hasUpdate = true;
        }
        if (hasUpdate) {
            userRepository.save(user);
            log.debug("Updated User with Email: {} Successfully", updatedDetails.getEmail());
            return "User Details Updated Successfully!";
        } else {
            return "Nothing to Update!";
        }
    }

    public String updateUserLocation(String email, String location) throws NotFoundException, BadDetailsException {
        log.debug("Updating {}'s location to {}", email, location);
        if (location.length() == 0) {
            log.error("Updating User: User location is invalid");
            throw new BadDetailsException("Please Enter a valid Location");
        }
        User user = getUserByEmail(email);
        user.setUserLocation(location);
        userRepository.save(user);
        return String.format("%s's location updated successfully to %s", email, location);
    }

    public List<Booking> getUserBookingsById(int userId) throws NotFoundException {
        log.debug("Fetching bookings by user Id: {}", userId);
        User foundUser = getUserById(userId);
        foundUser.getBookings().stream().forEach(booking -> {
            booking.getSlot().setSeats(null);
        });
        return foundUser.getBookings();
    }

    public List<Booking> getUserBookingsByUserEmail(String userEmail) throws NotFoundException {
        log.debug("Fetching bookings by user Email: {}", userEmail);
        User foundUser = getUserByEmail(userEmail);
        if(foundUser.getBookings().isEmpty()){
            throw new NotFoundException("You have no bookings till now!");
        }
        foundUser.getBookings().stream().forEach(booking -> {
            booking.getSlot().setSeats(null);
        });
        return foundUser.getBookings();
    }

    private String validateUser(UserDetailsDTO userDetails, boolean signup) {
        if (signup) {
            if (userDetails.getUsername() == null) {
                return "username cannot be null";
            }
            if (userDetails.getPhone() == null) {
                return "user phone number cannot be empty";
            }
            if (userDetails.getEmail() == null) {
                return "user e-mail cannot be empty";
            }
            if (userDetails.getPassword() == null) {
                return "user password cannot be empty";
            }
            if (userDetails.getGender() == null) {
                return "user gender cannot be empty";
            }
            if (userDetails.getLocation() == null) {
                return "user location cannot be empty";
            }
            if (userDetails.getAge() == 0) {
                return "user Age cannot be 0";
            }
        }

        if (userDetails.getUsername() != null) {
            int userNameLength = userDetails.getUsername().length();
            if (userNameLength < 5 || userNameLength > 50) {
                return "username should have 5-50 characters";
            }
        }
        if (userDetails.getPhone() != null) {
            String userPhone = userDetails.getPhone();
            if (userPhone.length() != 10 && !userPhone.matches("[+-]?[0-9]+")) {
                return "Enter a valid 10 digit Phone Number";
            }
        }
        if (userDetails.getLocation() != null) {
            String userLocation = userDetails.getLocation();
            if (userLocation.length() == 0) {
                return "Enter a valid Location";
            }
        }
        int userAge = userDetails.getAge();
        if (userAge != 0 && (userAge < 16 || userAge >= 100)) {
            return "User's age should be between 16-100";
        }
        if (userDetails.getGender() != null) {
            String gender = userDetails.getGender().toLowerCase();
            if (!(gender.equals("male") || gender.equals("female") || gender.equals("others"))) {
                return "Gender should be 'Male', 'Female' or 'Others'";
            }
        }
        if (userDetails.getEmail() != null && !userDetails.getEmail().matches("^(.+)@(.+)$")) {
            return "enter a valid email!";
        }
        if (userDetails.getPassword() != null) {
            String password = userDetails.getPassword();
            if (!password.matches("^(?=.*[0-9])(?=.*[a-z])(?=.*[A-Z])(?=.*[@#$%^&-+=()])(?=\\S+$).{10,30}$")) {
                return "Password Must Contain atleast one: " +
                    "\nUpper case letter, " +
                    "\nOne Lowercase letter, " +
                    "\nOne special character, " +
                    "\nNo White Spaces!" +
                    "\n10-50 characters Long";
            }
        }
        return "valid";
    }
}

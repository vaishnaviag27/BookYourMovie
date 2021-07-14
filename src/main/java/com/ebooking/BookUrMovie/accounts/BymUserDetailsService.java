package com.ebooking.BookUrMovie.accounts;

import com.ebooking.BookUrMovie.commons.models.User;
import com.ebooking.BookUrMovie.accounts.repositories.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.stereotype.Service;

import java.util.Optional;

/**
 * BymUserDetailsService loads the user by userEmail from the database
 * and maps it to BymUserDetails object which is encapsulated in Authentication object.
 */
@Service
public class BymUserDetailsService implements UserDetailsService {

    @Autowired
    UserRepository userRepository;

    /**
     * Method used to load UserDetails from
     *
     * @param userEmail The email used to retrieve user from user repository
     * @return UserDetails consisting of authentication details of the user
     * @throws UsernameNotFoundException if user with the given email is not found in user repository
     */
    @Override
    public UserDetails loadUserByUsername(String userEmail) throws UsernameNotFoundException {
        Optional<User> user = userRepository.findByUserEmail(userEmail);
        user.orElseThrow(() -> new UsernameNotFoundException("User with " + userEmail + " not found."));
        return user.map(BymUserDetails::new).get();
    }
}

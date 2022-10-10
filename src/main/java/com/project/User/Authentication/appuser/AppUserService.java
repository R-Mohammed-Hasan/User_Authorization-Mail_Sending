package com.project.User.Authentication.appuser;

import com.project.User.Authentication.registration.token.ConfirmationToken;
import com.project.User.Authentication.registration.token.ConfirmationTokenService;
import lombok.AllArgsConstructor;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.UUID;

@Service
@AllArgsConstructor
public class AppUserService implements UserDetailsService {

    private final static String userNotFoundMsg = "User with email %s not found";

    @Autowired
    private final UserRepo repo;
    private final BCryptPasswordEncoder encoder;
    private final ConfirmationTokenService confirmationTokenService;

    @Override
    public UserDetails loadUserByUsername(String email) throws UsernameNotFoundException {
        return repo.findByEmail(email).orElseThrow(
                () -> new UsernameNotFoundException(String.format(userNotFoundMsg,email))
        );
    }

    public String signUpUser(AppUser user){

        boolean userExists = repo.findByEmail(user.getEmail()).isPresent();
        // get user and store it in variable. Check user && user.confirmed == true.
        if(userExists){
            throw  new IllegalStateException("email already exists");
        }
        String encodedPassword = encoder.encode(user.getPassword());
        user.setPassword(encodedPassword);
        repo.save(user);
        // Creating token
        String token = UUID.randomUUID().toString();
        ConfirmationToken confirmationToken = new ConfirmationToken(token,
                LocalDateTime.now(),
                LocalDateTime.now().plusMinutes(15),
                user
                );
        confirmationTokenService.saveConfirmationToken(confirmationToken);
        return token;
    }

    public int enableAppUser(String email) {
        return repo.enableAppUser(email);
    }

}

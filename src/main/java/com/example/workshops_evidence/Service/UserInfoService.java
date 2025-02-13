package com.example.workshops_evidence.Service;

import com.example.workshops_evidence.Entity.UserInfo;
import com.example.workshops_evidence.Repository.UserInfoRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Primary;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.security.core.userdetails.UserDetailsService;
import org.springframework.security.core.userdetails.UsernameNotFoundException;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import java.util.Optional;
import lombok.extern.slf4j.Slf4j;

@Slf4j
@Service
@Primary
public class UserInfoService implements UserDetailsService {

    @Autowired
    private UserInfoRepository repository;

    @Autowired
    private PasswordEncoder encoder;

    @Override
    public UserDetails loadUserByUsername(String username) throws UsernameNotFoundException {
        log.info("Loading user by username: {}", username);
        try {
            Optional<UserInfo> userDetail = repository.findByName(username);
            return userDetail.map(UserInfoDetails::new)
                    .orElseThrow(() -> new UsernameNotFoundException("User not found: " + username));
        } catch (Exception e) {
            log.error("Error loading user by username: {}", username, e);
            throw e;
        }
    }

    public String addUser(UserInfo userInfo) {
        userInfo.setPassword(encoder.encode(userInfo.getPassword()));
        repository.save(userInfo);
        return "User Added Successfully";
    }
}
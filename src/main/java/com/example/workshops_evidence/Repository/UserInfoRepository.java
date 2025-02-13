package com.example.workshops_evidence.Repository;

import com.example.workshops_evidence.Entity.UserInfo;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface UserInfoRepository extends JpaRepository<UserInfo, Integer> {
    Optional<UserInfo> findByEmail(String email); // Use 'email' if that is the correct field for login
    Optional<UserInfo> findByName(String name);
    // You can add custom queries here if needed
    Optional<UserInfo> findByNameOrEmail(String username, String email);
    Boolean existsByName(String username);
    Boolean existsByEmail(String email);
    @Query("SELECT u FROM UserInfo u JOIN u.activities a WHERE a.id = :activityId")
    List<UserInfo> findUsersByActivityId(@Param("activityId") int activityId);
}
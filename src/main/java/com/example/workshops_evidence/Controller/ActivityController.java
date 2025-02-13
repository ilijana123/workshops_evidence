package com.example.workshops_evidence.Controller;

import com.example.workshops_evidence.Dto.UserDto;
import com.example.workshops_evidence.Entity.Activity;
import com.example.workshops_evidence.Entity.UserInfo;
import com.example.workshops_evidence.Entity.Workshop;
import com.example.workshops_evidence.Enums.TaskStatus;
import com.example.workshops_evidence.Repository.ActivityRepository;
import com.example.workshops_evidence.Repository.UserInfoRepository;
import com.example.workshops_evidence.Repository.WorkshopRepository;
import com.example.workshops_evidence.Service.JwtService;
import com.example.workshops_evidence.Service.UserInfoDetails;
import io.jsonwebtoken.ExpiredJwtException;
import io.jsonwebtoken.MalformedJwtException;
import io.jsonwebtoken.UnsupportedJwtException;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.web.bind.annotation.*;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/activities")
@CrossOrigin
public class ActivityController {

    @Autowired
    private JwtService jwtService;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private WorkshopRepository workshopRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @PostMapping("/add/{workshopId}")
    public ResponseEntity<?> addActivityToWorkshop(@RequestHeader("Authorization") String token, @PathVariable int workshopId, @RequestBody Activity activity) {
        if (token == null ||!token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Token is missing or invalid format");
        }

        String jwt = token.substring(7);

        try {
            String username = jwtService.extractUsername(jwt);

            Optional<UserInfo> userOptional = userInfoRepository.findByName(username);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            UserInfo user = userOptional.get();

            Optional<Workshop> workshopOpt = workshopRepository.findById(workshopId);

            if (workshopOpt.isPresent()) {
                Workshop workshop = workshopOpt.get();

                if (workshop.getOwnerId()==user.getId()) {
                    activity.setWorkshop(workshop);
                    activity.setStatus(TaskStatus.PENDING);
                    activityRepository.save(activity);
                    return new ResponseEntity<>("Activity added successfully!", HttpStatus.CREATED);
                } else {
                    return new ResponseEntity<>("Forbidden: Not the owner of this workshop", HttpStatus.FORBIDDEN);
                }

            } else {
                return new ResponseEntity<>("Workshop not found!", HttpStatus.NOT_FOUND);
            }

        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + ex.getMessage());
        }
    }

    @PatchMapping("/{activityId}/update")
    public ResponseEntity<?> updateActivity(
            @RequestHeader("Authorization") String token,
            @PathVariable int activityId,
            @RequestBody Activity activity) {

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: Token is missing or invalid format");
        }
        String jwt = token.substring(7);
        try {
            String username = jwtService.extractUsername(jwt);
            Optional<UserInfo> userOptional = userInfoRepository.findByName(username);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            UserInfo currUser = userOptional.get();
            UserDetails userDetails = new UserInfoDetails(currUser);
            if (!jwtService.validateToken(jwt, userDetails)) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("Unauthorized: Invalid token");
            }
            Activity currentActivity = activityRepository.findById(activityId).orElse(null);
            if (currentActivity == null) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND)
                        .body("Activity not found!");
            }
            int workshopOwnerId = currentActivity.getWorkshop().getOwnerId();
            if (currUser.getId() != workshopOwnerId) {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                        .body("User is not authorized to update this activity.");
            }
            if (activity.getStatus() != null) {
                currentActivity.setStatus(activity.getStatus());
            }
            if (activity.getDescription() != null) {
                currentActivity.setDescription(activity.getDescription());
            }
            if (activity.getTitle() != null) {
                currentActivity.setTitle(activity.getTitle());
            }

            activityRepository.save(currentActivity);
            return ResponseEntity.ok("Activity updated successfully!");

        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED)
                    .body("Unauthorized: " + e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR)
                    .body("Internal Server Error: " + ex.getMessage());
        }
    }

    @PostMapping("/{activityId}/addUser")
    public ResponseEntity<?> addUserToActivity(@RequestHeader("Authorization") String token, @PathVariable int activityId, @RequestBody int userId) {

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Token is missing or invalid format");
        }

        String jwt = token.substring(7);

        try {
            String username = jwtService.extractUsername(jwt);

            Optional<UserInfo> userOptional = userInfoRepository.findByName(username);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            UserInfo loggedInUser = userOptional.get();
            Optional<Activity> activityOpt = activityRepository.findById(activityId);

            if (activityOpt.isPresent()) {
                Activity currentActivity = activityOpt.get();

                if (currentActivity.getWorkshop().getOwnerId()==loggedInUser.getId()) {
                    Optional<UserInfo> userToAddOpt = userInfoRepository.findById(userId);

                    if (userToAddOpt.isPresent()) {
                        UserInfo userToAdd = userToAddOpt.get();

                        if (currentActivity.getUsers().contains(userToAdd)) {
                            return new ResponseEntity<>("User is already in this activity.", HttpStatus.CONFLICT);
                        }

                        currentActivity.getUsers().add(userToAdd);
                        userToAdd.getActivities().add(currentActivity);
                        userInfoRepository.save(userToAdd);
                        activityRepository.save(currentActivity);

                        return new ResponseEntity<>("User added successfully!", HttpStatus.CREATED);
                    } else {
                        return new ResponseEntity<>("User to add not found!", HttpStatus.NOT_FOUND);
                    }
                } else {
                    return new ResponseEntity<>("Forbidden: Not the owner of this activity's workshop", HttpStatus.FORBIDDEN);
                }
            } else {
                return new ResponseEntity<>("Activity not found!", HttpStatus.NOT_FOUND);
            }

        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + ex.getMessage());
        }
    }


    @PostMapping("/{activityId}/removeUser")
    public ResponseEntity<?> removeUserFromActivity(@RequestHeader("Authorization") String token, @PathVariable int activityId, @RequestBody int userId) {
        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Token is missing or invalid format");
        }

        String jwt = token.substring(7);

        try {
            String username = jwtService.extractUsername(jwt);

            Optional<UserInfo> userOptional = userInfoRepository.findByName(username);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }
            UserInfo loggedInUser = userOptional.get();
            Optional<Activity> activityOpt = activityRepository.findById(activityId);

            if (activityOpt.isPresent()) {
                Activity currentActivity = activityOpt.get();

                if (currentActivity.getWorkshop().getOwnerId()==loggedInUser.getId()) {
                    Optional<UserInfo> userToRemoveOpt = userInfoRepository.findById(userId);

                    if (userToRemoveOpt.isPresent()) {
                        UserInfo userToRemove = userToRemoveOpt.get();

                        if (!currentActivity.getUsers().contains(userToRemove)) {
                            return new ResponseEntity<>("User is not in this activity.", HttpStatus.NOT_FOUND);
                        }

                        currentActivity.getUsers().remove(userToRemove);
                        userToRemove.getActivities().remove(currentActivity);
                        userInfoRepository.save(userToRemove);
                        activityRepository.save(currentActivity);

                        return new ResponseEntity<>("User removed successfully!", HttpStatus.OK);
                    } else {
                        return new ResponseEntity<>("User to remove not found!", HttpStatus.NOT_FOUND);
                    }
                } else {
                    return new ResponseEntity<>("Forbidden: Not the owner of this activity's workshop", HttpStatus.FORBIDDEN);
                }
            } else {
                return new ResponseEntity<>("Activity not found!", HttpStatus.NOT_FOUND);
            }

        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + ex.getMessage());
        }
    }

    @GetMapping("/{activityId}/getUsers")
    public ResponseEntity<?> getActivityUsers(@RequestHeader("Authorization") String token, @PathVariable int activityId) {

        if (token == null || !token.startsWith("Bearer ")) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Token is missing or invalid format");
        }

        String jwt = token.substring(7);

        try {
            String username = jwtService.extractUsername(jwt);
            System.out.println("USERNAME"+username);
            Optional<UserInfo> userOptional = userInfoRepository.findByName(username);
            if (!userOptional.isPresent()) {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("User not found");
            }

            UserInfo loggedInUser = userOptional.get();
            Optional<Activity> activityOpt = activityRepository.findById(activityId);

            if (activityOpt.isPresent()) {
                Activity currentActivity = activityOpt.get();

                if (currentActivity.getWorkshop().getOwnerId() == loggedInUser.getId()) {
                    List<UserDto> userDtos = currentActivity.getUsers()
                            .stream()
                            .map(UserDto::new)
                            .collect(Collectors.toList());

                    return ResponseEntity.ok(userDtos);
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: Not the owner of this activity's workshop");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activity not found!");
            }

        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + ex.getMessage());
        }
    }

}

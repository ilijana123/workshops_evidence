package com.example.workshops_evidence.Controller;

import com.example.workshops_evidence.Dto.ActivityDto;
import com.example.workshops_evidence.Dto.WorkshopDto;
import com.example.workshops_evidence.Entity.*;
import com.example.workshops_evidence.Enums.Status;
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
import java.util.List;
import java.util.Optional;
import java.util.stream.Collectors;

@RestController
@RequestMapping("/api/workshops")
@CrossOrigin(origins = "http://localhost:3000", maxAge = 3600)
public class WorkshopController {
    @Autowired
    private JwtService jwtService;
    @Autowired
    private WorkshopRepository workshopRepository;
    @Autowired
    private UserInfoRepository userInfoRepository;
    @Autowired
    private ActivityRepository activityRepository;


    @PostMapping("/add")
    public ResponseEntity<?> addWorkshop(@RequestHeader("Authorization") String token, @RequestBody Workshop workshop) {
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
            UserInfo user = userOptional.get();
            UserDetails userDetails = new UserInfoDetails(user);

            if (jwtService.validateToken(jwt, userDetails)) {
                workshop.setOwnerId(user.getId());
                workshop.setStatus(Status.ACTIVE);
                workshopRepository.save(workshop);

                return ResponseEntity.status(HttpStatus.CREATED).body("Workshop added successfully!");
            } else {
                return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: Invalid token"); // Explicitly handle invalid token
            }
        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + ex.getMessage());
        }
    }

    @GetMapping("/all")
    public ResponseEntity<?> getAllWorkshops(@RequestHeader("Authorization") String token) {
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
            UserInfo user = userOptional.get();
            System.out.println("User ID: " + user.getId());
            List<Workshop> workshops = workshopRepository.findByOwnerId(user.getId());
            System.out.println("Number of workshops found: " + workshops.size());
            List<WorkshopDto> workshopDTOs = workshops.stream()
                    .map(WorkshopDto::new)
                    .collect(Collectors.toList());

            return new ResponseEntity<>(workshopDTOs, HttpStatus.OK);

        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + ex.getMessage());
        }
    }


    @GetMapping("/{workshopId}")
    public ResponseEntity<?> getWorkshopById(@RequestHeader("Authorization") String token, @PathVariable int workshopId) {
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
            UserInfo user = userOptional.get();
            Optional<Workshop> workshopOpt = workshopRepository.findById(workshopId);

            if (workshopOpt.isPresent()) {
                Workshop workshop = workshopOpt.get();
                if (workshop.getOwnerId() == user.getId()) {
                    WorkshopDto workshopDto = new WorkshopDto(workshop);
                    return ResponseEntity.ok(workshopDto);
                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: Not the owner of this workshop");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workshop not found!");
            }

        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + ex.getMessage());
        }
    }

    @GetMapping("/searchByStatus/{status}")
    public ResponseEntity<?> getWorkshopsByStatus(@RequestHeader("Authorization") String token, @PathVariable Status status) {
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
            UserInfo user = userOptional.get();
            List<Workshop> workshops = workshopRepository.findByStatusAndOwnerId(status, user.getId());
            List<WorkshopDto> workshopDTOs = workshopRepository.findByStatusAndOwnerId(status, user.getId())
                    .stream()
                    .map(WorkshopDto::new)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(workshopDTOs, HttpStatus.OK);
        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + ex.getMessage());
        }
    }

    @GetMapping("/searchByPriority/{priority}")
    public ResponseEntity<?> getWorkshopsByPriority(@RequestHeader("Authorization") String token, @PathVariable String priority) {
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
            UserInfo user = userOptional.get();
            List<WorkshopDto> workshopDTOs = workshopRepository.findByPriorityAndOwnerId(priority, user.getId())
                    .stream()
                    .map(WorkshopDto::new)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(workshopDTOs, HttpStatus.OK);

        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + ex.getMessage());
        }
    }

    @GetMapping("/searchByType/{type}")
    public ResponseEntity<?> getWorkshopsByType(@RequestHeader("Authorization") String token, @PathVariable String type) {
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
            UserInfo user = userOptional.get();
            List<WorkshopDto> workshopDTOs = workshopRepository.findByTypeAndOwnerId(type, user.getId())
                    .stream()
                    .map(WorkshopDto::new)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(workshopDTOs, HttpStatus.OK);

        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + ex.getMessage());
        }
    }


    @GetMapping("/searchByDate/{date}")
    public ResponseEntity<?> getWorkshopsByDate(@RequestHeader("Authorization") String token, @PathVariable String date) {

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

            UserInfo user = userOptional.get();
            List<WorkshopDto> workshopDTOs = workshopRepository.findByDateAndOwnerId(date, user.getId())
                    .stream()
                    .map(WorkshopDto::new)
                    .collect(Collectors.toList());
            return new ResponseEntity<>(workshopDTOs, HttpStatus.OK);

        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + ex.getMessage());
        }
    }

    @GetMapping("/{workshopId}/activities")
    public ResponseEntity<?> getActivitiesForWorkshop(@RequestHeader("Authorization") String token, @PathVariable int workshopId) {

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

            Optional<Workshop> workshopOpt = workshopRepository.findById(workshopId);

            if (workshopOpt.isPresent()) {
                Workshop workshop = workshopOpt.get();
                System.out.println("Logged user "+loggedInUser.getId());
                if (workshop.getOwnerId()==loggedInUser.getId()) {
                    System.out.println("YES");
                    List<ActivityDto> activityDTOs = workshop.getActivities()
                            .stream()
                            .map(ActivityDto::new)
                            .collect(Collectors.toList());
                    return new ResponseEntity<>(activityDTOs, HttpStatus.OK);
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

    @PostMapping("/{workshopId}/removeActivity")
    public ResponseEntity<?> removeActivity(@RequestHeader("Authorization") String token, @PathVariable int workshopId, @RequestBody int activityId) {
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
            Optional<Workshop> workshopOpt = workshopRepository.findById(workshopId);

            if (workshopOpt.isPresent()) {
                Workshop workshop = workshopOpt.get();

                if (workshop.getOwnerId()==loggedInUser.getId()) {
                    Optional<Activity> activityOpt = activityRepository.findById(activityId);
                    if (activityOpt.isEmpty()) {
                        return new ResponseEntity<>("Activity not found.", HttpStatus.NOT_FOUND);
                    }

                    Activity activity = activityOpt.get();

                    if (!workshop.getActivities().contains(activity)) {
                        return new ResponseEntity<>("Activity doesn't exist in this workshop.", HttpStatus.NOT_FOUND);
                    }

                    for (UserInfo user : activity.getUsers()) {
                        user.getActivities().remove(activity);
                        userInfoRepository.save(user);
                    }

                    workshop.getActivities().remove(activity);
                    workshopRepository.save(workshop);

                    return new ResponseEntity<>("Activity removed successfully!", HttpStatus.OK);

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
    @DeleteMapping("/{workshopId}/activities/{activityId}")
    public ResponseEntity<?> deleteActivity(@RequestHeader("Authorization") String token,
                                            @PathVariable int workshopId,
                                            @PathVariable int activityId) {
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
            Optional<Workshop> workshopOpt = workshopRepository.findById(workshopId);
            if (workshopOpt.isPresent()) {
                Workshop workshop = workshopOpt.get();
                if (workshop.getOwnerId() == loggedInUser.getId()) {
                    Optional<Activity> activityOpt = activityRepository.findById(activityId);
                    if (activityOpt.isEmpty()) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activity not found.");
                    }
                    Activity activity = activityOpt.get();
                    if (!workshop.getActivities().contains(activity)) {
                        return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Activity doesn't exist in this workshop.");
                    }
                    for (UserInfo user : activity.getUsers()) {
                        user.getActivities().remove(activity);
                        userInfoRepository.save(user);
                    }
                    workshop.getActivities().remove(activity);
                    workshopRepository.save(workshop);
                    activityRepository.delete(activity);

                    return ResponseEntity.ok("Activity removed successfully!");

                } else {
                    return ResponseEntity.status(HttpStatus.FORBIDDEN).body("Forbidden: Not the owner of this workshop");
                }
            } else {
                return ResponseEntity.status(HttpStatus.NOT_FOUND).body("Workshop not found!");
            }
        } catch (ExpiredJwtException | MalformedJwtException | UnsupportedJwtException | IllegalArgumentException e) {
            return ResponseEntity.status(HttpStatus.UNAUTHORIZED).body("Unauthorized: " + e.getMessage());
        } catch (Exception ex) {
            return ResponseEntity.status(HttpStatus.INTERNAL_SERVER_ERROR).body("Internal Server Error: " + ex.getMessage());
        }
    }


    @PutMapping("/{workshopId}/updateWorkshop")
    public ResponseEntity<?> updateWorkshop(@RequestHeader("Authorization") String token, @PathVariable int workshopId, @RequestBody Workshop updatedWorkshop) {
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
            Optional<Workshop> workshopOpt = workshopRepository.findById(workshopId);
            if (workshopOpt.isPresent()) {
                Workshop currentWorkshop = workshopOpt.get();

                if (currentWorkshop.getOwnerId()==loggedInUser.getId()) {
                    currentWorkshop.setTitle(updatedWorkshop.getTitle());
                    currentWorkshop.setStatus(updatedWorkshop.getStatus());
                    currentWorkshop.setDate(updatedWorkshop.getDate());
                    currentWorkshop.setType(updatedWorkshop.getType());
                    currentWorkshop.setPriority(updatedWorkshop.getPriority());

                    workshopRepository.save(currentWorkshop);
                    return new ResponseEntity<>("Workshop updated successfully!", HttpStatus.OK);
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
}

package com.example.workshops_evidence.Service;

import com.example.activities.*;
import com.example.workshops_evidence.Entity.Activity;
import com.example.workshops_evidence.Entity.UserInfo;
import com.example.workshops_evidence.Entity.Workshop;
import com.example.workshops_evidence.Enums.TaskStatus;
import com.example.workshops_evidence.Repository.ActivityRepository;
import com.example.workshops_evidence.Repository.UserInfoRepository;
import com.example.workshops_evidence.Repository.WorkshopRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.security.core.userdetails.UserDetails;
import org.springframework.stereotype.Component;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.ArrayList;
import java.util.List;
import java.util.Objects;
import java.util.Optional;

import static com.example.workshops_evidence.Enums.TaskStatus.IN_PROGRESS;
import static com.example.workshops_evidence.Enums.TaskStatus.TODO;

@Component
@Endpoint
public class ActivityEndpoint {

    private static final String NAMESPACE_URI = "http://example.com/activities";

    @Autowired
    private WorkshopRepository workshopRepository;

    @Autowired
    private ActivityRepository activityRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @Autowired
    private ActivityService activityService;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getUsersForActivityRequest")
    @ResponsePayload
    public GetUsersForActivityResponse getUsersForActivity(@RequestPayload GetUsersForActivityRequest request) {
        GetUsersForActivityResponse response = new GetUsersForActivityResponse();

        List<UserInfo> users = activityService.getUsersForActivity(request.getActivityId());
        users.forEach(user -> {
            UserResponse userResponse = new UserResponse();
            userResponse.setId(user.getId());
            userResponse.setEmail(user.getEmail());
            response.getUsers().add(userResponse);
        });

        return response;
    }

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "addActivityRequest")
    @ResponsePayload
    public AddActivityResponse addActivity(@RequestPayload AddActivityRequest request) {
        Logger logger = LoggerFactory.getLogger(ActivityEndpoint.class);
        AddActivityResponse response = new AddActivityResponse();

        try {
            logger.info("📝 Received addActivityRequest for Workshop ID: {}", request.getWorkshopId());

            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String loggedInUsername = authentication.getName();
            logger.info("Raw authenticated username: {}", loggedInUsername);

            if (authentication.getPrincipal() instanceof UserDetails) {
                UserDetails userDetails = (UserDetails) authentication.getPrincipal();
                loggedInUsername = userDetails.getUsername();
                logger.info("Extracted user email from principal: {}", loggedInUsername);
            }
            logger.info("Authenticated User: {}", loggedInUsername);

            Optional<Workshop> workshopOpt = workshopRepository.findById(request.getWorkshopId());
            if (!workshopOpt.isPresent()) {
                logger.warn("Workshop not found for ID: {}", request.getWorkshopId());
                response.setSuccess(false);
                response.setMessage("Workshop not found!");
                return response;
            }

            Workshop workshop = workshopOpt.get();
            logger.info("Workshop found: ID = {}, Owner ID = {}", workshop.getId(), workshop.getOwnerId());

            Optional<UserInfo> userOpt = userInfoRepository.findById(workshop.getOwnerId());
            if (!userOpt.isPresent()) {
                logger.error("Owner not found for Workshop ID: {}", request.getWorkshopId());
                response.setSuccess(false);
                response.setMessage("Workshop owner not found!");
                return response;
            }

            UserInfo owner = userOpt.get();
            logger.info("👤 Workshop Owner: ID = {}, Email = {}", owner.getId(), owner.getEmail());

            if (!owner.getName().equalsIgnoreCase(loggedInUsername)) {
                logger.warn("Unauthorized: Logged-in User '{}' does not own Workshop ID: {}", loggedInUsername, workshop.getId());
                response.setSuccess(false);
                response.setMessage("Unauthorized: You do not own this workshop!");
                return response;
            }

            logger.info("User '{}' is authorized to add an activity to Workshop ID: {}", loggedInUsername, workshop.getId());

            Activity activity = new Activity();
            activity.setTitle(request.getTitle());
            activity.setDescription(request.getDescription());
            activity.setStatus(mapActivityStatusToTaskStatus(request.getStatus()));
            activity.setWorkshop(workshop);

            activityRepository.save(activity);
            logger.info("🎉 Activity saved successfully! Title: '{}', Workshop ID: {}", request.getTitle(), workshop.getId());

            response.setSuccess(true);
            response.setMessage("Activity added successfully!");
        } catch (Exception e) {
            logger.error("ERROR: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("Internal Server Error: " + e.getMessage());
        }

        return response;
    }


    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "deleteActivityRequest")
    @ResponsePayload
    public DeleteActivityResponse deleteActivity(@RequestPayload DeleteActivityRequest request) {
        Logger logger = LoggerFactory.getLogger(ActivityEndpoint.class);
        DeleteActivityResponse response = new DeleteActivityResponse();

        try {
            logger.info("🗑 Received delete request for Activity ID: {} in Workshop ID: {}", request.getActivityId(), request.getWorkshopId());

            Optional<Workshop> workshopOpt = workshopRepository.findById(request.getWorkshopId());
            if (!workshopOpt.isPresent()) {
                logger.warn("Workshop with ID {} not found!", request.getWorkshopId());
                response.setSuccess(false);
                response.setMessage("Workshop not found!");
                return response;
            }

            Workshop workshop = workshopOpt.get();
            Optional<Activity> activityOpt = activityRepository.findById(request.getActivityId());
            if (activityOpt.isEmpty()) {
                logger.warn("Activity with ID {} not found!", request.getActivityId());
                response.setSuccess(false);
                response.setMessage("Activity not found!");
                return response;
            }

            Activity activity = activityOpt.get();

            if (!workshop.getActivities().contains(activity)) {
                logger.warn("Activity ID {} does not belong to Workshop ID {}!", request.getActivityId(), request.getWorkshopId());
                response.setSuccess(false);
                response.setMessage("Activity does not belong to this workshop!");
                return response;
            }

            logger.info("Removing user relationships for Activity ID: {}", activity.getId());
            for (UserInfo user : activity.getUsers()) {
                user.getActivities().remove(activity);
                userInfoRepository.save(user);
            }

            workshop.getActivities().remove(activity);
            workshopRepository.save(workshop);
            activityRepository.delete(activity);
            logger.info("✅ Activity with ID {} deleted successfully!", request.getActivityId());

            response.setSuccess(true);
            response.setMessage("Activity deleted successfully!");
        } catch (Exception e) {
            logger.error("❌ ERROR: {}", e.getMessage(), e);
            response.setSuccess(false);
            response.setMessage("Internal Server Error: " + e.getMessage());
        }

        return response;
    }




    private TaskStatus mapActivityStatusToTaskStatus(ActivityStatus activityStatus) {
        switch (activityStatus) {
            case TODO:
                return TODO;
            case IN_PROGRESS:
                return IN_PROGRESS;
            case DONE:
                return TaskStatus.DONE;
            case PENDING:
                return TaskStatus.PENDING;
            default:
                throw new IllegalArgumentException("Unknown ActivityStatus: " + activityStatus);
        }
    }
    private boolean isUserOwnerOfWorkshop(Workshop workshop, String loggedInUsername) {
        if (workshop == null) {
            return false;
        }
        var owner = userInfoRepository.findById(workshop.getOwnerId()).orElse(null);
        return owner != null && owner.getEmail().equals(loggedInUsername);
    }

}

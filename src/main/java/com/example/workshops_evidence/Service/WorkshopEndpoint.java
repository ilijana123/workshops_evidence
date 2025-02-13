package com.example.workshops_evidence.Service;
import com.example.activities.*;
import com.example.workshops_evidence.Entity.UserInfo;
import com.example.workshops_evidence.Entity.Workshop;
import com.example.workshops_evidence.Repository.UserInfoRepository;
import com.example.workshops_evidence.Repository.WorkshopRepository;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;
import org.springframework.ws.server.endpoint.annotation.Endpoint;
import org.springframework.ws.server.endpoint.annotation.PayloadRoot;
import org.springframework.ws.server.endpoint.annotation.RequestPayload;
import org.springframework.ws.server.endpoint.annotation.ResponsePayload;

import java.util.List;
import java.util.Optional;

@Endpoint
public class WorkshopEndpoint {

    private static final String NAMESPACE_URI = "http://example.com/activities";

    @Autowired
    private WorkshopRepository workshopRepository;

    @Autowired
    private UserInfoRepository userInfoRepository;

    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "deleteWorkshopRequest")
    @ResponsePayload
    public DeleteWorkshopResponse deleteWorkshop(@RequestPayload DeleteWorkshopRequest request) {
        DeleteWorkshopResponse response = new DeleteWorkshopResponse();
        Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
        String loggedInUsername = authentication.getName();
        Workshop workshop = workshopRepository.findById(request.getWorkshopId())
                .orElse(null);
        int ownerId = workshop.getOwnerId();
        Optional<UserInfo> user = userInfoRepository.findById(ownerId);
        if (workshop == null || !user.isPresent() || !user.get().getEmail().equals(loggedInUsername)) {
            response.setSuccess(false);
            response.setMessage("Workshop not found or you do not own this workshop!");
            return response;
        }
        workshopRepository.delete(workshop);
        response.setSuccess(true);
        response.setMessage("Workshop deleted successfully.");
        return response;
    }
    @PayloadRoot(namespace = NAMESPACE_URI, localPart = "getAllWorkshopsRequest")
    @ResponsePayload
    public GetAllWorkshopsResponse getAllWorkshops(@RequestPayload GetAllWorkshopsRequest request) {
        Logger logger = LoggerFactory.getLogger(WorkshopEndpoint.class);
        GetAllWorkshopsResponse response = new GetAllWorkshopsResponse();

        try {
            Authentication authentication = SecurityContextHolder.getContext().getAuthentication();
            String loggedInUsername = authentication.getName();
            logger.info("📢 Received request to get workshops for user: {}", loggedInUsername);

            Optional<UserInfo> userOptional = userInfoRepository.findByName(loggedInUsername);
            if (!userOptional.isPresent()) {
                logger.warn("User not found: {}", loggedInUsername);
                return response;
            }
            UserInfo loggedInUser = userOptional.get();
            List<Workshop> workshops = workshopRepository.findByOwnerId(loggedInUser.getId());

            for (Workshop workshop : workshops) {
                WorkshopResponse workshopResponse = new WorkshopResponse();
                workshopResponse.setId(workshop.getId());
                workshopResponse.setTitle(workshop.getTitle());
                workshopResponse.setOwnerId(workshop.getOwnerId());
                response.getWorkshops().add(workshopResponse);
            }

            logger.info("Retrieved {} workshops for User: {}", workshops.size(), loggedInUsername);
        } catch (Exception e) {
            logger.error("ERROR retrieving workshops: {}", e.getMessage(), e);
        }

        return response;
    }


}

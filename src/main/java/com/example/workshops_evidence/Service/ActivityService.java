package com.example.workshops_evidence.Service;

import com.example.workshops_evidence.Entity.UserInfo;
import com.example.workshops_evidence.Repository.UserInfoRepository;
import org.springframework.stereotype.Service;
import org.springframework.beans.factory.annotation.Autowired;

import java.util.List;

@Service
public class ActivityService {

@Autowired
private UserInfoRepository userInfoRepository;

    public List<UserInfo> getUsersForActivity(int activityId) {
        return userInfoRepository.findUsersByActivityId(activityId);
    }

}

package com.example.workshops_evidence.Repository;

import com.example.workshops_evidence.Entity.Activity;
import com.example.workshops_evidence.Entity.Workshop;
import com.example.workshops_evidence.Enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface ActivityRepository extends JpaRepository<Activity, Integer> {
    List<Activity> findByWorkshopId(int workshop_id);
}
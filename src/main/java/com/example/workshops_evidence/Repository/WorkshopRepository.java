package com.example.workshops_evidence.Repository;

import com.example.workshops_evidence.Entity.Workshop;
import com.example.workshops_evidence.Enums.Status;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface WorkshopRepository extends JpaRepository<Workshop, Integer> {
    List<Workshop> findByStatus(Status status);
    List<Workshop> findByPriority(String priority);
    List<Workshop> findByType(String type);
    List<Workshop> findByDate(String date);
    List<Workshop> findByOwnerId(int ownerId);
    List<Workshop> findByStatusAndOwnerId(Status status, int id);
    List<Workshop> findByPriorityAndOwnerId(String priority, Integer ownerId);
    List<Workshop> findByTypeAndOwnerId(String type, Integer ownerId);
    List<Workshop> findByDateAndOwnerId(String date, Integer ownerId);
}
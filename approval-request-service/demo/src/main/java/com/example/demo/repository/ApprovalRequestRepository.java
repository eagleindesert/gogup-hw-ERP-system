package com.example.demo.repository;

import java.util.List;
import java.util.Optional;

import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.document.ApprovalRequestDocument;

@Repository
public interface ApprovalRequestRepository extends MongoRepository<ApprovalRequestDocument, String> {

    Optional<ApprovalRequestDocument> findByRequestId(Long requestId);

    Optional<ApprovalRequestDocument> findTopByOrderByRequestIdDesc();

    List<ApprovalRequestDocument> findByFinalStatus(String finalStatus);

    List<ApprovalRequestDocument> findByRequesterId(Long requesterId);
}

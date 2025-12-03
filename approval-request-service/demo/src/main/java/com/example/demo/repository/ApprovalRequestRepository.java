package com.example.demo.repository;

import com.example.demo.document.ApprovalRequestDocument;
import org.springframework.data.mongodb.repository.MongoRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ApprovalRequestRepository extends MongoRepository<ApprovalRequestDocument, String> {

    Optional<ApprovalRequestDocument> findByRequestId(Long requestId);

    Optional<ApprovalRequestDocument> findTopByOrderByRequestIdDesc();

    List<ApprovalRequestDocument> findByFinalStatus(String finalStatus);
}

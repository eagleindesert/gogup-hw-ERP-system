package com.example.demo.dto;

import com.example.demo.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class EmployeeResponse {

    private Long id;
    private String name;
    private String department;
    private String position;
    private String email;
    private String phoneNumber;
    private LocalDate hireDate;
    private LocalDate birthDate;
    private String address;
    private String emergencyContact;
    private String status;
    private String profileImageUrl;
    private Long managerId;
    private Integer level;
    private LocalDateTime createdAt;

    public static EmployeeResponse from(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .hireDate(employee.getHireDate())
                .birthDate(employee.getBirthDate())
                .address(employee.getAddress())
                .emergencyContact(employee.getEmergencyContact())
                .status(employee.getStatus())
                .profileImageUrl(employee.getProfileImageUrl())
                .managerId(employee.getManagerId())
                .level(employee.getLevel())
                .createdAt(employee.getCreatedAt())
                .build();
    }
}

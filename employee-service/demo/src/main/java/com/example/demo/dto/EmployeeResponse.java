package com.example.demo.dto;

import com.example.demo.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

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
    private LocalDateTime createdAt;

    public static EmployeeResponse from(Employee employee) {
        return EmployeeResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .createdAt(employee.getCreatedAt())
                .build();
    }
}

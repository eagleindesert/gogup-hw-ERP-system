package com.example.demo.dto;

import com.example.demo.entity.Employee;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.ArrayList;
import java.util.List;

/**
 * 조직도 응답 DTO (재귀 구조)
 */
@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class OrgChartResponse {

    private Long id;
    private String name;
    private String department;
    private String position;
    private String email;
    private String phoneNumber;
    private Integer level;
    private Long managerId;
    
    @Builder.Default
    private List<OrgChartResponse> subordinates = new ArrayList<>();

    public static OrgChartResponse from(Employee employee) {
        return OrgChartResponse.builder()
                .id(employee.getId())
                .name(employee.getName())
                .department(employee.getDepartment())
                .position(employee.getPosition())
                .email(employee.getEmail())
                .phoneNumber(employee.getPhoneNumber())
                .level(employee.getLevel())
                .managerId(employee.getManagerId())
                .subordinates(new ArrayList<>())
                .build();
    }

    public void addSubordinate(OrgChartResponse subordinate) {
        this.subordinates.add(subordinate);
    }
}

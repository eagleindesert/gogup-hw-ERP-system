package com.example.demo.dto;

import java.time.LocalDate;

import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeUpdateRequest {

    @Size(max = 100, message = "부서는 100자를 초과할 수 없습니다.")
    private String department;

    @Size(max = 100, message = "직책은 100자를 초과할 수 없습니다.")
    private String position;

    @Size(max = 100, message = "이메일은 100자를 초과할 수 없습니다.")
    private String email;

    @Size(max = 20, message = "전화번호는 20자를 초과할 수 없습니다.")
    private String phoneNumber;

    private LocalDate hireDate;

    private LocalDate birthDate;

    @Size(max = 200, message = "주소는 200자를 초과할 수 없습니다.")
    private String address;

    @Size(max = 100, message = "긴급연락처는 100자를 초과할 수 없습니다.")
    private String emergencyContact;

    @Size(max = 20, message = "상태는 20자를 초과할 수 없습니다.")
    private String status;

    @Size(max = 500, message = "프로필 이미지 URL은 500자를 초과할 수 없습니다.")
    private String profileImageUrl;

    private Long managerId;

    private Integer level;
}

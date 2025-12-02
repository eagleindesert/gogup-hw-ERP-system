package com.example.demo.dto;

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
}

package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class EmployeeCreateRequest {

    @NotBlank(message = "이름은 필수 입력 항목입니다.")
    @Size(max = 100, message = "이름은 100자를 초과할 수 없습니다.")
    private String name;

    @NotBlank(message = "부서는 필수 입력 항목입니다.")
    @Size(max = 100, message = "부서는 100자를 초과할 수 없습니다.")
    private String department;

    @NotBlank(message = "직책은 필수 입력 항목입니다.")
    @Size(max = 100, message = "직책은 100자를 초과할 수 없습니다.")
    private String position;
}

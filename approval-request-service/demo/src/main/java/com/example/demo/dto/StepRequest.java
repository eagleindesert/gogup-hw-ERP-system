package com.example.demo.dto;

import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Positive;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class StepRequest {

    @NotNull(message = "결재 단계 번호는 필수입니다.")
    @Positive(message = "결재 단계 번호는 양수여야 합니다.")
    private Integer step;

    @NotNull(message = "결재자 ID는 필수입니다.")
    private Long approverId;
}

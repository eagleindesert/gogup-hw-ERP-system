package com.example.demo.dto;

import jakarta.validation.constraints.NotBlank;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
@Builder
public class ProcessRequest {

    @NotBlank(message = "상태는 필수입니다. (approved 또는 rejected)")
    private String status;
    
    /**
     * 결재 의견/코멘트
     */
    private String comment;
}

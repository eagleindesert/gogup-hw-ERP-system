package com.example.demo.controller;

import com.example.demo.dto.EmployeeCreateRequest;
import com.example.demo.dto.EmployeeUpdateRequest;
import com.example.demo.entity.Employee;
import com.example.demo.repository.EmployeeRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.hamcrest.Matchers.*;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.*;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class EmployeeControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @Autowired
    private EmployeeRepository employeeRepository;

    @BeforeEach
    void setUp() {
        employeeRepository.deleteAll();
    }

    @Test
    @DisplayName("직원 생성 성공")
    void createEmployee_Success() throws Exception {
        EmployeeCreateRequest request = new EmployeeCreateRequest("Kim", "HR", "Manager");

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.id").exists());
    }

    @Test
    @DisplayName("직원 생성 실패 - 필수 필드 누락")
    void createEmployee_Fail_MissingField() throws Exception {
        EmployeeCreateRequest request = new EmployeeCreateRequest("", "HR", "Manager");

        mockMvc.perform(post("/employees")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest());
    }

    @Test
    @DisplayName("직원 목록 조회")
    void getEmployees_Success() throws Exception {
        // Given
        createTestEmployee("Kim", "HR", "Manager");
        createTestEmployee("Lee", "IT", "Developer");

        // When & Then
        mockMvc.perform(get("/employees"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)));
    }

    @Test
    @DisplayName("부서별 직원 필터링 조회")
    void getEmployees_FilterByDepartment() throws Exception {
        // Given
        createTestEmployee("Kim", "HR", "Manager");
        createTestEmployee("Lee", "IT", "Developer");
        createTestEmployee("Park", "HR", "Staff");

        // When & Then
        mockMvc.perform(get("/employees").param("department", "HR"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(2)))
                .andExpect(jsonPath("$[*].department", everyItem(is("HR"))));
    }

    @Test
    @DisplayName("부서 및 직책별 직원 필터링 조회")
    void getEmployees_FilterByDepartmentAndPosition() throws Exception {
        // Given
        createTestEmployee("Kim", "HR", "Manager");
        createTestEmployee("Lee", "HR", "Staff");
        createTestEmployee("Park", "IT", "Manager");

        // When & Then
        mockMvc.perform(get("/employees")
                        .param("department", "HR")
                        .param("position", "Manager"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$", hasSize(1)))
                .andExpect(jsonPath("$[0].name", is("Kim")));
    }

    @Test
    @DisplayName("직원 상세 조회 성공")
    void getEmployee_Success() throws Exception {
        // Given
        Employee employee = createTestEmployee("Kim", "HR", "Manager");

        // When & Then
        mockMvc.perform(get("/employees/{id}", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.id", is(employee.getId().intValue())))
                .andExpect(jsonPath("$.name", is("Kim")))
                .andExpect(jsonPath("$.department", is("HR")))
                .andExpect(jsonPath("$.position", is("Manager")));
    }

    @Test
    @DisplayName("직원 상세 조회 실패 - 존재하지 않는 ID")
    void getEmployee_Fail_NotFound() throws Exception {
        mockMvc.perform(get("/employees/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("직원 존재 여부 확인 - 존재함")
    void existsEmployee_True() throws Exception {
        // Given
        Employee employee = createTestEmployee("Kim", "HR", "Manager");

        // When & Then
        mockMvc.perform(get("/employees/{id}/exists", employee.getId()))
                .andExpect(status().isOk())
                .andExpect(content().string("true"));
    }

    @Test
    @DisplayName("직원 존재 여부 확인 - 존재하지 않음")
    void existsEmployee_False() throws Exception {
        mockMvc.perform(get("/employees/{id}/exists", 999L))
                .andExpect(status().isOk())
                .andExpect(content().string("false"));
    }

    @Test
    @DisplayName("직원 정보 수정 성공 - department만")
    void updateEmployee_Success_Department() throws Exception {
        // Given
        Employee employee = createTestEmployee("Kim", "HR", "Manager");
        EmployeeUpdateRequest request = new EmployeeUpdateRequest("Finance", null);

        // When & Then
        mockMvc.perform(put("/employees/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department", is("Finance")))
                .andExpect(jsonPath("$.position", is("Manager")))
                .andExpect(jsonPath("$.name", is("Kim")));
    }

    @Test
    @DisplayName("직원 정보 수정 성공 - department와 position 모두")
    void updateEmployee_Success_Both() throws Exception {
        // Given
        Employee employee = createTestEmployee("Kim", "HR", "Manager");
        EmployeeUpdateRequest request = new EmployeeUpdateRequest("Finance", "Director");

        // When & Then
        mockMvc.perform(put("/employees/{id}", employee.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.department", is("Finance")))
                .andExpect(jsonPath("$.position", is("Director")));
    }

    @Test
    @DisplayName("직원 정보 수정 실패 - 존재하지 않는 ID")
    void updateEmployee_Fail_NotFound() throws Exception {
        EmployeeUpdateRequest request = new EmployeeUpdateRequest("Finance", "Director");

        mockMvc.perform(put("/employees/{id}", 999L)
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("직원 삭제 성공")
    void deleteEmployee_Success() throws Exception {
        // Given
        Employee employee = createTestEmployee("Kim", "HR", "Manager");

        // When & Then
        mockMvc.perform(delete("/employees/{id}", employee.getId()))
                .andExpect(status().isNoContent());

        // Verify deletion
        mockMvc.perform(get("/employees/{id}", employee.getId()))
                .andExpect(status().isNotFound());
    }

    @Test
    @DisplayName("직원 삭제 실패 - 존재하지 않는 ID")
    void deleteEmployee_Fail_NotFound() throws Exception {
        mockMvc.perform(delete("/employees/{id}", 999L))
                .andExpect(status().isNotFound());
    }

    private Employee createTestEmployee(String name, String department, String position) {
        Employee employee = Employee.builder()
                .name(name)
                .department(department)
                .position(position)
                .build();
        return employeeRepository.save(employee);
    }
}

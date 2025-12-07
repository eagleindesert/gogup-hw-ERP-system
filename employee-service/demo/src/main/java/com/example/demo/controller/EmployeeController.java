package com.example.demo.controller;

import com.example.demo.dto.EmployeeCreateRequest;
import com.example.demo.dto.EmployeeIdResponse;
import com.example.demo.dto.EmployeeResponse;
import com.example.demo.dto.EmployeeUpdateRequest;
import com.example.demo.dto.OrgChartResponse;
import com.example.demo.service.EmployeeService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/employees")
@RequiredArgsConstructor
public class EmployeeController {

    private final EmployeeService employeeService;

    /**
     * 직원 생성
     * POST /employees
     */
    @PostMapping
    public ResponseEntity<EmployeeIdResponse> createEmployee(
            @Valid @RequestBody EmployeeCreateRequest request) {
        Long id = employeeService.createEmployee(request);
        return ResponseEntity.status(HttpStatus.CREATED)
                .body(new EmployeeIdResponse(id));
    }

    /**
     * 직원 목록 조회 (필터링 지원)
     * GET /employees?department=HR&position=Manager
     */
    @GetMapping
    public ResponseEntity<List<EmployeeResponse>> getEmployees(
            @RequestParam(required = false) String department,
            @RequestParam(required = false) String position) {
        List<EmployeeResponse> employees = employeeService.getEmployees(department, position);
        return ResponseEntity.ok(employees);
    }

    /**
     * 직원 상세 조회
     * GET /employees/{id}
     */
    @GetMapping("/{id}")
    public ResponseEntity<EmployeeResponse> getEmployee(@PathVariable Long id) {
        EmployeeResponse employee = employeeService.getEmployee(id);
        return ResponseEntity.ok(employee);
    }

    /**
     * 직원 존재 여부 확인 (다른 서비스에서 사용)
     * GET /employees/{id}/exists
     */
    @GetMapping("/{id}/exists")
    public ResponseEntity<Boolean> existsEmployee(@PathVariable Long id) {
        boolean exists = employeeService.existsEmployee(id);
        return ResponseEntity.ok(exists);
    }

    /**
     * 직원 정보 수정 (department, position만 수정 가능)
     * PUT /employees/{id}
     */
    @PutMapping("/{id}")
    public ResponseEntity<EmployeeResponse> updateEmployee(
            @PathVariable Long id,
            @Valid @RequestBody EmployeeUpdateRequest request) {
        EmployeeResponse employee = employeeService.updateEmployee(id, request);
        return ResponseEntity.ok(employee);
    }

    /**
     * 직원 삭제
     * DELETE /employees/{id}
     */
    @DeleteMapping("/{id}")
    public ResponseEntity<Void> deleteEmployee(@PathVariable Long id) {
        employeeService.deleteEmployee(id);
        return ResponseEntity.noContent().build();
    }

    // ==================== 조직도 관련 API ====================

    /**
     * 전체 조직도 조회
     * GET /employees/org-chart
     */
    @GetMapping("/org-chart")
    public ResponseEntity<List<OrgChartResponse>> getOrgChart() {
        List<OrgChartResponse> orgChart = employeeService.getOrgChart();
        return ResponseEntity.ok(orgChart);
    }

    /**
     * 특정 직원을 루트로 하는 조직도 조회
     * GET /employees/{id}/org-chart
     */
    @GetMapping("/{id}/org-chart")
    public ResponseEntity<OrgChartResponse> getOrgChartByRoot(@PathVariable Long id) {
        OrgChartResponse orgChart = employeeService.getOrgChartByRoot(id);
        return ResponseEntity.ok(orgChart);
    }

    /**
     * 특정 직원의 부하 직원 목록 조회
     * GET /employees/{id}/subordinates
     */
    @GetMapping("/{id}/subordinates")
    public ResponseEntity<List<EmployeeResponse>> getSubordinates(@PathVariable Long id) {
        List<EmployeeResponse> subordinates = employeeService.getSubordinates(id);
        return ResponseEntity.ok(subordinates);
    }

    /**
     * 특정 직원의 상급자 조회
     * GET /employees/{id}/manager
     */
    @GetMapping("/{id}/manager")
    public ResponseEntity<EmployeeResponse> getManager(@PathVariable Long id) {
        EmployeeResponse manager = employeeService.getManager(id);
        return ResponseEntity.ok(manager);
    }

    /**
     * 부서별 직원 목록 조회
     * GET /employees/department/{department}
     */
    @GetMapping("/department/{department}")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByDepartment(@PathVariable String department) {
        List<EmployeeResponse> employees = employeeService.getEmployeesByDepartment(department);
        return ResponseEntity.ok(employees);
    }

    /**
     * 상태별 직원 목록 조회
     * GET /employees/status/{status}
     */
    @GetMapping("/status/{status}")
    public ResponseEntity<List<EmployeeResponse>> getEmployeesByStatus(@PathVariable String status) {
        List<EmployeeResponse> employees = employeeService.getEmployeesByStatus(status);
        return ResponseEntity.ok(employees);
    }
}

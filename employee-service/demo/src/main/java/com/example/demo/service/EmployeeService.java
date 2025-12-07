package com.example.demo.service;

import com.example.demo.dto.EmployeeCreateRequest;
import com.example.demo.dto.EmployeeResponse;
import com.example.demo.dto.EmployeeUpdateRequest;
import com.example.demo.dto.OrgChartResponse;
import com.example.demo.entity.Employee;
import com.example.demo.exception.EmployeeNotFoundException;
import com.example.demo.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@Service
@RequiredArgsConstructor
@Transactional(readOnly = true)
public class EmployeeService {

    private final EmployeeRepository employeeRepository;

    /**
     * 직원 생성
     */
    @Transactional
    public Long createEmployee(EmployeeCreateRequest request) {
        Employee employee = Employee.builder()
                .name(request.getName())
                .department(request.getDepartment())
                .position(request.getPosition())
                .email(request.getEmail())
                .phoneNumber(request.getPhoneNumber())
                .hireDate(request.getHireDate())
                .birthDate(request.getBirthDate())
                .address(request.getAddress())
                .emergencyContact(request.getEmergencyContact())
                .status(request.getStatus() != null ? request.getStatus() : "ACTIVE")
                .profileImageUrl(request.getProfileImageUrl())
                .managerId(request.getManagerId())
                .level(request.getLevel())
                .build();

        Employee savedEmployee = employeeRepository.save(employee);
        return savedEmployee.getId();
    }

    /**
     * 전체 직원 목록 조회 (필터링 지원)
     */
    public List<EmployeeResponse> getEmployees(String department, String position) {
        List<Employee> employees;

        if (department != null && position != null) {
            employees = employeeRepository.findByDepartmentAndPosition(department, position);
        } else if (department != null) {
            employees = employeeRepository.findByDepartment(department);
        } else if (position != null) {
            employees = employeeRepository.findByPosition(position);
        } else {
            employees = employeeRepository.findAll();
        }

        return employees.stream()
                .map(EmployeeResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 직원 상세 조회
     */
    public EmployeeResponse getEmployee(Long id) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));
        return EmployeeResponse.from(employee);
    }

    /**
     * 직원 존재 여부 확인
     */
    public boolean existsEmployee(Long id) {
        return employeeRepository.existsById(id);
    }

    /**
     * 직원 정보 수정 (department, position만 수정 가능)
     */
    @Transactional
    public EmployeeResponse updateEmployee(Long id, EmployeeUpdateRequest request) {
        Employee employee = employeeRepository.findById(id)
                .orElseThrow(() -> new EmployeeNotFoundException(id));

        if (request.getDepartment() != null && !request.getDepartment().isBlank()) {
            employee.setDepartment(request.getDepartment());
        }
        if (request.getPosition() != null && !request.getPosition().isBlank()) {
            employee.setPosition(request.getPosition());
        }
        if (request.getEmail() != null) {
            employee.setEmail(request.getEmail());
        }
        if (request.getPhoneNumber() != null) {
            employee.setPhoneNumber(request.getPhoneNumber());
        }
        if (request.getHireDate() != null) {
            employee.setHireDate(request.getHireDate());
        }
        if (request.getBirthDate() != null) {
            employee.setBirthDate(request.getBirthDate());
        }
        if (request.getAddress() != null) {
            employee.setAddress(request.getAddress());
        }
        if (request.getEmergencyContact() != null) {
            employee.setEmergencyContact(request.getEmergencyContact());
        }
        if (request.getStatus() != null) {
            employee.setStatus(request.getStatus());
        }
        if (request.getProfileImageUrl() != null) {
            employee.setProfileImageUrl(request.getProfileImageUrl());
        }
        if (request.getManagerId() != null) {
            employee.setManagerId(request.getManagerId());
        }
        if (request.getLevel() != null) {
            employee.setLevel(request.getLevel());
        }

        return EmployeeResponse.from(employee);
    }

    /**
     * 직원 삭제
     */
    @Transactional
    public void deleteEmployee(Long id) {
        if (!employeeRepository.existsById(id)) {
            throw new EmployeeNotFoundException(id);
        }
        employeeRepository.deleteById(id);
    }

    // ==================== 조직도 관련 메서드 ====================

    /**
     * 전체 조직도 조회 (최상위부터 Tree 구조)
     */
    public List<OrgChartResponse> getOrgChart() {
        log.info("전체 조직도 조회 시작");
        
        // 최상위 직원들 조회 (managerId가 null인 직원들)
        List<Employee> topLevelEmployees = employeeRepository.findByManagerIdIsNull();
        
        return topLevelEmployees.stream()
                .map(this::buildOrgChartRecursive)
                .collect(Collectors.toList());
    }

    /**
     * 특정 직원을 루트로 하는 조직도 조회
     */
    public OrgChartResponse getOrgChartByRoot(Long employeeId) {
        log.info("조직도 조회: rootEmployeeId={}", employeeId);
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        
        return buildOrgChartRecursive(employee);
    }

    /**
     * 재귀적으로 조직도 Tree 구조 생성
     */
    private OrgChartResponse buildOrgChartRecursive(Employee employee) {
        OrgChartResponse response = OrgChartResponse.from(employee);
        
        // 부하 직원들 조회
        List<Employee> subordinates = employeeRepository.findByManagerId(employee.getId());
        
        // 재귀적으로 부하 직원들의 조직도 생성
        List<OrgChartResponse> subordinateResponses = subordinates.stream()
                .map(this::buildOrgChartRecursive)
                .collect(Collectors.toList());
        
        response.setSubordinates(subordinateResponses);
        
        return response;
    }

    /**
     * 특정 직원의 부하 직원 목록 조회
     */
    public List<EmployeeResponse> getSubordinates(Long managerId) {
        log.info("부하 직원 조회: managerId={}", managerId);
        
        if (!employeeRepository.existsById(managerId)) {
            throw new EmployeeNotFoundException(managerId);
        }
        
        List<Employee> subordinates = employeeRepository.findByManagerId(managerId);
        
        return subordinates.stream()
                .map(EmployeeResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 특정 직원의 상급자 조회
     */
    public EmployeeResponse getManager(Long employeeId) {
        log.info("상급자 조회: employeeId={}", employeeId);
        
        Employee employee = employeeRepository.findById(employeeId)
                .orElseThrow(() -> new EmployeeNotFoundException(employeeId));
        
        if (employee.getManagerId() == null) {
            throw new IllegalStateException("해당 직원은 최상위 직원입니다 (상급자 없음)");
        }
        
        Employee manager = employeeRepository.findById(employee.getManagerId())
                .orElseThrow(() -> new EmployeeNotFoundException(employee.getManagerId()));
        
        return EmployeeResponse.from(manager);
    }

    /**
     * 부서별 직원 목록 조회
     */
    public List<EmployeeResponse> getEmployeesByDepartment(String department) {
        log.info("부서별 직원 조회: department={}", department);
        
        List<Employee> employees = employeeRepository.findByDepartment(department);
        
        return employees.stream()
                .map(EmployeeResponse::from)
                .collect(Collectors.toList());
    }

    /**
     * 상태별 직원 목록 조회
     */
    public List<EmployeeResponse> getEmployeesByStatus(String status) {
        log.info("상태별 직원 조회: status={}", status);
        
        List<Employee> employees = employeeRepository.findByStatus(status);
        
        return employees.stream()
                .map(EmployeeResponse::from)
                .collect(Collectors.toList());
    }
}

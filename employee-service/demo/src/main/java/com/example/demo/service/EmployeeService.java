package com.example.demo.service;

import com.example.demo.dto.EmployeeCreateRequest;
import com.example.demo.dto.EmployeeResponse;
import com.example.demo.dto.EmployeeUpdateRequest;
import com.example.demo.entity.Employee;
import com.example.demo.exception.EmployeeNotFoundException;
import com.example.demo.repository.EmployeeRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;
import java.util.stream.Collectors;

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
}

package com.example.demo.repository;

import java.util.List;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import com.example.demo.entity.Employee;

@Repository
public interface EmployeeRepository extends JpaRepository<Employee, Long> {

    // 부서별 직원 조회
    List<Employee> findByDepartment(String department);

    // 직책별 직원 조회
    List<Employee> findByPosition(String position);

    // 부서 및 직책별 직원 조회
    List<Employee> findByDepartmentAndPosition(String department, String position);
}

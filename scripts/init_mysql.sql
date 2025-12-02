-- ERP Employee Service Database Initialization Script

-- Create database if not exists
CREATE DATABASE IF NOT EXISTS erp_employee;
USE erp_employee;

-- Drop table if exists
DROP TABLE IF EXISTS employees;

-- Create employees table
CREATE TABLE employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    position VARCHAR(100) NOT NULL,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP
);

-- Insert sample data
INSERT INTO employees (name, department, position) VALUES
('Kim', 'HR', 'Manager'),
('Lee', 'IT', 'Developer'),
('Park', 'Finance', 'Accountant'),
('Choi', 'HR', 'Staff'),
('Jung', 'IT', 'Manager'),
('Kang', 'Finance', 'Director'),
('Yoon', 'IT', 'Developer');

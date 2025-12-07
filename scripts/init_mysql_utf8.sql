-- ERP Employee Service Database Initialization Script

-- Create database if not exists with UTF-8 charset
CREATE DATABASE IF NOT EXISTS erp_employee CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;
USE erp_employee;

-- Drop table if exists
DROP TABLE IF EXISTS employees;

-- Create employees table with extended profile fields and UTF-8 charset
CREATE TABLE employees (
    id BIGINT PRIMARY KEY AUTO_INCREMENT,
    name VARCHAR(100) NOT NULL,
    department VARCHAR(100) NOT NULL,
    position VARCHAR(100) NOT NULL,
    email VARCHAR(100),
    phone_number VARCHAR(20),
    hire_date DATE,
    birth_date DATE,
    address VARCHAR(200),
    emergency_contact VARCHAR(100),
    status VARCHAR(20) DEFAULT 'ACTIVE',
    profile_image_url VARCHAR(500),
    manager_id BIGINT,
    level INT,
    created_at DATETIME DEFAULT CURRENT_TIMESTAMP,
    FOREIGN KEY (manager_id) REFERENCES employees(id) ON DELETE SET NULL
) CHARACTER SET utf8mb4 COLLATE utf8mb4_unicode_ci;

-- Insert sample data (30 employees with organizational hierarchy)
-- Level 1: CEO
INSERT INTO employees (name, department, position, email, phone_number, hire_date, birth_date, address, emergency_contact, status, manager_id, level) VALUES
('김대표', '경영진', '대표이사', 'ceo.kim@company.com', '010-1000-0001', '2015-01-01', '1975-03-15', '서울시 강남구 테헤란로 123', '010-1000-0002', 'ACTIVE', NULL, 1);

-- Level 2: 임원급 (CTO, CFO, CHRO)
INSERT INTO employees (name, department, position, email, phone_number, hire_date, birth_date, address, emergency_contact, status, manager_id, level) VALUES
('박CTO', '기술본부', '기술이사', 'cto.park@company.com', '010-2000-0001', '2016-03-01', '1978-06-20', '서울시 서초구 서초대로 456', '010-2000-0002', 'ACTIVE', 1, 2),
('이CFO', '재무본부', '재무이사', 'cfo.lee@company.com', '010-2000-0003', '2016-05-01', '1976-09-10', '서울시 송파구 올림픽로 789', '010-2000-0004', 'ACTIVE', 1, 2),
('최CHRO', '인사본부', '인사이사', 'chro.choi@company.com', '010-2000-0005', '2017-01-01', '1980-11-25', '서울시 마포구 월드컵로 321', '010-2000-0006', 'ACTIVE', 1, 2);

-- Level 3: 팀장급
INSERT INTO employees (name, department, position, email, phone_number, hire_date, birth_date, address, emergency_contact, status, manager_id, level) VALUES
-- 기술본부 산하
('정백엔드팀장', '개발팀', '백엔드팀장', 'backend.jung@company.com', '010-3000-0001', '2018-02-01', '1985-04-12', '서울시 강남구 역삼로 111', '010-3000-0002', 'ACTIVE', 2, 3),
('강프론트팀장', '개발팀', '프론트팀장', 'frontend.kang@company.com', '010-3000-0003', '2018-03-01', '1986-07-18', '서울시 서초구 반포대로 222', '010-3000-0004', 'ACTIVE', 2, 3),
('윤인프라팀장', '인프라팀', '인프라팀장', 'infra.yoon@company.com', '010-3000-0005', '2018-06-01', '1984-02-28', '서울시 송파구 송파대로 333', '010-3000-0006', 'ACTIVE', 2, 3),
-- 재무본부 산하
('한회계팀장', '회계팀', '회계팀장', 'accounting.han@company.com', '010-3000-0007', '2019-01-01', '1983-05-22', '서울시 영등포구 여의대로 444', '010-3000-0008', 'ACTIVE', 3, 3),
('신재무팀장', '재무팀', '재무팀장', 'finance.shin@company.com', '010-3000-0009', '2019-03-01', '1987-08-14', '서울시 강남구 테헤란로 555', '010-3000-0010', 'ACTIVE', 3, 3),
-- 인사본부 산하
('조인사팀장', '인사팀', '인사팀장', 'hr.jo@company.com', '010-3000-0011', '2019-05-01', '1988-12-03', '서울시 마포구 마포대로 666', '010-3000-0012', 'ACTIVE', 4, 3),
('오총무팀장', '총무팀', '총무팀장', 'admin.oh@company.com', '010-3000-0013', '2019-07-01', '1982-01-30', '서울시 용산구 한강대로 777', '010-3000-0014', 'ACTIVE', 4, 3);

-- Level 4: 선임급
INSERT INTO employees (name, department, position, email, phone_number, hire_date, birth_date, address, emergency_contact, status, manager_id, level) VALUES
-- 백엔드팀
('김백엔드선임', '개발팀', '백엔드 선임개발자', 'backend1.kim@company.com', '010-4000-0001', '2020-01-15', '1990-03-08', '서울시 강남구 논현로 101', '010-4000-0002', 'ACTIVE', 5, 4),
('박백엔드선임', '개발팀', '백엔드 선임개발자', 'backend2.park@company.com', '010-4000-0003', '2020-02-01', '1991-06-17', '서울시 서초구 서초중앙로 102', '010-4000-0004', 'ACTIVE', 5, 4),
-- 프론트팀
('이프론트선임', '개발팀', '프론트 선임개발자', 'frontend1.lee@company.com', '010-4000-0005', '2020-03-01', '1989-09-25', '서울시 송파구 백제고분로 103', '010-4000-0006', 'ACTIVE', 6, 4),
('최프론트선임', '개발팀', '프론트 선임개발자', 'frontend2.choi@company.com', '010-4000-0007', '2020-04-01', '1992-11-11', '서울시 강동구 천호대로 104', '010-4000-0008', 'ACTIVE', 6, 4),
-- 인프라팀
('정인프라선임', '인프라팀', '인프라 선임엔지니어', 'infra1.jung@company.com', '010-4000-0009', '2020-05-01', '1988-02-14', '서울시 마포구 공덕로 105', '010-4000-0010', 'ACTIVE', 7, 4),
-- 회계팀
('강회계선임', '회계팀', '회계 선임', 'acc1.kang@company.com', '010-4000-0011', '2020-06-01', '1990-07-07', '서울시 영등포구 국회대로 106', '010-4000-0012', 'ACTIVE', 8, 4),
-- 재무팀
('윤재무선임', '재무팀', '재무 선임', 'fin1.yoon@company.com', '010-4000-0013', '2020-07-01', '1991-10-20', '서울시 서초구 효령로 107', '010-4000-0014', 'ACTIVE', 9, 4),
-- 인사팀
('한인사선임', '인사팀', '인사 선임', 'hr1.han@company.com', '010-4000-0015', '2020-08-01', '1989-12-25', '서울시 용산구 이촌로 108', '010-4000-0016', 'ACTIVE', 10, 4);

-- Level 5: 사원급
INSERT INTO employees (name, department, position, email, phone_number, hire_date, birth_date, address, emergency_contact, status, manager_id, level) VALUES
-- 백엔드팀
('신백엔드사원', '개발팀', '백엔드 개발자', 'backend.shin@company.com', '010-5000-0001', '2022-01-10', '1995-04-05', '서울시 강남구 봉은사로 201', '010-5000-0002', 'ACTIVE', 5, 5),
('조백엔드사원', '개발팀', '백엔드 개발자', 'backend.jo@company.com', '010-5000-0003', '2022-03-01', '1996-08-15', '서울시 서초구 방배로 202', '010-5000-0004', 'ACTIVE', 5, 5),
('오백엔드사원', '개발팀', '백엔드 개발자', 'backend.oh@company.com', '010-5000-0005', '2023-01-15', '1997-01-22', '서울시 송파구 잠실로 203', '010-5000-0006', 'ACTIVE', 5, 5),
-- 프론트팀
('서프론트사원', '개발팀', '프론트 개발자', 'frontend.seo@company.com', '010-5000-0007', '2022-02-01', '1995-06-10', '서울시 강동구 올림픽로 204', '010-5000-0008', 'ACTIVE', 6, 5),
('권프론트사원', '개발팀', '프론트 개발자', 'frontend.kwon@company.com', '010-5000-0009', '2023-02-01', '1996-09-18', '서울시 마포구 독막로 205', '010-5000-0010', 'ACTIVE', 6, 5),
-- 인프라팀
('임인프라사원', '인프라팀', '인프라 엔지니어', 'infra.lim@company.com', '010-5000-0011', '2022-06-01', '1994-11-30', '서울시 영등포구 당산로 206', '010-5000-0012', 'ACTIVE', 7, 5),
-- 회계팀
('배회계사원', '회계팀', '회계 담당자', 'acc.bae@company.com', '010-5000-0013', '2022-07-01', '1995-03-12', '서울시 서초구 남부순환로 207', '010-5000-0014', 'ACTIVE', 8, 5),
-- 재무팀
('홍재무사원', '재무팀', '재무 담당자', 'fin.hong@company.com', '010-5000-0015', '2023-03-01', '1996-05-28', '서울시 용산구 한강로 208', '010-5000-0016', 'ACTIVE', 9, 5),
-- 인사팀
('송인사사원', '인사팀', '인사 담당자', 'hr.song@company.com', '010-5000-0017', '2022-09-01', '1995-07-14', '서울시 강남구 삼성로 209', '010-5000-0018', 'ACTIVE', 10, 5),
('문총무사원', '총무팀', '총무 담당자', 'admin.moon@company.com', '010-5000-0019', '2023-04-01', '1997-02-20', '서울시 마포구 토정로 210', '010-5000-0020', 'ACTIVE', 11, 5);

-- 휴직/퇴사 직원 예시
INSERT INTO employees (name, department, position, email, phone_number, hire_date, birth_date, address, emergency_contact, status, manager_id, level) VALUES
('양휴직사원', '개발팀', '백엔드 개발자', 'onleave.yang@company.com', '010-6000-0001', '2021-05-01', '1993-10-05', '서울시 서초구 강남대로 301', '010-6000-0002', 'ON_LEAVE', 5, 5),
('류퇴사자', '재무팀', '재무 담당자', 'resigned.ryu@company.com', '010-6000-0003', '2020-03-01', '1992-12-18', '서울시 송파구 석촌호수로 302', '010-6000-0004', 'RESIGNED', 9, 5);

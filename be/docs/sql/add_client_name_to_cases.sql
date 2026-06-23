-- Thêm trường client_name vào bảng legal_cases
ALTER TABLE legal_cases
ADD COLUMN client_name VARCHAR(255) DEFAULT NULL;

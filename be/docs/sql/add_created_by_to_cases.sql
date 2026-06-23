-- Thêm trường created_by và creator_name vào bảng legal_cases
ALTER TABLE legal_cases
ADD COLUMN created_by BIGINT DEFAULT NULL,
ADD COLUMN creator_name VARCHAR(255) DEFAULT NULL;

-- Cập nhật dữ liệu cũ: Lấy tên và id của assigned_lawyer_id hiện tại gán ngược lại cho created_by
-- Vì trước đây assigned_lawyer cũng đóng vai trò là người tạo.
UPDATE legal_cases lc
JOIN users u ON lc.assigned_lawyer_id = u.id
SET lc.created_by = u.id, lc.creator_name = u.full_name;

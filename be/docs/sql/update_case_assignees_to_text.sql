DELIMITER $$

CREATE PROCEDURE DropForeignKeyIfExist(
    IN p_tableName VARCHAR(64),
    IN p_columnName VARCHAR(64)
)
BEGIN
    DECLARE fk_name VARCHAR(64);
    
    -- Tìm tên foreign key
    SELECT CONSTRAINT_NAME INTO fk_name
    FROM information_schema.KEY_COLUMN_USAGE
    WHERE TABLE_NAME = p_tableName
      AND COLUMN_NAME = p_columnName
      AND TABLE_SCHEMA = DATABASE()
      AND REFERENCED_TABLE_NAME IS NOT NULL
    LIMIT 1;
    
    -- Nếu tìm thấy thì thực hiện lệnh xoá
    IF fk_name IS NOT NULL THEN
        SET @s = CONCAT('ALTER TABLE ', p_tableName, ' DROP FOREIGN KEY ', fk_name);
        PREPARE stmt FROM @s;
        EXECUTE stmt;
        DEALLOCATE PREPARE stmt;
    END IF;
END $$

DELIMITER ;

-- Gọi hàm để xoá các khoá ngoại tự động (không cần biết tên ngẫu nhiên của Hibernate)
CALL DropForeignKeyIfExist('legal_cases', 'partner_id');
CALL DropForeignKeyIfExist('legal_cases', 'intern_lawyer_id');
CALL DropForeignKeyIfExist('legal_cases', 'trainee_id');

-- Xoá hàm sau khi dùng xong
DROP PROCEDURE DropForeignKeyIfExist;

-- Cuối cùng, đổi tên cột và kiểu dữ liệu
ALTER TABLE legal_cases 
    CHANGE COLUMN partner_id partner_name VARCHAR(255) DEFAULT NULL,
    CHANGE COLUMN intern_lawyer_id intern_lawyer_name VARCHAR(255) DEFAULT NULL,
    CHANGE COLUMN trainee_id trainee_name VARCHAR(255) DEFAULT NULL;

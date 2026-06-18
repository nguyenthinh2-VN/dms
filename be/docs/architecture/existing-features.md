# Danh sách các tính năng hiện tại (Existing Features)

Hệ thống Document Management System (DMS) được thiết kế xoay quanh nghiệp vụ pháp lý, quản lý hợp đồng và phân quyền động.

## 1. Module Authentication & Authorization (Phân quyền động)

- **Authentication:** Đăng nhập, sinh JWT Token.
- **Dynamic RBAC (Role-Based Access Control):** 
  - Thay vì hardcode kiểm tra `ROLE_ADMIN`, hệ thống sử dụng `PermissionChecker` để kiểm tra quyền ở mức độ chi tiết (hạt mịn - fine-grained) thông qua các `permission.code` như `contract.create`, `user.list`.
  - Hỗ trợ gán permission mặc định cho các Role: `SUPER_ADMIN`, `ADMIN`, `PARTNER`, `LAWYER`, `INTERN_LAWYER`, `TRAINEE`.
  - Quản trị viên có thể gán/thu hồi quyền cá nhân hóa cho từng user thông qua bảng `rules`.

## 2. Module Quản lý Mẫu Hợp đồng (Contract Template)

- **Upload & Quản lý:** Tải lên các file `.docx` chứa các biến đánh dấu (ví dụ: `{{CUSTOMER_NAME}}`).
- **Tự động bóc tách (Field Extraction):** Hệ thống tự động phân tích file DOCX để tìm các field cần điền và lưu thành cấu trúc schema động (Metadata).
- **Versioning:** Hỗ trợ lưu trữ nhiều phiên bản (version) cho cùng một mẫu hợp đồng.
- **Workflow:** Có các trạng thái (DRAFT, ACTIVE, ARCHIVED).

## 3. Module Sinh và Quản lý Hợp đồng (Contract Generation)

- **Dynamic Form Data:** Nhận dữ liệu do người dùng nhập từ form động (dựa trên schema của Template).
- **Preview Hợp đồng (Xem trước):** Merge dữ liệu vào file DOCX mẫu và convert sang HTML để hiển thị trực tiếp trên trình duyệt mà không cần tải file về.
- **Create & Finalize:**
  - Merge dữ liệu an toàn vào cấu trúc XML của file DOCX (Tránh lỗi thẻ XML nhờ `XmlUtils` và regex chống đứt gãy thẻ `w:t`).
  - Tự động convert sang định dạng PDF sử dụng `XDocReport` (Tối ưu tốc độ, khắc phục hoàn toàn lỗi Font/NullPointer của Apache FOP).
  - Tự động lưu trữ cả bản DOCX và bản PDF vào ổ đĩa thông qua `ContractFileStorage`.
- **Download:** Cung cấp API tải xuống file DOCX và PDF chính xác với HTTP Header `Content-Disposition: attachment`.
- **Liên kết nghiệp vụ:** Cho phép đính kèm hợp đồng vào một "Vụ việc" (Legal Case) thông qua `legalCaseId`.

## 4. Module Quản lý File (Storage)

- **ContractFileStorage:** Service chuyên biệt xử lý việc ghi/đọc/xóa file trên ổ cứng vật lý.
- Tương lai có thể dễ dàng cắm (plug) Amazon S3 hoặc MinIO nhờ cấu trúc Hexagonal Architecture.

## 5. Module Quản lý Vụ việc (Legal Case) - (Đang hoàn thiện)

- Cung cấp API lấy danh sách vụ việc để gắn vào Hợp đồng.
- Phân quyền theo scope: Xem danh sách toàn bộ vụ việc (Admin/Manager) hoặc Xem danh sách vụ việc được phân công (Lawyer/Trainee).

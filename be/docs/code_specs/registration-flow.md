# Registration & Login Flow

Tài liệu này lưu trữ các luồng logic kỹ thuật cho tính năng xác thực và quản lý tài khoản.

## 1. Đăng ký (Registration Flow)
- **Validation Input**:
  - `phoneNumber`: Kiểm tra pattern `^0\d{1,14}$` (bắt đầu bằng 0, tối đa 15 ký tự).
  - `password` & `confirmPassword`: Phải khớp nhau.
  - `workEmail`: Kiểm tra định dạng email hợp lệ.
  - `role`: Validate ngay tại Backend. Chỉ chấp nhận `Luật sư`, `Luật sư thực tập`, `Thực tập sinh`. Nếu FE gửi lên `super_admin` hoặc `admin`, trả về lỗi (400 Bad Request / 403 Forbidden).

- **Kiểm tra trùng lặp**:
  - Truy vấn `email` hoặc `phoneNumber` xem đã tồn tại chưa. Nếu có ném `UserAlreadyExistsException`.

- **Hash Password**:
  - Sử dụng `BCryptPasswordEncoder` để mã hóa mật khẩu trước khi lưu.

- **Persist (Lưu trữ)**:
  - Khởi tạo Domain Entity `User`.
  - Application gọi interface `UserRepository.save(user)`.
  - Infrastructure sẽ map `User` sang `UserJpaEntity` và gọi hàm `save` của Spring Data JPA.

## 2. Đăng nhập (Login Flow)
- **Xác thực**:
  - Tìm `User` bằng `email` (hoặc `phoneNumber`).
  - Nếu tồn tại, kiểm tra `password` (bằng `BCryptPasswordEncoder.matches()`).
  - Nếu sai trả về `401 Unauthorized`.

- **Tạo JWT Token**:
  - Tạo `Access Token` với payload bao gồm `userId`, `email`, `role`.
  - Trả về JSON chứa Token cho phía Frontend.

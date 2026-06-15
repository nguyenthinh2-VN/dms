# Áp dụng SOLID Principles

Dự án này tuân thủ các nguyên lý SOLID nhằm duy trì mã nguồn dễ mở rộng, dễ kiểm thử và tránh "code smell". Dưới đây là giải thích về cách áp dụng từng nguyên lý trong dự án.

## 1. S - Single Responsibility Principle (SRP)
**Nguyên lý:** "Mỗi file chỉ có 1 lý do để thay đổi. Không được viết nhiều chức năng hỗn hợp trong cùng 1 file".

**Trong dự án:**
- `AuthController`: Chỉ chịu trách nhiệm tiếp nhận request HTTP, validate format cơ bản, và trả về HTTP Response.
- `RegisterUseCase`: Chỉ chứa logic nghiệp vụ đăng ký (ví dụ kiểm tra số điện thoại, check trùng email, băm mật khẩu). Nếu logic HTTP thay đổi, file này không bị ảnh hưởng.
- `JpaUserRepositoryImpl`: Chỉ chịu trách nhiệm map dữ liệu giữa Domain Entity (`User`) và JPA Entity (`UserJpaEntity`) để giao tiếp với DB.

## 2. O - Open/Closed Principle (OCP)
**Nguyên lý:** "Mở cho việc mở rộng, đóng cho việc sửa đổi". Bạn có thể thêm tính năng mới mà không cần sửa code cũ.

**Trong dự án:**
- Tầng Application định nghĩa interface `PasswordEncoderPort`.
- Hiện tại, hạ tầng sử dụng `BCryptPasswordEncoderAdapter` để băm mật khẩu. 
- Sau này nếu muốn đổi sang thuật toán `Argon2` hay `PBKDF2`, ta chỉ cần tạo một class adapter mới implement `PasswordEncoderPort` và tiêm (inject) vào cấu hình, mà **không cần sửa bất kỳ dòng code nào** trong `RegisterUseCase` hay `LoginUseCase`.

## 3. I - Interface Segregation Principle (ISP)
**Nguyên lý:** "Không nên ép buộc một class phải implement những phương thức mà nó không sử dụng. Thay vì dùng 1 interface khổng lồ, hãy chia thành nhiều interface nhỏ".

**Trong dự án:**
- Thay vì tạo một interface `DatabaseRepository` chứa tất cả các hàm CRUD cho User, Role, Permission... ta tách nhỏ thành:
  - `UserRepository` (chỉ quản lý User)
  - `RoleRepository` (chỉ quản lý Role)
  - `PermissionRepository` (chỉ quản lý Permission)
  - `RuleRepository` (chỉ quản lý Rule)
- Việc chia nhỏ giúp các UseCase chỉ phụ thuộc vào đúng những interface mà nó thực sự cần gọi.

## 4. D - Dependency Inversion Principle (DIP)
**Nguyên lý:** "Các module cấp cao (như Application/Domain) không nên phụ thuộc vào các module cấp thấp (như Infrastructure/Database). Cả hai nên phụ thuộc vào abstractions (Interface)".

**Trong dự án:**
- `RegisterUseCase` (module cấp cao) không khởi tạo trực tiếp `new JpaUserRepositoryImpl()` (module cấp thấp).
- Thay vào đó, nó nhận vào thông qua constructor interface `UserRepository`.
- Framework (Spring Boot) sẽ tự động khởi tạo `JpaUserRepositoryImpl` và **tiêm (Dependency Injection - DI)** vào `RegisterUseCase` khi hệ thống chạy thông qua `@Bean` trong `UseCaseConfig`.

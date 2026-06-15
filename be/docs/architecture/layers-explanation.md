# Chức năng của từng tầng (Layers Explanation)

Dự án được chia thành 4 tầng chính:

## 1. Domain Layer (`domain`)
- **Vai trò:** Chứa Core Business Logic, bao gồm các cấu trúc dữ liệu cốt lõi nhất.
- **Nội dung:**
  - `entity`: Cấu trúc POJO, ví dụ `User`, `Role`. KHÔNG gắn annotation JPA (`@Entity`, `@Table`) ở đây.
  - `valueobject`: Đối tượng định danh bằng giá trị.
  - `exception`: Các Business Exception (ví dụ: `UserAlreadyExistsException`).
  - `repository`: Các interface kho chứa dữ liệu, định nghĩa các hợp đồng (contract) mà Infrastructure phải tuân theo.
- **Quy tắc:** Tuyệt đối không import thư viện Framework (Spring) ngoại trừ một số chuẩn cấu trúc dữ liệu Java.

## 2. Application Layer (`application`)
- **Vai trò:** Điều phối luồng xử lý của hệ thống (Use Cases).
- **Nội dung:**
  - `usecase`: Các file chứa logic cho mỗi Use Case (VD: `RegisterUseCase`, `LoginUseCase`).
  - `dto`: Các Request/Response models dùng để giao tiếp với Presentation (VD: `RegisterRequest`, `LoginRequest`).
  - `port`: Input/Output ports nếu có.
- **Quy tắc:** Phụ thuộc vào `Domain`, không biết về `Infrastructure` (Database) hay `Presentation` (HTTP).

## 3. Infrastructure Layer (`infrastructure`)
- **Vai trò:** Cung cấp hạ tầng, công cụ cho hệ thống (DB, Network, Framework).
- **Nội dung:**
  - `persistence`: Chứa `JpaEntities` (VD: `UserJpaEntity` gắn `@Table`), Spring Data `Repositories` và các classes implement `domain.repository` Interfaces.
  - `security`: Chứa cấu hình JWT, PasswordEncoder.
  - `config`: Cấu hình Framework nói chung.
- **Quy tắc:** Implement các interface được định nghĩa ở `Domain` và `Application`.

## 4. Presentation Layer (`presentation`)
- **Vai trò:** Tương tác với người dùng hoặc hệ thống bên ngoài (REST API).
- **Nội dung:**
  - `controller`: Chứa các REST Controllers (VD: `AuthController`).
  - `exception`: Chứa GlobalExceptionHandler (`@ControllerAdvice`) để map Exception sang mã lỗi HTTP.
- **Quy tắc:** Dùng DTOs để nhận dữ liệu, gọi Application `UseCase`, trả về DTOs hoặc chuẩn JSON. Không được chứa logic nghiệp vụ.

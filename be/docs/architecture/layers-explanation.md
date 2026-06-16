# Chức năng của từng tầng (Layers Explanation)

Dự án được chia thành 4 tầng chính:

## 1. Domain Layer (`domain`)
- **Vai trò:** Chứa Core Business Logic, bao gồm các cấu trúc dữ liệu cốt lõi nhất.
- **Nội dung:**
  - `entity`: Cấu trúc POJO, Enum, ví dụ `User`, `Role`, `CaseCategory`, `CaseStatus`. KHÔNG gắn annotation JPA (`@Entity`, `@Table`) ở đây.
  - `valueobject`: Đối tượng định danh bằng giá trị.
  - `exception`: Các Business Exception (ví dụ: `UserAlreadyExistsException`, `InvalidRoleException`).
  - `repository`: Các interface định nghĩa hợp đồng (contract) giao tiếp dữ liệu (VD: `LegalCaseRepository`, `UserRepository`).
- **Quy tắc:** Tuyệt đối không import thư viện Framework (Spring) ngoại trừ một số chuẩn cấu trúc dữ liệu Java.

## 2. Application Layer (`application`)
- **Vai trò:** Điều phối luồng xử lý của hệ thống (Use Cases) và quản lý các tiện ích (Utils).
- **Nội dung:**
  - `usecase`: Các file chứa logic cho mỗi Use Case (VD: `RegisterUseCase`, `CaseUseCase`, `GetReferralsUseCase`).
  - `dto`: Các Request/Response models dùng để giao tiếp với Presentation (VD: `RegisterRequest`, `CaseResponse`, `AuthResponse`).
  - `util`: Chứa các tiện ích mức logic (VD: `LanguageContextHolder`, `MessageUtils` xử lý i18n).
  - `port`: Input/Output ports nếu có.
- **Quy tắc:** Phụ thuộc vào `Domain`, không biết về `Infrastructure` (Database) hay `Presentation` (HTTP).

## 3. Infrastructure Layer (`infrastructure`)
- **Vai trò:** Cung cấp hạ tầng, công cụ cho hệ thống (DB, Network, Framework).
- **Nội dung:**
  - `persistence`: Chứa `JpaEntities` (VD: `UserJpaEntity`, `LegalCaseJpaEntity` gắn `@Table`), Spring Data `Repositories` và các classes implement `domain.repository` (VD: `JpaLegalCaseRepositoryImpl`).
  - `security`: Chứa cấu hình JWT (Token filter, TokenProvider), WebSecurityConfig.
  - `config`: Cấu hình Framework nói chung (VD: `WebMvcConfig`, `RateLimitInterceptor`, `LanguageInterceptor`).
- **Quy tắc:** Implement các interface được định nghĩa ở `Domain` và `Application`.

## 4. Presentation Layer (`presentation`)
- **Vai trò:** Tương tác với người dùng hoặc hệ thống bên ngoài (REST API).
- **Nội dung:**
  - `controller`: Chứa các REST Controllers (VD: `AuthController`, `CaseController`, `UserController`). Đảm nhận nhận request `lan` và gọi `UseCase`.
  - `exception`: Chứa `GlobalExceptionHandler` (`@ControllerAdvice`) để map Exception sang mã lỗi HTTP và dịch Exception sang dạng Message thân thiện thông qua `MessageUtils`.
- **Quy tắc:** Dùng DTOs để nhận dữ liệu, gọi Application `UseCase`, trả về DTOs hoặc chuẩn JSON. Có trả về status HTTP (Ví dụ 200, 201, 400). Không được chứa logic nghiệp vụ.

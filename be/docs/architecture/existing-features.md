# Các chức năng hiện có trong hệ thống (Features Overview)

Tài liệu này tổng hợp các tính năng và module đã được hoàn thiện trong hệ thống (Backend).

## 1. Module Xác thực & Người dùng (Authentication & Users)
- **Đăng ký (Register):** Đăng ký tài khoản người dùng mới kèm theo hệ thống mã giới thiệu (Referral Code). Cho phép chọn các Role (Luật sư, Thực tập sinh, Partner, ...).
- **Đăng nhập (Login):** Trả về JWT Token để truy cập các API yêu cầu xác thực.
- **Hệ thống Referral:** Tự động tạo Referral Code cho mỗi User. Hỗ trợ API `GET /api/v1/users/me/referrals` để lấy link giới thiệu và danh sách người dùng đã được giới thiệu.
- **Quản lý Người dùng (User Management):** API CRUD cung cấp tính năng xem danh sách người dùng, cập nhật trạng thái hoạt động (Active/Inactive), cập nhật thông tin cá nhân.
- **Phân quyền nâng cao (RBAC):** Hệ thống phân quyền động thông qua `Role`, `Permission`, và `Rule`. Cho phép kiểm tra quyền (`PermissionChecker`) và gán quyền (`assignPermissions`).
- **Danh sách nhân sự (Staff):** Cung cấp API để Frontend lấy danh sách nhân sự phân loại theo chức danh (Lawyer, Partner, Intern...) phục vụ việc assign Vụ việc.

## 2. Module Quản lý Vụ việc (Case Management)
- **Danh mục hệ thống:** API cung cấp danh sách Loại vụ việc (Category) và Trạng thái vụ việc (Status). Hỗ trợ hiển thị text theo đa ngôn ngữ (VI, TW).
- **Tạo Vụ việc:** Lưu thông tin vụ việc, tính toán tỉ lệ % chia hoa hồng cho người phụ trách, đối tác, người giới thiệu, và số tiền thực nhận (Net Value). Tự động gán người tạo làm luật sư xử lý chính và tự động sinh tiêu đề vụ việc.
- **Lấy danh sách Vụ việc:** Phân quyền hiển thị (Admin xem toàn bộ, các role khác chỉ xem vụ việc có liên quan/gắn tên).
- **Xem chi tiết Vụ việc:** Kiểm tra quyền (Authorization) trước khi trả về chi tiết.
- **Cập nhật & Phân công:** Sửa thông tin hoặc phân công thêm nhân sự. Có kiểm tra quyền truy cập.

## 3. Module Biểu mẫu Hợp đồng (Contract Templates)
- **Quản lý Mẫu hợp đồng:** Tạo, cập nhật, lưu trữ file `.docx` mẫu hợp đồng (`ContractTemplateUseCase`). Lưu trữ file qua `LocalContractFileStorage`.
- **Phân tích File Docx:** Tự động phân tích file upload (`DocxPlaceholderExtractor`) để trích xuất các biến (placeholders) và tự động dự đoán loại dữ liệu (Text, Date, Number...).
- **Chuyển đổi Docx sang HTML:** Hỗ trợ render nội dung docx lên giao diện (`DocxToHtmlConverter`).
- **Trường dữ liệu động (Template Fields):** Lưu các cấu hình liên quan đến vị trí cần điền dữ liệu động trong biểu mẫu hợp đồng.

## 4. Hệ thống Cốt lõi & Middleware (Cross-cutting Concerns)
- **Kiến trúc Clean Architecture:** Chia rẽ các phần (Domain, Application, Infrastructure, Presentation), đảm bảo logic cốt lõi hoàn toàn độc lập với Framework và Database.
- **Đa ngôn ngữ (i18n):** Hỗ trợ Header `Accept-Language` và Query Param `?lan=` thông qua `LanguageInterceptor`. `MessageUtils` giúp dịch message tự động (Ví dụ: dịch các lỗi chuẩn và tên Enum sang tiếng Đài Loan).
- **Xử lý Exception tập trung:** `GlobalExceptionHandler` bắt tất cả các lỗi trong hệ thống và chuẩn hóa cấu trúc HTTP Response (Status codes: 400, 401, 403, 500) kết hợp đa ngôn ngữ.
- **Rate Limiting:** Bảo vệ các API (`/api/**`) bằng `RateLimitInterceptor`, sử dụng Bucket4j để chống Spam/DDoS.
- **Spring Security & JWT:** Bảo mật stateless, lọc Request bằng Token. Đã cấu hình phân quyền (Role-based access).

## 5. Tài liệu Kỹ thuật
- **Swagger / API Docs:** Có sẵn trong `docs/api_docs/`.
- **Enum Mapping:** Tài liệu cung cấp cách đọc/hiểu các Enum code và map với Text phía Frontend trong `enum-mapping-docs.md`.
- **Knowledge Graph Wiki:** Tài liệu cấu trúc Code tự động tạo (trong folder `.code-review-graph/wiki`) giúp hiểu rõ dòng chảy (Flow) của hệ thống.

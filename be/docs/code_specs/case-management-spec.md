# Spec: Quản lý vụ việc (Case Management)

## Objective
Xây dựng module quản lý vụ việc cho phép người dùng (Luật sư, Admin, Thực tập sinh,...) tạo và theo dõi các vụ việc pháp lý. Đảm bảo phân quyền chặt chẽ: chỉ những role được cấp phép mới được tạo vụ việc, và mỗi user chỉ được xem các vụ việc mà họ có liên quan (hoặc toàn quyền nếu là Admin).

## Tech Stack
- **Backend:** Spring Boot 4.1.0, Java 17
- **Database:** MySQL (Spring Data JPA)
- **Security:** Spring Security & JWT (Đã cấu hình)

## Commands
- Build: `mvn clean compile`
- Run: `mvn spring-boot:run`

## Project Structure
- `domain/entity/LegalCase.java` → Entity lưu trữ thông tin vụ việc.
- `application/dto/Case...` → Các DTO cho request/response.
- `application/usecase/...` → Chứa logic tạo, lấy danh sách, xem chi tiết.
- `presentation/controller/CaseController.java` → API endpoints.

## Code Style
- Đặt tên biến rõ ràng bằng tiếng Anh.
- Sử dụng `@Builder` để khởi tạo Entity/DTO.
- Trả về response theo chuẩn chung có `status`, `message`, `data`.

## Testing Strategy
- Manual test thông qua API Docs/Postman.
- Đảm bảo logic phân quyền (Admin thấy hết, User thường chỉ thấy của mình) hoạt động đúng.

## Boundaries
- **Always:** Validate giá trị đầu vào (Tiêu đề không rỗng, Giá trị >= 0, % Hoa hồng >= 0).
- **Ask first:** Thay đổi cấu trúc bảng User để phục vụ Case (nếu cần).
- **Never:** Cho phép user tự sửa trường `creator` hoặc đổi `status` khác "Mới" lúc tạo.

## Success Criteria
- [ ] API lấy danh sách User theo Role (Luật sư, Partner, TTS) trả về đúng dữ liệu.
- [ ] API Tạo vụ việc hoạt động: tự động gán Luật sư xử lý là người tạo, sinh Tiêu đề theo format `[Title]_[Type]_[ddMMyy]_[Creator]`, trạng thái mặc định "Mới".
- [ ] API Lấy danh sách vụ việc: Admin thấy toàn bộ. Luật sư xử lý, Partner, TTS chỉ thấy vụ việc có tên mình trong đó.
- [ ] API Xem chi tiết vụ việc: Trả về đầy đủ giá trị, tỷ lệ %, tiền Net (Tính bằng `Giá trị vụ việc - tổng Hoa hồng`). Chỉ những người liên quan hoặc Admin mới được gọi API này. Chặn lỗi 403 nếu cố tình truy cập trái phép.

## Open Questions
*(Xem chi tiết trong Implementation Plan và phản hồi)*

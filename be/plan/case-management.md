# KẾ HOẠCH TỔNG THỂ: QUẢN LÝ VỤ VIỆC (CASE MANAGEMENT)

Tài liệu này tổng hợp toàn bộ Spec (đặc tả) và Kế hoạch triển khai (Implementation Plan) cho tính năng Quản lý vụ việc của hệ thống DMS.

---

## PHẦN 1: TỔNG QUAN (SPECIFICATION)

### 1.1. Mục tiêu (Objective)
Xây dựng module quản lý vụ việc cho phép người dùng (Luật sư, Admin, Thực tập sinh,...) tạo, phân công và theo dõi các vụ việc pháp lý.
Đảm bảo phân quyền chặt chẽ:
- Chỉ những role được cấp phép mới được tạo vụ việc.
- Mỗi user chỉ được xem các vụ việc mà họ có liên quan (trừ Admin được xem toàn quyền).
- Tự động tính toán Hoa hồng và tiền Net.

### 1.2. Công nghệ (Tech Stack)
- **Backend:** Spring Boot 4.1.0, Java 17
- **Database:** MySQL (Spring Data JPA)
- **Security:** Spring Security & JWT (Đã cấu hình từ trước)

### 1.3. Cấu trúc mã nguồn (Project Structure)
- `domain/entity/LegalCase.java`: Entity lưu trữ thông tin vụ việc.
- `application/dto/Case...`: Các DTO cho request/response của Vụ việc.
- `application/usecase/...`: Chứa logic nghiệp vụ Tạo, Xem danh sách, Chi tiết, Cập nhật/Phân công.
- `presentation/controller/CaseController.java`: Nơi định nghĩa các API endpoints cho Frontend.

---

## PHẦN 2: THIẾT KẾ DATABASE (ENTITY)

Tạo Entity `LegalCase` với các trường dữ liệu sau:
- `id` (Long - Tự tăng)
- `generatedTitle` (String) - Tiêu đề tự sinh theo format: `[Tiêu đề]_[Loại]_[ddMMyy]_[Người tạo]`. (Nếu loại vụ việc rỗng thì format là `[Tiêu đề]_[ddMMyy]_[Người tạo]`).
- `title` (String), `type` (String - nullable), `description` (Text - nullable).
- `referrerName` (String - Người giới thiệu, nhập text tự do, không liên kết User DB).
- `assignedLawyer` (ManyToOne -> User) - Người xử lý chính (Mặc định tự động gán là tài khoản tạo vụ việc).
- `partner`, `internLawyer`, `trainee` (ManyToOne -> User) - Những người được phân công.
- `caseValue` (BigDecimal / Double) - Giá trị vụ việc bằng tiền VND.
- `paymentStatus` (Enum) - Trạng thái thanh toán: `UNPAID` (Chưa thu), `PAID` (Đã thu). Lúc tạo mặc định là `UNPAID`.
- `status` (Enum `CaseStatus`) - Trạng thái vụ việc gồm 9 loại: `NEW` (Mới), `WAITING_VERIFICATION` (Đợi xác minh), `VERIFYING` (Đang xác minh), `NEGOTIATING` (Đang chốt hợp đồng), `CONTRACTED` (Đã chốt hợp đồng), `PROCESSING` (Đang xử lý), `PAUSED` (Tạm dừng), `ACCEPTANCE_PAYMENT` (Đang nghiệm thu/thanh toán), `CLOSED` (Đã đóng). Lúc tạo mặc định là `NEW`.
- `% Hoa hồng` (Double): `referrerPercent`, `assignedLawyerPercent`, `partnerPercent`, `internPercent`, `traineePercent`.

---

## PHẦN 3: CÁC API CHI TIẾT SẼ PHÁT TRIỂN

### API 1: Lấy danh sách nhân sự (Nhóm theo Role)
- **Endpoint**: `GET /api/v1/users/staff`
- **Mục đích**: FE lấy dữ liệu để đổ vào Dropdown lúc chọn người phân công.
- **Dữ liệu trả về**: Json dạng Group:
  ```json
  {
    "LAWYER": [{id: 1, name: "A"}],
    "PARTNER": [{id: 2, name: "B"}],
    "INTERN_LAWYER": [{id: 3, name: "C"}],
    "TRAINEE": [{id: 4, name: "D"}]
  }
  ```

### API 2: Lấy cấu hình trạng thái vụ việc
- **Endpoint**: `GET /api/v1/cases/statuses`
- **Mục đích**: Trả về list các trạng thái (Kèm tiếng Việt) để FE làm ô dropdown lúc tạo/chỉnh sửa trạng thái.

### API 3: Tạo vụ việc mới
- **Endpoint**: `POST /api/v1/cases`
- **Quyền thao tác**: Chỉ Role `Admin`, `Luật sư`, `Luật sư thực tập` được quyền tạo.
- **Luồng xử lý**:
  - Gán tự động `assignedLawyer` = tài khoản đang login.
  - Trạng thái mặc định: `status` = `NEW`, `paymentStatus` = `UNPAID`.
  - Hệ thống tự sinh `generatedTitle` theo format.
  - Có thể truyền kèm các tham số % hoa hồng từ FE luôn.

### API 4: Lấy danh sách vụ việc
- **Endpoint**: `GET /api/v1/cases`
- **Quyền thao tác**: 
  - Admin: Lấy toàn bộ.
  - Role khác: Chỉ lấy các vụ việc mà bản thân là `assignedLawyer`, `partner`, `internLawyer` hoặc `trainee` (Những người có liên quan/được phân công).

### API 5: Xem chi tiết vụ việc
- **Endpoint**: `GET /api/v1/cases/{id}`
- **Quyền thao tác**: Chỉ Admin và những người có liên quan tới vụ việc mới được xem (Quy tắc giống API 4). Nếu không liên quan -> Trả về lỗi `403 Forbidden`.
- **Tính toán tự động**: Backend tự động tính tiền VND từ % hoa hồng tương ứng. Tiền Net = `caseValue` - (Tổng các khoản hoa hồng).
- **Sơ đồ vai trò**: Trả thông tin dữ liệu sơ đồ, phần Khách hàng tạm thời để rỗng hoặc NULL theo như thiết kế (đã loại bỏ trường Khách hàng).
- **Hồ sơ vụ việc**: Trả mảng rỗng `files: []` (Chờ phát triển Module quản lý File sau).

### API 6: Sửa vụ việc / Phân công
- **Endpoint**: `PUT /api/v1/cases/{id}`
- **Quyền thao tác**: Chỉ **Admin** và **Người tạo vụ việc (assignedLawyer)** mới được phép Sửa hoặc Phân công người khác. (Các đối tượng được phân công khác như Partner, TTS chỉ có quyền Xem chứ không được sửa vụ việc).
- **Mục đích**: Update thông tin vụ việc, Update `% hoa hồng` cho từng người, Chuyển đổi trạng thái `status` hoặc `paymentStatus`. Khi FE truyền ID vào các trường như `partner_id`, `trainee_id` thì sẽ được Backend ghi nhận như một hành động "Phân công".

---

## KẾT LUẬN VÀ KIỂM TRA
Quy tắc trên đã tuân thủ triệt để:
- Hạn chế quyền (Role-based access control).
- Tách bạch rõ quyền XEM và quyền SỬA/PHÂN CÔNG.
- Tính toán tiền (Net/Commission) 100% được đẩy về Backend xử lý để đảm bảo chính xác dữ liệu.

*Tài liệu này là bản chốt cuối cùng trước khi code.*

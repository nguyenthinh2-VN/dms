# Quản lý Vụ việc (Case Management API Docs)

> **Lưu ý Đa ngôn ngữ (i18n):**
> Tất cả các API đều hỗ trợ tham số ngôn ngữ. Đặc biệt với các API lấy danh mục (Statuses, Categories), việc gửi ngôn ngữ lên sẽ thay đổi text trả về.
> Bạn có thể gửi ngôn ngữ mong muốn qua Query Parameter `?lan=TW` hoặc qua HTTP Header `Accept-Language: TW`. Mặc định nếu không gửi là tiếng Việt (`VI`). Mọi thông báo lỗi (`message`) cũng sẽ được dịch tự động.

## 1. Lấy danh sách nhân sự (Nhóm theo Role)
**Endpoint:** `GET /api/v1/users/staff`

**Mô tả:** Lấy danh sách các nhân sự được phân loại theo mã Role (`LAWYER`, `PARTNER`, `INTERN_LAWYER`, `TRAINEE`). Dùng để render các dropdown chọn người phân công (Partner, Luật sư thực tập, Thực tập sinh).

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "LAWYER": [
      {
        "id": 1,
        "fullName": "Nguyen Van A"
      }
    ],
    "PARTNER": [
      {
        "id": 5,
        "fullName": "Tran Thi B"
      }
    ],
    "INTERN_LAWYER": [],
    "TRAINEE": []
  }
}
```

---

## 2. Lấy danh sách Trạng thái vụ việc
**Endpoint:** `GET /api/v1/cases/statuses` (Hoặc `?lan=TW` để lấy tiếng Đài Loan)

**Mô tả:** Lấy danh sách 9 trạng thái của vụ việc để render dropdown trên UI.

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Thành công",
  "data": [
    {
      "code": "NEW",
      "description": "Mới tiếp nhận"
    },
    {
      "code": "PROCESSING",
      "description": "Đang xử lý"
    }
  ]
}
```

---

## 3. Lấy danh sách Lĩnh vực (Loại vụ việc)
**Endpoint:** `GET /api/v1/cases/categories` (Hoặc `?lan=TW` để lấy tiếng Đài Loan)

**Mô tả:** Lấy danh sách 12 Lĩnh vực pháp lý (Dân sự, Hình sự...) để làm dropdown lúc tạo vụ việc. Cần truyền `code` lên API.

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Thành công",
  "data": [
    {
      "code": "CIVIL",
      "description": "Dân sự"
    },
    {
      "code": "CRIMINAL",
      "description": "Hình sự"
    }
  ]
}
```

---

## 4. Tạo Vụ việc (Create Case)
**Endpoint:** `POST /api/v1/cases`

**Mô tả:** Tạo một vụ việc mới. Chỉ `ADMIN`, `LAWYER`, `INTERN_LAWYER` mới có quyền tạo. Hệ thống tự gán user hiện tại làm người phụ trách (Assigned Lawyer).

**Request Body (`application/json`):**
```json
{
  "title": "Tranh chấp đất đai",
  "category": "OTHER",
  "customCategory": "Tư vấn hợp đồng đặc biệt",
  "description": "Mô tả chi tiết vụ việc...",
  "referrerName": "Ông B",
  "caseValue": 100000000.0,
  "referrerPercent": 10.0,
  "assignedLawyerPercent": 20.0,
  "partnerPercent": 0,
  "internPercent": 0,
  "traineePercent": 0,
  "partnerName": "Ông C",
  "internLawyerName": "Thực tập sinh D",
  "traineeName": "Trainee E",
  "clientName": "Ông F"
}
```

**Giải thích các trường (Field Descriptions) dành cho UI:**
- `title` (Tên vụ việc): Tiêu đề ngắn gọn của vụ việc.
- `category` (Lĩnh vực pháp lý): Bắt buộc, gửi mã `code` (VD: `CIVIL`, `OTHER`) lấy từ API `/categories`.
- `customCategory` (Lĩnh vực pháp lý khác): Text tự do. Nếu `category` là `OTHER` thì FE hiển thị ô này cho người dùng nhập tay. Nếu `category` khác `OTHER`, Frontend có thể truyền `null` hoặc không gửi trường này (Backend sẽ tự động ép thành `null`).
- `description` (Mô tả): Chi tiết nội dung vụ việc.
- `referrerName` (Người giới thiệu): Tên của người hoặc đối tác đã giới thiệu vụ việc này (không bắt buộc, text tự do).
- `clientName` (Khách hàng): Tên của khách hàng (không bắt buộc, text tự do).
- `caseValue` (Giá trị vụ việc): Tổng số tiền thù lao thu được từ vụ việc (VND).
- `referrerPercent` (% Người giới thiệu): Tỉ lệ phần trăm hoa hồng trích lại cho Người giới thiệu.
- `assignedLawyerPercent` (% Người phụ trách): Tỉ lệ phần trăm cho Luật sư phụ trách chính (Người tạo vụ việc).
- `partnerPercent`, `internPercent`, `traineePercent`: Tỉ lệ phần trăm cho Partner, Luật sư thực tập và Thực tập sinh tương ứng.

**Response (200 OK):**
```json
{
  "status": 201,
  "message": "Thành công",
  "data": {
    "id": 1,
    "generatedTitle": "Tranh chấp đất đai_Tư vấn pháp luật_150626_Nguyen Van A",
    "status": "NEW",
    "paymentStatus": "UNPAID",
    "netValue": 70000000.0
    "createdBy": 1,
    "creatorName": "Nguyen Van A"
    // ... trả về chi tiết CaseResponse (Hoa hồng, tiền Net)
  }
}
```

**Lỗi có thể trả về:**
- `403 Forbidden`: `{"status": 403, "message": "Bạn không có quyền truy cập"}`
- `400 Bad Request`: Validation lỗi hoặc sai logic. `{"status": 400, "message": "..."}`

---

## 5. Lấy Danh sách Vụ việc
**Endpoint:** `GET /api/v1/cases`

**Mô tả:** Lấy danh sách vụ việc. Admin và Manager Lawyer xem được toàn bộ. Các Role khác chỉ xem được vụ việc mà mình có liên quan.

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Thành công",
  "data": [
    {
      "id": 1,
      "title": "Tranh chấp đất đai",
      "status": "NEW",
      "referrerName": "Ông B",
      "clientName": "Bà C",
      "assignedLawyer": {
  ]
}
```

---

## 6. Lấy Chi tiết Vụ việc
**Endpoint:** `GET /api/v1/cases/{id}`

**Mô tả:** Lấy chi tiết vụ việc kèm theo số tiền tính toán.

**Lỗi có thể trả về:**
- `403 Forbidden`: `{"status": 403, "message": "Bạn không có quyền truy cập"}`

---

## 7. Cập nhật / Phân công Vụ việc
**Endpoint:** `PUT /api/v1/cases/{id}`

**Mô tả:** Sửa thông tin vụ việc hoặc gán thêm người (Partner, Intern, Trainee). Chỉ Admin hoặc Người tạo (Assigned Lawyer) mới có quyền gọi API này.

**Request Body (`application/json`):**
```json
{
  "title": "Tranh chấp đất đai (Update)",
  "category": "CIVIL",
  "customCategory": null,
  "description": "Đã cập nhật",
  "referrerName": "Ông B",
  "caseValue": 150000000.0,
  "referrerPercent": 10.0,
  "assignedLawyerPercent": 20.0,
  "partnerPercent": 5.0,
  "internPercent": 2.0,
  "traineePercent": 1.0,
  "status": "PROCESSING",
  "paymentStatus": "PAID",
  "partnerName": "Luật sư F",
  "internLawyerName": "TTS G",
  "traineeName": null
}
```

**Giải thích các trường Update (Tương tự Create) cộng thêm:**
- `status`: Cập nhật trạng thái của vụ việc (Lấy từ API GET `/api/v1/cases/statuses`).
- `paymentStatus`: Trạng thái thanh toán (Truyền lên `UNPAID` hoặc `PAID`).

**Lỗi có thể trả về:**
- `403 Forbidden`: `{"status": 403, "message": "Bạn không có quyền truy cập"}`

---

## 8. Phân công Vụ việc (Case Assignment)

API này dùng để gán nhân sự (Luật sư, Partner, TTS,...) vào một vụ việc cụ thể, có thể kèm theo **% hoa hồng** và **ghi chú công việc**.

> [!TIP]
> **Luồng tích hợp FE (Frontend Integration Flow):**
> 
> BƯỚC 1: Lấy danh sách nhân sự để làm UI Chọn người:
> - Gọi API: `GET /api/v1/users/staff` (Mục 1)
> - Data trả về là danh sách nhân sự được nhóm theo Role (VD: `LAWYER`, `PARTNER`, `INTERN_LAWYER`, `TRAINEE`).
> - FE dùng danh sách này để render dropdown/select để người dùng chọn tên.
> 
> BƯỚC 2: Người dùng nhập Ghi chú (Tuỳ chọn) và % Hoa hồng (Bắt buộc nếu đã chọn người).
> 
> BƯỚC 3: Nhấn nút [Lưu phân công].
> - FE gom dữ liệu thành 1 Mảng (List) và đẩy lên API `POST /api/v1/cases/{id}/assignments`. Mảng này có thể rỗng `[]` nếu không phân công ai, hoặc chứa 1-2 object nếu phân công 1-2 người.
> - Hệ thống sẽ tự động đồng bộ % Hoa hồng vào Vụ việc gốc (`LegalCase`) và lưu vết lịch sử vào bảng `CaseAssignment`.

### 8.1 Tạo Phân công mới (Assign Users)
- **URL:** `/api/v1/cases/{id}/assignments`
- **Method:** `POST`
- **Quyền yêu cầu:** Người gọi phải là `ADMIN`, `PARTNER`, hoặc `Người tạo vụ việc / Luật sư đang được gán`.
- **Request Body (JSON Array):**
```json
[
  {
    "assigneeId": 10,
    "roleInCase": "PARTNER", 
    "commissionPercent": 15.0,
    "note": "Anh lo giúp em mảng hợp đồng dân sự nhé."
  },
  {
    "assigneeId": 25,
    "roleInCase": "INTERN_LAWYER", 
    "commissionPercent": 5.0,
    "note": ""
  }
]
```
> **Lưu ý:** 
> - Request body có thể là mảng rỗng `[]` nếu không muốn gán cho ai.
> - `roleInCase` chỉ nhận 4 giá trị hợp lệ: `LAWYER`, `PARTNER`, `INTERN_LAWYER`, `TRAINEE`.
> - `commissionPercent` là **BẮT BUỘC** điền nếu đã chọn người.
> - `note` là tuỳ chọn.

- **Response (200 OK):**
```json
{
  "status": 200,
  "message": "Phân công thành công",
  "data": [
    {
      "id": 1,
      "legalCaseId": 5,
      "assignee": {
        "id": 10,
        "fullName": "Nguyen Van Partner"
      },
      "assigner": {
        "id": 1,
        "fullName": "Admin"
      },
      "roleInCase": "PARTNER",
      "note": "Anh lo giúp em mảng hợp đồng dân sự nhé.",
      "commissionPercent": 15.0,
      "createdAt": "2026-06-19T10:00:00Z"
    }
  ]
}
```

### 8.2 Xem Lịch sử Phân công
- **URL:** `/api/v1/cases/{id}/assignments`
- **Method:** `GET`
- **Response (200 OK):** Trả về danh sách Lịch sử phân công (để FE có thể render timeline ai đã gán ai, note là gì).
```json
{
  "status": 200,
  "message": "Thành công",
  "data": [
     // Mảng danh sách phân công giống phần data của API POST
  ]
}
```

### 8.3 Xóa Phân công
API này dùng để gỡ bỏ một người khỏi vụ việc ở một vai trò cụ thể. Nó sẽ xóa tên người đó khỏi vị trí hiện tại trong vụ việc (`LegalCase`), và set % hoa hồng về `0.0`. Lịch sử phân công trong bảng `CaseAssignment` vẫn được giữ nguyên.
- **URL:** `/api/v1/cases/{id}/assignments/{assigneeId}`
- **Method:** `DELETE`
- **Path Variables:**
  - `id`: ID của vụ việc.
  - `assigneeId`: ID của nhân sự cần gỡ bỏ khỏi vụ việc.
- **Quyền yêu cầu:** Người gọi phải là `SP_ADMIN/ADMIN`, `PARTNER`, hoặc `Người tạo vụ việc / Luật sư đang được gán`.
- **Response (200 OK):**
```json
{
  "status": 200,
  "message": "Xóa phân công thành công"
}
```
- **Lỗi có thể trả về:**
  - `400 Bad Request`: Vai trò không hợp lệ.
  - `403 Forbidden`: `{"status": 403, "message": "Không có quyền truy cập"}`

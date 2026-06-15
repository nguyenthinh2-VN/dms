# Quản lý Vụ việc (Case Management API Docs)

## 1. Lấy danh sách nhân sự (Nhóm theo Role)
**Endpoint:** `GET /api/v1/users/staff`

**Mô tả:** Lấy danh sách các nhân sự được phân loại theo mã Role (`LAWYER`, `PARTNER`, `INTERN_LAWYER`, `TRAINEE`). Dùng để render các dropdown chọn người phân công (Partner, Luật sư thực tập, Thực tập sinh).

**Response (200 OK):**
```json
{
  "status": "success",
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
**Endpoint:** `GET /api/v1/cases/statuses`

**Mô tả:** Lấy danh sách 9 trạng thái của vụ việc để render dropdown trên UI.

**Response (200 OK):**
```json
{
  "status": "success",
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
**Endpoint:** `GET /api/v1/cases/categories`

**Mô tả:** Lấy danh sách 12 Lĩnh vực pháp lý (Dân sự, Hình sự...) để làm dropdown lúc tạo vụ việc. Cần truyền `code` lên API.

**Response (200 OK):**
```json
{
  "status": "success",
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
  "partnerId": null,
  "internLawyerId": null,
  "traineeId": null
}
```

**Giải thích các trường (Field Descriptions) dành cho UI:**
- `title` (Tên vụ việc): Tiêu đề ngắn gọn của vụ việc.
- `category` (Lĩnh vực pháp lý): Bắt buộc, gửi mã `code` (VD: `CIVIL`, `OTHER`) lấy từ API `/categories`.
- `customCategory` (Lĩnh vực pháp lý khác): Text tự do. Nếu `category` là `OTHER` thì FE hiển thị ô này cho người dùng nhập tay. Nếu `category` khác `OTHER`, Frontend có thể truyền `null` hoặc không gửi trường này (Backend sẽ tự động ép thành `null`).
- `description` (Mô tả): Chi tiết nội dung vụ việc.
- `referrerName` (Người giới thiệu): Tên của khách hàng hoặc đối tác đã giới thiệu vụ việc này (không bắt buộc, do user tự gõ vào).
- `caseValue` (Giá trị vụ việc): Tổng số tiền thù lao thu được từ vụ việc (VND).
- `referrerPercent` (% Người giới thiệu): Tỉ lệ phần trăm hoa hồng trích lại cho Người giới thiệu.
- `assignedLawyerPercent` (% Người phụ trách): Tỉ lệ phần trăm cho Luật sư phụ trách chính (Người tạo vụ việc).
- `partnerPercent`, `internPercent`, `traineePercent`: Tỉ lệ phần trăm cho Partner, Luật sư thực tập và Thực tập sinh tương ứng.
- `partnerId`, `internLawyerId`, `traineeId`: ID của nhân sự được phân công thêm (Lấy từ API danh sách nhân sự).

**Response (200 OK):**
```json
{
  "status": "success",
  "data": {
    "id": 1,
    "generatedTitle": "Tranh chấp đất đai_Tư vấn pháp luật_150626_Nguyen Van A",
    "status": "NEW",
    "paymentStatus": "UNPAID",
    "netValue": 70000000.0
    // ... trả về chi tiết CaseResponse (Hoa hồng, tiền Net)
  }
}
```

**Lỗi có thể trả về:**
- `403 Forbidden`: `{"status": "error", "message": "403_FORBIDDEN: Bạn không có quyền tạo vụ việc."}`
- `400 Bad Request`: Validation lỗi hoặc sai logic.

---

## 5. Lấy Danh sách Vụ việc
**Endpoint:** `GET /api/v1/cases`

**Mô tả:** Lấy danh sách vụ việc. Admin và Manager Lawyer xem được toàn bộ. Các Role khác chỉ xem được vụ việc mà mình có liên quan.

**Response (200 OK):**
```json
{
  "status": "success",
  "data": [
    {
      "id": 1,
      "title": "Tranh chấp đất đai",
      "status": "NEW"
      // ...
    }
  ]
}
```

---

## 6. Lấy Chi tiết Vụ việc
**Endpoint:** `GET /api/v1/cases/{id}`

**Mô tả:** Lấy chi tiết vụ việc kèm theo số tiền tính toán.

**Lỗi có thể trả về:**
- `403 Forbidden`: `{"status": "error", "message": "403_FORBIDDEN: Bạn không có quyền xem vụ việc này..."}`

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
  "partnerId": 5,
  "internLawyerId": 6,
  "traineeId": 7
}
```

**Giải thích các trường Update (Tương tự Create) cộng thêm:**
- `status`: Cập nhật trạng thái của vụ việc (Lấy từ API GET `/api/v1/cases/statuses`).
- `paymentStatus`: Trạng thái thanh toán (Truyền lên `UNPAID` hoặc `PAID`).

**Lỗi có thể trả về:**
- `403 Forbidden`: `{"status": "error", "message": "403_FORBIDDEN: Bạn không có quyền sửa hoặc phân công vụ việc này..."}`

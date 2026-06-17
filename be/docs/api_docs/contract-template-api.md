# Quản lý Mẫu Hợp đồng (Contract Template API Docs)

> **Lưu ý Đa ngôn ngữ (i18n):**
> Tất cả các API đều hỗ trợ tham số ngôn ngữ. Đặc biệt với API lấy danh sách kiểu trường, việc gửi ngôn ngữ lên sẽ thay đổi text trả về.
> Bạn có thể gửi ngôn ngữ mong muốn qua Query Parameter `?lan=TW` hoặc qua HTTP Header `Accept-Language: TW`. Mặc định nếu không gửi là tiếng Việt (`VI`). Mọi thông báo lỗi (`message`) cũng sẽ được dịch tự động.

---

## 🎯 Luồng tích hợp dành cho Frontend (Workflow)

Để tạo một mẫu hợp đồng mới, Frontend cần thực hiện theo **quy trình 2 bước** sau:

1. **Bước 1 (Preview & Cấu hình):** Người dùng chọn file Word (`.docx`). FE gọi API **`POST /analyze`** (Mục 2) truyền file này lên. 
   - Backend sẽ trả về `htmlContent` (để FE hiển thị giao diện xem trước mẫu) và mảng `fields` (các biến tìm thấy trong file).
   - FE hiển thị form cho Admin nhập **Mã mẫu (`code`)** và **Tên mẫu (`name`)** (2 thông tin này Admin tự nghĩ ra và nhập vào). 
   - Đồng thời, hiển thị danh sách `fields` để Admin có thể sửa đổi Nhãn (`label`) hoặc Kiểu dữ liệu (`fieldType`).

2. **Bước 2 (Lưu chính thức):** Sau khi Admin điền đầy đủ `code`, `name` và chốt danh sách `fields`, bấm nút "Lưu". 
   - FE bọc `code`, `name`, và mảng `fields` thành 1 cục JSON (`metadata`).
   - FE gọi API **`POST /contract-templates`** (Mục 3), truyền lại đúng file Word ban đầu cùng với cục `metadata` này để Backend tiến hành lưu vào DB.

## 1. Lấy danh sách kiểu trường (Field Types)
**Endpoint:** `GET /api/v1/contract-templates/field-types` (Hoặc `?lan=TW` để lấy tiếng Đài Loan)

**Mô tả:** Lấy danh sách kiểu dữ liệu cho trường (TEXT, DATE, NUMBER, MONEY, PARAGRAPH) để render dropdown trên UI.

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Thành công",
  "data": [
    {
      "code": "TEXT",
      "description": "Văn bản"
    },
    {
      "code": "DATE",
      "description": "Ngày"
    }
  ]
}
```

---

## 2. Phân tích file Word (Analyze)
**Endpoint:** `POST /api/v1/contract-templates/analyze`

**Mô tả:** Nhận diện và trích xuất các biến trong file Word có dạng `{{ten_bien}}`, đồng thời convert thành HTML để preview.

**Request (`multipart/form-data`):**
- `file`: File mẫu `.docx` cần phân tích.

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "previewHtml": "<html>...</html>",
    "fields": [
      {
        "fieldKey": "party_a_name",
        "label": "Party A Name",
        "fieldType": "TEXT",
        "required": true,
        "displayOrder": 1,
        "defaultValue": null
      }
    ],
    "warnings": []
  }
}
```

---

## 3. Tạo/Lưu mẫu hợp đồng
**Endpoint:** `POST /api/v1/contract-templates`

**Mô tả:** Upload file Word gốc và các metadata (tên mẫu, danh sách field schema) để lưu vào hệ thống.

**Request (`multipart/form-data`):**
- `file`: File `.docx`.
- `metadata`: Chuỗi JSON thông tin mẫu.

**Ví dụ JSON metadata:**
```json
{
  "code": "HD_DICH_VU_PL",
  "name": "Hợp đồng dịch vụ pháp lý",
  "fields": [
    {
      "fieldKey": "party_a_name",
      "label": "Tên bên A",
      "fieldType": "TEXT",
      "required": true,
      "displayOrder": 1,
      "defaultValue": ""
    }
  ]
}
```

**Response (201 Created):**
```json
{
  "status": 201,
  "message": "Thành công",
  "data": {
    "id": 1,
    "code": "HD_DICH_VU_PL",
    "name": "Hợp đồng dịch vụ pháp lý",
    "version": 1,
    "status": "ACTIVE",
    "originalFileName": "template.docx",
    "htmlContent": "<html>...</html>",
    "createdBy": 1,
    "createdAt": "2023-10-05T12:00:00Z",
    "updatedAt": "2023-10-05T12:00:00Z",
    "fields": [...]
  }
}
```

---

## 4. Lấy Danh sách Mẫu hợp đồng
**Endpoint:** `GET /api/v1/contract-templates`

**Mô tả:** Lấy danh sách tất cả các mẫu đang hoạt động (`status=ACTIVE`). 

**Response (200 OK):**
```json
{
  "status": 200,
  "message": "Thành công",
  "data": [
    {
      "id": 1,
      "code": "HD_DICH_VU_PL",
      "name": "Hợp đồng dịch vụ pháp lý",
      "version": 1,
      "status": "ACTIVE",
      "originalFileName": "template.docx",
      "createdBy": 1,
      "createdAt": "...",
      "updatedAt": "...",
      "fields": null
    }
  ]
}
```

---

## 5. Lấy Chi tiết Mẫu hợp đồng
**Endpoint:** `GET /api/v1/contract-templates/{id}`

**Mô tả:** Lấy chi tiết mẫu hợp đồng (bao gồm schema `fields` và `htmlContent`).

**Response (200 OK):** Trả về đối tượng `ContractTemplateResponse` tương tự như lúc `POST`.

---

## 6. Cập nhật Mẫu hợp đồng
**Endpoint:** `PUT /api/v1/contract-templates/{id}`

**Mô tả:** 
- Nếu gửi kèm `file` mới: hệ thống tự tạo version mới. Mẫu cũ chuyển về `ARCHIVED`.
- Nếu chỉ gửi `metadata`: cập nhật tên mẫu hoặc schema (`label`, `fieldType`, `required`, ...).

**Request (`multipart/form-data`):**
- `file` (tùy chọn): File `.docx` mới.
- `metadata` (tùy chọn): Chuỗi JSON (tương tự lúc tạo, nhưng thiếu `code` vì không được đổi code).

**Ví dụ JSON metadata:**
```json
{
  "name": "Hợp đồng dịch vụ pháp lý (đã sửa)",
  "fields": [
    ...
  ]
}
```

---

## 7. Lưu trữ (Archive) Mẫu hợp đồng
**Endpoint:** `PATCH /api/v1/contract-templates/{id}/archive`

**Mô tả:** Chuyển trạng thái của mẫu từ `ACTIVE` sang `ARCHIVED`.

# Hướng dẫn tích hợp Frontend: Phân hệ Sinh Hợp đồng tự động

Tài liệu này cung cấp hướng dẫn chi tiết từng bước (Step-by-step) dành cho Frontend (FE) để tích hợp luồng "Tạo hợp đồng từ Mẫu (Template)". Ngoài ra, tài liệu mô tả đặc tả kỹ thuật (API Specification) chi tiết cho các endpoints liên quan.

---

## 1. Flow tích hợp dành cho Frontend

Luồng nghiệp vụ được thiết kế gồm 4 bước chính: **Lấy cấu hình mẫu -> Nhập liệu (Dynamic Form) -> Xem trước (Preview) -> Tạo và Lưu trữ (Create & Finalize)**.

### Bước 1: Lấy cấu hình các trường cần điền (Dynamic Form Builder)

**Mục đích:** Khi người dùng chọn một Mẫu hợp đồng (Template), FE cần biết mẫu này yêu cầu những thông tin gì (Tên khách hàng, Ngày ký, Số tiền,...) để hiển thị Form động.

1. **Gọi API:** `GET /api/v1/contract-templates/{templateId}` (API này thuộc module Quản lý Template).
2. **Xử lý Response:** Trong object response, tìm mảng `fields`. Mỗi phần tử trong mảng đại diện cho một ô nhập liệu.
3. **Render UI tương ứng:** Dựa vào `fieldType` của từng phần tử, FE render input phù hợp:
   - Nếu `fieldType === 'TEXT'`: Hiển thị `<input type="text" />`.
   - Nếu `fieldType === 'NUMBER'` hoặc `MONEY`: Hiển thị `<input type="number" />` (Nên thêm mask format định dạng tiền tệ).
   - Nếu `fieldType === 'DATE'`: Hiển thị `<input type="date" />` hoặc dùng DatePicker component.
   - Nếu `fieldType === 'SELECT'`: Hiển thị Dropdown `<select>`.
   - Dùng thuộc tính `label` làm tên hiển thị, `required` để thêm validate bắt buộc nhập.

### Bước 2: Xem trước hợp đồng (Preview)

**Mục đích:** Người dùng muốn xem các thông tin họ vừa nhập sẽ hiển thị ra sao trên văn bản hợp đồng thực tế, TRƯỚC KHI bấm "Lưu".

1. **Thu thập dữ liệu:** FE map toàn bộ dữ liệu người dùng nhập trong form thành một object JSON dạng key-value. Key chính là `fieldKey` từ Bước 1.
   *Ví dụ:* `{"contract_no": "HD-01", "contract_date": "2026-06-18"}`
2. **Gọi API:** `POST /api/v1/contracts/preview`
3. **Hiển thị Preview:** API trả về thuộc tính `renderedHtml` (chứa toàn bộ nội dung file Word đã được convert sang HTML). FE lấy chuỗi HTML này bind vào một thẻ `<div dangerouslySetInnerHTML>` hoặc `<iframe>` để hiển thị trực quan. (Không có file nào bị lưu vào database lúc này).

### Bước 3: Tạo và lưu hợp đồng (Finalize)

**Mục đích:** Người dùng đã hài lòng với bản Preview và bấm "Tạo hợp đồng". Hệ thống sẽ merge dữ liệu, sinh file DOCX, sinh file PDF, và lưu vào CSDL.

1. **Gọi API:** `POST /api/v1/contracts` với payload y hệt như lúc gọi Preview (thêm trường `legalCaseId` nếu hợp đồng thuộc về một Vụ việc cụ thể).
2. **Xử lý Thành công:** API trả về `201 Created` kèm theo object hợp đồng, bao gồm đường link tải file DOCX và PDF. FE có thể chuyển hướng sang trang "Chi tiết hợp đồng" hoặc "Danh sách hợp đồng".

### Bước 4: Xem và Tải file (Download)

1. Ở màn hình chi tiết, FE hiển thị nút "Tải bản Word" và "Tải bản PDF".
2. Khi người dùng click, FE sẽ điều hướng trình duyệt hoặc gọi API:
   - `GET /api/v1/contracts/{id}/download/docx`
   - `GET /api/v1/contracts/{id}/download/pdf`
3. Trình duyệt sẽ tự động tải file xuống dưới dạng Attachment nhờ vào HTTP Header `Content-Disposition`.

---

## 2. Đặc tả API (API Endpoints Specification)

> **Lưu ý chung:** Tất cả các API yêu cầu Header `Authorization: Bearer <token>`.

### 2.1. API Xem trước Hợp đồng (Preview)

Nhận dữ liệu nhập từ form, merge vào file template DOCX và trả ra định dạng HTML để FE nhúng vào màn hình.

- **URL:** `/api/v1/contracts/preview`
- **Method:** `POST`
- **Permissions:** Cần quyền `contract.create`

**Request Body:**
```json
{
  "templateId": 1,
  "data": {
    "contract_no": "HD-2026-0001",
    "contract_date": "2026-02-22",
    "party_a_name": "Công ty TNHH Phần mềm AAA",
    "party_a_representative": "Nguyễn Văn A",
    "party_b_name": "Công ty Cổ phần Xây dựng BBB",
    "amount": "50,000,000"
  }
}
```

**Response - Thành công (200 OK):**
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "renderedHtml": "<html><body>...<p>Số: HD-2026-0001</p>...</body></html>"
  }
}
```

**Các lỗi thường gặp:**
- `400 Bad Request`: Thiếu `templateId` hoặc truyền thiếu các field `required` (Lỗi validation).
- `404 Not Found`: Không tìm thấy `templateId` trong hệ thống.

---

### 2.2. API Tạo Hợp đồng (Create)

Merge dữ liệu, sinh và lưu trữ file cứng (DOCX, PDF) lên ổ đĩa/cloud, lưu thông tin vào CSDL.

- **URL:** `/api/v1/contracts`
- **Method:** `POST`
- **Permissions:** Cần quyền `contract.create`

**Request Body:**
```json
{
  "templateId": 1,
  "legalCaseId": 100, 
  "data": {
    "contract_no": "HD-2026-0001",
    "contract_date": "2026-02-22",
    "party_a_name": "Công ty TNHH Phần mềm AAA",
    "party_a_representative": "Nguyễn Văn A"
    // Liệt kê toàn bộ các keys động tại đây
  }
}
```
*(Ghi chú: `legalCaseId` là ID của vụ việc, có thể null nếu hợp đồng độc lập).*

**Response - Thành công (201 Created):**
```json
{
  "status": 201,
  "message": "Thành công",
  "data": {
    "id": 5,
    "contractNo": "HD-2026-0001",
    "templateId": 1,
    "templateVersion": 2,
    "legalCaseId": 100,
    "status": "FINALIZED",
    "downloadUrl": {
      "docx": "/api/v1/contracts/5/download/docx",
      "pdf": "/api/v1/contracts/5/download/pdf"
    },
    "renderedHtml": "<html>...</html>",
    "data": {
      "contract_no": "HD-2026-0001"
    },
    "createdBy": 1,
    "createdAt": "2026-06-18T10:00:00Z"
  }
}
```

---

### 2.3. API Lấy thông tin Hợp đồng (Get By ID)

- **URL:** `/api/v1/contracts/{id}`
- **Method:** `GET`
- **Permissions:** Cần quyền `contract.view`

**Response - Thành công (200 OK):**
*Cấu trúc JSON trả về giống hệt thuộc tính `data` của API Create.*

---

### 2.4. API Danh sách Hợp đồng (List)

- **URL:** `/api/v1/contracts`
- **Method:** `GET`
- **Permissions:** Cần quyền `contract.list`
- **Query Parameters (Tùy chọn):**
  - `legalCaseId`: Lọc theo Vụ việc
  - `templateId`: Lọc theo Mẫu

**Response - Thành công (200 OK):**
```json
{
  "status": 200,
  "message": "Thành công",
  "data": [
    {
      "id": 5,
      "contractNo": "HD-2026-0001",
      "status": "FINALIZED",
      "createdAt": "2026-06-18T10:00:00Z",
      "downloadUrl": {
        "docx": "/api/v1/contracts/5/download/docx",
        "pdf": "/api/v1/contracts/5/download/pdf"
      }
    }
  ]
}
```

---

### 2.5. API Download File DOCX

- **URL:** `/api/v1/contracts/{id}/download/docx`
- **Method:** `GET`
- **Permissions:** Cần quyền `contract.view`
- **Description:** FE gọi request GET này, response trả về sẽ là một luồng dữ liệu nhị phân (Binary Stream). Trình duyệt sẽ tự động nhận diện thông qua HTTP Headers.

**HTTP Headers trả về:**
```http
Content-Type: application/vnd.openxmlformats-officedocument.wordprocessingml.document
Content-Disposition: attachment; filename="contract_{id}.docx"
```

---

### 2.6. API Download File PDF

- **URL:** `/api/v1/contracts/{id}/download/pdf`
- **Method:** `GET`
- **Permissions:** Cần quyền `contract.view`
- **Description:** Tương tự tải DOCX nhưng format là PDF.

**HTTP Headers trả về:**
```http
Content-Type: application/pdf
Content-Disposition: attachment; filename="contract_{id}.pdf"
```

> **Mẹo cho Frontend khi xử lý Download bằng Axios/Fetch:**
> Thay vì gọi ajax, cách dễ nhất là gán URL vào thẻ `<a>` hoặc dùng `window.location.href`. 
> Nếu cần nhét Bearer Token vào header, FE bắt buộc phải dùng Axios (với `responseType: 'blob'`), sau đó tạo URL tĩnh tạm thời:
> ```javascript
> const response = await axios.get('/api/v1/contracts/1/download/pdf', { responseType: 'blob' });
> const url = window.URL.createObjectURL(new Blob([response.data]));
> const link = document.createElement('a');
> link.href = url;
> link.setAttribute('download', 'contract_1.pdf');
> document.body.appendChild(link);
> link.click();
> ```

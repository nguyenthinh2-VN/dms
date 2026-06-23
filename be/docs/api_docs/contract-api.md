# Hướng dẫn tích hợp Frontend: Phân hệ Sinh Hợp đồng tự động

Tài liệu này cung cấp hướng dẫn chi tiết từng bước (Step-by-step) dành cho Frontend (FE) để tích hợp luồng "Tạo hợp đồng từ Mẫu (Template)". Ngoài ra, tài liệu mô tả đặc tả kỹ thuật (API Specification) chi tiết cho các endpoints liên quan.

---

## 1. Flow tích hợp dành cho Frontend

Luồng nghiệp vụ được thiết kế gồm 4 bước chính: **Lấy cấu hình mẫu -> Nhập liệu (Dynamic Form) -> Xem trước (Preview) -> Tạo và Lưu trữ (Create & Finalize)**.

### Bước 1: Lấy HTML của Mẫu hợp đồng và tạo Form Inline

**Mục đích:** FE nhận mã HTML gốc (đã được giữ nguyên định dạng từ bản Word) và biến các biến `{{FIELD}}` thành các thẻ `<input>` để user nhập liệu trực tiếp trên mặt văn bản.

1. **Gọi API:** `GET /api/v1/contract-templates/{templateId}`.
2. **Xử lý Response:** 
   - Lấy thuộc tính `htmlContent` (chứa toàn bộ nội dung file Word). Backend đã đảm bảo các biến `{{FIELD_NAME}}` được gộp dính liền khối (không bị đứt gãy HTML).
   - Lấy mảng `fields` để biết danh sách các biến cần thay thế.
3. **Thay thế chuỗi (Replace):** Dùng Javascript quét chuỗi `htmlContent` và thay thế `{{FIELD_NAME}}` thành `<input>`:
   ```javascript
   let finalHtml = template.htmlContent;
   template.fields.forEach(f => {
     const regex = new RegExp(`{{${f.fieldKey}}}`, 'g');
     // Replace bằng input, gán name để lúc submit dễ lấy data
     finalHtml = finalHtml.replace(regex, `<input name="${f.fieldKey}" placeholder="Nhập ${f.label}..." class="contract-input" />`);
   });
   ```
4. **Render UI:** Đưa `finalHtml` vào trình duyệt bằng cách dùng `dangerouslySetInnerHTML` hoặc gắn vào `innerHTML` của một thẻ `<div>`.

### Bước 2: Nhập liệu và Lưu hợp đồng (Finalize)

**Mục đích:** Người dùng nhập trực tiếp trên văn bản HTML. Khi bấm "Tạo hợp đồng", hệ thống sẽ gửi data lên BE để lưu thành file cứng (DOCX, PDF) và lưu vào CSDL.

1. **Thu thập dữ liệu:** Khi user submit, FE dùng Javascript select tất cả các input trong form HTML để lấy giá trị:
   ```javascript
   const formData = {};
   document.querySelectorAll('.contract-input').forEach(input => {
     formData[input.name] = input.value;
   });
   ```
2. **Gọi API:** `POST /api/v1/contracts` (truyền `templateId`, `legalCaseId` và object `data` vừa thu thập).
3. **Xử lý Thành công:** API trả về `201 Created` kèm theo object hợp đồng, bao gồm đường link tải file DOCX và PDF. FE có thể chuyển hướng sang trang "Chi tiết hợp đồng" hoặc hiển thị nút tải file ngay trên màn hình.

### Bước 3: Xem và Tải file (Download)

1. Ở màn hình chi tiết, FE hiển thị nút "Tải bản Word" và "Tải bản PDF".
2. Khi người dùng click, FE sẽ điều hướng trình duyệt hoặc gọi API:
   - `GET /api/v1/contracts/{id}/download/docx`
   - `GET /api/v1/contracts/{id}/download/pdf`
3. Trình duyệt sẽ tự động tải file xuống dưới dạng Attachment nhờ vào HTTP Header `Content-Disposition`.

---

## 2. Đặc tả API (API Endpoints Specification)

> **Lưu ý chung:** Tất cả các API yêu cầu Header `Authorization: Bearer <token>`.



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
      "templateId": 1,
      "templateName": "Hợp đồng tư vấn",
      "legalCaseId": 100,
      "status": "FINALIZED",
      "createdBy": 1,
      "creatorName": "Admin",
      "createdAt": "2026-06-18T10:00:00Z"
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

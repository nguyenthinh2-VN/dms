# Tài liệu Mapping các Enum (Dành cho Frontend)

Tài liệu này giải thích cách hiển thị văn bản (Text) cho các mã trạng thái (Enum Code) trả về từ API Backend.

## Nguyên tắc hoạt động
1. Các API trả về chi tiết hoặc danh sách vụ việc (ví dụ `CaseResponse`) sẽ luôn trả về các **mã Code** (VD: `CIVIL`, `NEW`, `PAID`).
2. Frontend sử dụng các API danh mục để lấy danh sách map giữa **mã Code** và **Tên hiển thị (Text)** tương ứng với ngôn ngữ hiện tại.
3. Backend hỗ trợ truyền tham số ngôn ngữ qua 1 trong 2 cách:
   - Truyền qua Query Parameter: `?lan=VI` hoặc `?lan=TW`
   - Truyền qua HTTP Header: `Accept-Language: VI` hoặc `Accept-Language: TW`
   (Mặc định nếu không truyền sẽ là `VI`).

---

## 1. Loại vụ việc (CaseCategory)

- **API lấy danh sách**: `GET /api/v1/cases/categories?lan=TW`

| Mã Code (Enum) | Tên hiển thị (VI) | Tên hiển thị (TW) |
|---|---|---|
| `CIVIL` | Dân sự | 民事 |
| `CRIMINAL` | Hình sự | 刑事 |
| `ADMINISTRATIVE` | Hành chính | 行政 |
| `MARRIAGE_FAMILY` | Hôn nhân & Gia đình | 婚姻與家庭 |
| `LAND_REAL_ESTATE` | Đất đai – Bất động sản | 土地與房地產 |
| `LABOR` | Lao động | 勞動 |
| `CORPORATE_INVESTMENT` | Doanh nghiệp – Đầu tư | 企業與投資 |
| `COMMERCIAL` | Kinh doanh – Thương mại | 商業與貿易 |
| `INTELLECTUAL_PROPERTY` | Sở hữu trí tuệ | 智慧財產權 |
| `TAX_FINANCE` | Thuế – Tài chính | 稅務與財務 |
| `BANKRUPTCY` | Phá sản | 破產 |
| `OTHER` | Khác | 其他 |

---

## 2. Trạng thái vụ việc (CaseStatus)

- **API lấy danh sách**: `GET /api/v1/cases/statuses?lan=TW`

| Mã Code (Enum) | Tên hiển thị (VI) | Tên hiển thị (TW) |
|---|---|---|
| `NEW` | Mới | 新增 |
| `PENDING_VERIFICATION` | Đợi xác minh | 待驗證 |
| `VERIFYING` | Đang xác minh | 驗證中 |
| `CONTRACT_NEGOTIATING` | Đang chốt hợp đồng | 合約洽談中 |
| `CONTRACTED` | Đã chốt hợp đồng | 已簽約 |
| `PROCESSING` | Đang xử lý | 處理中 |
| `PAUSED` | Tạm dừng | 暫停 |
| `ACCEPTANCE_PAYMENT` | Đang nghiệm thu/thanh toán | 驗收/付款中 |
| `CLOSED` | Đã đóng | 已結案 |

---

## 3. Trạng thái thanh toán (PaymentStatus)

*(Sẽ có API tương ứng hoặc Frontend tự map tĩnh do ít thay đổi)*

| Mã Code (Enum) | Tên hiển thị (VI) | Tên hiển thị (TW) |
|---|---|---|
| `UNPAID` | Chưa thu | 未付款 |
| `PAID` | Đã thu | 已付款 |

---

## 4. Định dạng Response chuẩn chung

Tất cả các API hiện tại đều đã được chuẩn hóa để trả về theo cấu trúc sau:

**Response Thành công (200 / 201):**
```json
{
  "status": 200,
  "message": "Thành công", // Hoặc "成功" nếu lan=TW
  "data": { ... }
}
```

**Response Lỗi (400 / 401 / 403 / 500):**
```json
{
  "status": 403,
  "message": "Không có quyền truy cập", // Hoặc "您沒有權限存取此資源" nếu lan=TW
  "errors": { ... } // Tùy chọn, trả về danh sách lỗi validation nếu có
}
```

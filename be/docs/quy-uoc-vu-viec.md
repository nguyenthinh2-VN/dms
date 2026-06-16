# Quy ước Vụ việc — Legalprotech

Tài liệu tham chiếu cho module tạo & quản lý vụ việc.

---

## 1. Phân loại vụ việc

Tách **2 field** độc lập. Lưu `code` (tiếng Anh) trong DB, label chỉ là key dịch (vi / zh-Hant).

### 1.1. Loại vụ việc — theo lĩnh vực pháp lý

| Label (vi) | Code |
|---|---|
| Dân sự | `CIVIL` |
| Hình sự | `CRIMINAL` |
| Hành chính | `ADMINISTRATIVE` |
| Hôn nhân & Gia đình | `MARRIAGE_FAMILY` |
| Đất đai – Bất động sản | `LAND_REAL_ESTATE` |
| Lao động | `LABOR` |
| Doanh nghiệp – Đầu tư | `CORPORATE_INVESTMENT` |
| Kinh doanh – Thương mại | `COMMERCIAL` |
| Sở hữu trí tuệ | `INTELLECTUAL_PROPERTY` |
| Thuế – Tài chính | `TAX_FINANCE` |
| Phá sản | `BANKRUPTCY` |
| Khác | `OTHER` *(kèm ô nhập tự do)* |

---

## 2. Format tên vụ việc

Áp dụng sau khi đã tạo vụ việc:

```
Tiêu đề vụ việc (Sự việc)_Loại vụ việc_DDMMYY_Người tạo
```

Ví dụ:

```
Xử lý ly hôn_Tư vấn_050626_Administrator
```

> **Lưu ý:** đoạn "Loại vụ việc" trong tên = **Hình thức dịch vụ** (mục 1.2, vd "Tư vấn"). Lĩnh vực pháp lý (vd "ly hôn") nằm trong phần Tiêu đề. `DDMMYY` = ngày tạo (vd 05/06/26 → `050626`).

---

## 3. Trạng thái vụ việc

Mặc định khi tạo mới: **Mới** (`NEW`).

| # | Label (vi) | Code |
|---|---|---|
| 0 | Mới *(mặc định)* | `NEW` |
| 1 | Đợi xác minh | `PENDING_VERIFICATION` |
| 2 | Đang xác minh | `VERIFYING` |
| 3 | Đang chốt hợp đồng | `CONTRACT_NEGOTIATING` |
| 4 | Đã chốt hợp đồng | `CONTRACT_SIGNED` |
| 5 | Đang xử lý | `IN_PROGRESS` |
| 6 | Tạm dừng | `ON_HOLD` |
| 7 | Đang nghiệm thu / thanh toán | `ACCEPTANCE_PAYMENT` |
| 8 | Đã đóng vụ việc | `CLOSED` |

Luồng thông thường: `NEW → PENDING_VERIFICATION → VERIFYING → CONTRACT_NEGOTIATING → CONTRACT_SIGNED → IN_PROGRESS → ACCEPTANCE_PAYMENT → CLOSED`. `ON_HOLD` có thể bật ở bất kỳ bước nào trước khi đóng.

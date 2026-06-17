# Quy ước đặt tên biến trong mẫu hợp đồng — Legalprotech

Tài liệu tham chiếu cho việc đánh dấu vùng cần điền trong file Word `.docx` khi tạo mẫu hợp đồng.

Áp dụng cho: chức năng **Tạo mẫu hợp đồng** (UC1) và **Tạo hợp đồng từ mẫu** (UC2).

---

## 1. Cú pháp chuẩn

Mọi vùng cần điền trong file Word đều phải bọc trong cặp dấu **`{{` `}}`** (dấu ngoặc nhọn đôi).

```
{{ten_bien}}
```

Ví dụ trong file Word:

```
Bên A: {{party_a_name}}
Mã số thuế: {{party_a_tax_code}}
Địa chỉ: {{party_a_address}}
```

> **Lưu ý:** Hệ thống **chỉ** dò biến theo cú pháp `{{...}}`. Các pattern cũ như `[...]`, `___`, `......` **không** được nhận diện. Soạn thảo viên phải chuyển hết về `{{...}}`.

---

## 2. Quy tắc đặt tên (bên trong `{{ }}`)

| Quy tắc | Bắt buộc | Mô tả |
|---|---|---|
| `snake_case` | ✅ | Chữ thường ASCII, từ ngăn cách bằng `_`. |
| Ký tự cho phép | ✅ | `[a-z0-9_]` — chữ thường, số, gạch dưới. |
| Bắt đầu bằng chữ cái | ✅ | Không bắt đầu bằng số hoặc `_`. |
| Không có khoảng trắng | ✅ | `{{ party a name }}` ❌ — sai. |
| Không dấu tiếng Việt | ✅ | `{{ten_bên_a}}` ❌ — sai. |
| Không nested | ✅ | `{{ {{x}} }}` ❌ — sai. |
| Unique trong cùng 1 mẫu | ✅ | Cùng 1 `key` xuất hiện nhiều lần được phép, được hiểu là **cùng 1 biến**, gắn với cùng 1 input trên form. |

---

## 3. Tiền tố nhóm (prefix) — bắt buộc khi áp dụng được

Để phân nhóm trên form nhập liệu (UC2) và tránh trùng tên giữa các bên/mục, đặt biến theo tiền tố sau:

| Nhóm | Prefix | Ví dụ |
|---|---|---|
| Bên A (bên thuê dịch vụ / bên bán) | `party_a_` | `party_a_name`, `party_a_tax_code`, `party_a_address`, `party_a_representative`, `party_a_position` |
| Bên B (bên cung cấp dịch vụ / bên mua) | `party_b_` | `party_b_name`, `party_b_tax_code`, `party_b_address`, `party_b_representative` |
| Thông tin hợp đồng | `contract_` | `contract_no`, `contract_date`, `contract_value`, `contract_term` |
| Thanh toán | `payment_` | `payment_term`, `payment_method`, `payment_due_date` |
| Vụ việc liên kết | `case_` | `case_title`, `case_no`, `case_category` |
| Người đại diện ký | `signer_` | `signer_a_name`, `signer_b_name` |

> **Lưu ý:** Nếu hợp đồng có nhiều bên (>2), dùng `party_c_`, `party_d_` ... theo thứ tự.

---

## 4. Hậu tố gợi ý kiểu dữ liệu (suffix — tùy chọn)

Hậu tố giúp hệ thống đề xuất `fieldType` mặc định khi sinh schema. Người dùng vẫn có thể sửa lại trong bước review.

| Hậu tố | Kiểu gợi ý | Ví dụ |
|---|---|---|
| `_date` | `DATE` | `contract_date`, `payment_due_date` |
| `_value`, `_amount` | `MONEY` | `contract_value`, `payment_amount` |
| `_no`, `_code` | `TEXT` | `contract_no`, `party_a_tax_code` |
| `_percent`, `_rate` | `NUMBER` | `interest_rate`, `vat_percent` |
| `_text`, `_note`, `_description` | `PARAGRAPH` | `contract_note`, `payment_description` |
| (mặc định) | `TEXT` | mọi trường hợp khác |

---

## 5. Bảng biến chuẩn dùng chung

Khuyến nghị dùng đúng các tên này khi gặp các trường thông dụng để mẫu hợp đồng giữa các luật sư đồng nhất:

| Biến | Nhãn (vi) | Kiểu |
|---|---|---|
| `party_a_name` | Tên Bên A | TEXT |
| `party_a_tax_code` | Mã số thuế Bên A | TEXT |
| `party_a_address` | Địa chỉ Bên A | TEXT |
| `party_a_representative` | Người đại diện Bên A | TEXT |
| `party_a_position` | Chức vụ Bên A | TEXT |
| `party_a_phone` | SĐT Bên A | TEXT |
| `party_b_name` | Tên Bên B | TEXT |
| `party_b_tax_code` | Mã số thuế Bên B | TEXT |
| `party_b_address` | Địa chỉ Bên B | TEXT |
| `party_b_representative` | Người đại diện Bên B | TEXT |
| `contract_no` | Số hợp đồng | TEXT |
| `contract_date` | Ngày ký hợp đồng | DATE |
| `contract_value` | Giá trị hợp đồng | MONEY |
| `contract_term` | Thời hạn hợp đồng | TEXT |
| `payment_term` | Điều khoản thanh toán | PARAGRAPH |
| `payment_method` | Phương thức thanh toán | TEXT |
| `case_title` | Tên vụ việc liên kết | TEXT |
| `case_no` | Số vụ việc | TEXT |

---

## 6. Ví dụ đúng / sai

**Đúng ✅**
```
{{party_a_name}}
{{contract_no}}
{{contract_date}}
{{payment_method}}
```

**Sai ❌**
```
{{ Party A Name }}      → có khoảng trắng + viết hoa
{{tên_bên_a}}           → có dấu tiếng Việt
{{2_party_name}}        → bắt đầu bằng số
{ party_a_name }        → thiếu dấu ngoặc đôi
{{party-a-name}}        → dùng dấu gạch ngang
{{ {{x}} }}             → nested
```

---

## 7. Quy tắc xử lý phía hệ thống

1. **Khoảng trắng trong `{{ }}`:** Hệ thống tự `trim()`. `{{ party_a_name }}` ↔ `{{party_a_name}}` được coi là cùng 1 biến.
2. **Trùng biến:** Cùng 1 `key` xuất hiện nhiều lần trong file → gộp thành **1 field** trong schema, **chỉ render 1 ô input** trên form, lúc merge sẽ thay tất cả vị trí.
3. **Cảnh báo:** Tên không theo prefix khuyến nghị (mục 3) → chỉ **cảnh báo (warning)**, **không chặn**. Tên sai cú pháp (mục 2) → **lỗi 400, chặn lưu**.
4. **Word cắt run:** Khi gõ `{{key}}` trong Word, đôi khi Word tự cắt thành nhiều run (`{{`, `key`, `}}` ở 3 run khác nhau). Hệ thống chạy `VariablePrepare.prepare()` của docx4j để gộp lại trước khi dò.

---

## 8. Tham khảo

- File implementation plan: [plan/contract-template-creation.md](../../plan/contract-template-creation.md)
- File permission keys: [permission-keys.md](permission-keys.md)
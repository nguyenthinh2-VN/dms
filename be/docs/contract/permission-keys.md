# Quy ước Permission Key — Hệ thống DMS

Tài liệu mô tả cách đặt mã `permission.code` cho RBAC động. Áp dụng cho mọi module, đặc biệt là các module mới (Contract Template, Contract, Case List API ...).

---

## 1. Format chung

```
{domain}.{action}
```

Hoặc khi cần phân scope:

```
{domain}.{action}.{scope}
```

- `domain`: tên module (snake_case, số ít). VD: `contract_template`, `contract`, `case`, `user`.
- `action`: hành động (snake_case). VD: `create`, `view`, `update`, `archive`, `download`, `assign`, `list`.
- `scope` (tùy chọn): phạm vi áp dụng. VD: `all` (toàn bộ), `own` (chỉ của mình), `assigned` (vụ việc được phân công).

---

## 2. Bảng permission keys của các module hiện tại & mới

### 2.1. Contract Template (UC1)

| Code | Mô tả |
|---|---|
| `contract_template.create` | Upload file Word + lưu mẫu hợp đồng. |
| `contract_template.view` | Xem chi tiết mẫu (gồm field schema). |
| `contract_template.list` | Xem danh sách mẫu. |
| `contract_template.update` | Sửa mẫu (tạo version mới). |
| `contract_template.archive` | Chuyển mẫu sang `ARCHIVED`. |

### 2.2. Contract (UC2)

| Code | Mô tả |
|---|---|
| `contract.create` | Tạo hợp đồng từ mẫu (preview + lưu). |
| `contract.view.own` | Xem hợp đồng do mình tạo / được phân công. |
| `contract.view.all` | Xem mọi hợp đồng (cấp Admin/Manager). |
| `contract.list.own` | Danh sách hợp đồng của mình. |
| `contract.list.all` | Danh sách toàn bộ hợp đồng. |
| `contract.download` | Tải file `.docx` / `.pdf` của hợp đồng được phép xem. |

### 2.3. Case (UC3 — danh sách vụ việc dùng cho UC2 attach)

| Code | Mô tả |
|---|---|
| `case.list.own` | Danh sách vụ việc liên quan tới mình (assignedLawyer/partner/intern/trainee). |
| `case.list.all` | Toàn bộ danh sách vụ việc. |

---

## 3. Pipeline kiểm tra quyền

Trong mỗi use case mới (UC1/UC2/UC3) **không hardcode** so sánh role:

```java
// ❌ WRONG — hardcode role
if (!roleName.equals("ADMIN") && !roleName.equals("LAWYER")) {
    throw new ForbiddenException("...");
}

// ✅ RIGHT — check permission động
if (!permissionChecker.hasPermission(currentUser, "contract_template.create")) {
    throw new ForbiddenException("Bạn không có quyền tạo mẫu hợp đồng.");
}
```

Service `PermissionChecker` (đặt trong `application/service/`) đọc qua `RuleRepository`:

1. Lấy mọi `Rule` của `userId` có `status = ACTIVE`.
2. Join sang `Permission` để lấy `code`.
3. Trả `true` nếu `code` cần kiểm tra nằm trong danh sách.
4. Cache theo `userId` ở cấp request scope (tránh hỏi DB nhiều lần trong cùng 1 request).

---

## 4. Cấp permission mặc định cho Role

`DataInitializer` (lúc app khởi động) seed:

| Role code | Permission gán mặc định |
|---|---|
| `ADMIN` | Toàn bộ permission của mọi module (`*.*`). |
| `MANAGER_LAWYER` | `contract_template.*`, `contract.*` (trừ `archive` của template), `case.list.all`. |
| `LAWYER` | `contract_template.create`, `contract_template.view`, `contract_template.list`, `contract.create`, `contract.view.own`, `contract.list.own`, `contract.download`, `case.list.own`. |
| `INTERN_LAWYER` | `contract_template.view`, `contract_template.list`, `contract.view.own`, `contract.list.own`, `contract.download`, `case.list.own`. |
| `PARTNER` | `contract.view.own`, `contract.list.own`, `contract.download`, `case.list.own`. |
| `TRAINEE` | `case.list.own`. |

> **Lưu ý:** Đây là **mặc định**. Admin vẫn có thể vào trang quản lý quyền cấp/thu hồi từng permission cho từng user qua bảng `rules (user_id, permission_id, status)`. Pipeline check ở mục 3 đã hỗ trợ điều này.

---

## 5. Quy tắc khi thêm permission mới

1. Thêm 1 dòng `Permission` vào `DataInitializer.seedPermissions()` với `code` đúng format mục 1.
2. Cập nhật bảng phân quyền mặc định (mục 4) trong tài liệu này.
3. Thêm permission key vào file plan tương ứng (mục **PHÂN QUYỀN** của plan).
4. Trong use case, gọi `permissionChecker.hasPermission(user, "{code}")`.
5. Nếu permission có scope `own`/`all`, viết hàm scope helper riêng (vd `filterByScope(list, user, "case.list")`).
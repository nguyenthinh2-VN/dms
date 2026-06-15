# LGL_DMS — Đặc tả Phân quyền (RBAC) cho Backend Dev

**Phiên bản:** 2.0 · **Cập nhật:** 15/06/2026
**File đi kèm:** `LGL_DMS_MaTranPhanQuyen_v2.xlsx` (ma trận tick allow/deny, 86 permission keys)

> **Thay đổi v2:** Ma trận chuyển sang dạng **tick ✔ (allow/deny)** map thẳng vào `role_permissions(role_id, permission_id)`. Phạm vi dữ liệu (data scope) tách khỏi ma trận, trở thành **1 thuộc tính `default_scope` gắn vào permission** (tùy chọn) — bỏ được nếu chưa cần.

---

## 1. Mô hình tổng quan

- **RBAC (chính):** mỗi vai trò hệ thống được tick ✔ cho từng permission. `✔` = được phép, ô trống = không.
- **Vai trò trong vụ việc (bổ sung):** với tài nguyên thuộc một vụ việc, áp thêm bảng năng lực theo vai trò vụ việc (QL chính/phụ, người xử lý, người duyệt, TTS, bên thứ 3).
- **default_scope (tùy chọn):** mỗi permission có thể gắn 1 phạm vi dữ liệu mặc định để lọc bản ghi (Admin bypass). Xem mục 3.

> **Quyền hiệu lực:** được phép khi vai trò hệ thống có ✔ cho permission **VÀ** (nếu là tài nguyên vụ việc) vai trò trong vụ việc cho phép. Vai trò vụ việc không nâng quyền vượt vai trò hệ thống.

---

## 2. Vai trò hệ thống (RBAC)

| Mã | Vai trò | Tóm tắt |
|----|---------|---------|
| R1 | Quản trị viên hệ thống (Admin) | Toàn quyền: hệ thống, bảo mật, RBAC, cấu hình AI, nhân sự, dữ liệu cá nhân. Không giới hạn. |
| R2 | Luật sư cấp quản lý | Quản lý vụ việc, phân công, thù lao, báo cáo & audit của cấp dưới. Không đụng hệ thống/AI config. |
| R3 | Luật sư thực thi | Toàn quyền tác nghiệp trên vụ việc được giao. |
| R4 | Thực tập sinh | Đầu việc chia nhỏ; không chạy phân tích rủi ro, không quản lý vụ việc. |
| R5 | Partner / Cộng tác viên | Chỉ thấy tài liệu/vụ việc mình tham gia; có mã giới thiệu & cây phả hệ riêng. |
| R6 | Khách vãng lai (Guest) | Đã đăng nhập, chỉ xem nội dung công khai + trợ lý AI bản giới hạn. |

---

## 3. default_scope (tùy chọn) — phạm vi dữ liệu gắn vào permission

Bảng `permissions` có thể thêm cột `default_scope`. Khi role có ✔ cho permission, backend lọc bản ghi theo scope đó (Admin/role toàn cục bypass). **Nếu không dùng**, chỉ cần tick allow/deny, phần lọc xử lý ở tầng nghiệp vụ.

| default_scope | Ý nghĩa | Điều kiện truy vấn gợi ý |
|---------------|---------|--------------------------|
| `all` | Toàn hệ thống | `WHERE 1=1` |
| `downline` | Của mình + cấp dưới (cây phả hệ) | `WHERE owner_id IN (downline_of(user))` |
| `case` | Vụ việc được phân công | `WHERE case_id IN (cases_assigned_to(user))` |
| `own` | Chỉ của mình | `WHERE owner_id = user.id` |
| `public` | Nội dung công khai | `WHERE visibility = 'public'` |

`downline_of(user)` tính đệ quy từ `referrer_id` (mã giới thiệu); đã bao gồm chính nút gốc. Giá trị `default_scope` của từng permission xem cột tương ứng ở sheet **"Danh mục Permission Key"**.

---

## 4. Vai trò trong vụ việc (gán khi phân công)

Lưu tại `case_participants.case_role`. ✔ = được phép trên tài nguyên của vụ việc đó.

| Năng lực | QL chính | QL phụ | Người xử lý | Người duyệt | TTS | Bên thứ 3 |
|----------|:---:|:---:|:---:|:---:|:---:|:---:|
| Xem hồ sơ & file vụ việc | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| Tải lên / sửa file | ✔ | ✔ | ✔ |  | ✔ |  |
| Tạo/sửa tài liệu, hợp đồng | ✔ | ✔ | ✔ |  |  |  |
| Chạy phân tích tài liệu/rủi ro | ✔ | ✔ | ✔ | ✔ |  |  |
| Tạo nhắc hẹn vụ việc | ✔ | ✔ | ✔ | ✔ | ✔ |  |
| Phân công đầu việc con | ✔ | ✔ |  |  |  |  |
| Thêm/bớt người tham gia | ✔ |  |  |  |  |  |
| Duyệt sản phẩm công việc | ✔ | ✔ |  | ✔ |  |  |
| Thiết lập % thù lao | ✔ |  |  |  |  |  |
| Nhận % thù lao | ✔ | ✔ | ✔ | ✔ | ✔ | ✔ |
| Đóng/lưu trữ vụ việc | ✔ |  |  |  |  |  |

Bên thứ 3 mặc định chỉ xem. Một người có thể giữ vai trò vụ việc khác nhau ở các vụ việc khác nhau.

---

## 5. Pipeline phân quyền (mỗi request)

```
1. Authenticate → user + role (R1..R6)
2. Tra permission_key của endpoint trong ma trận theo role → có ✔ không?
3. Không ✔ → 403
4. Tài nguyên thuộc vụ việc → kiểm tra thêm case_role (∩)
5. (tùy chọn) áp default_scope để lọc bản ghi; Admin bypass
6. permission *.approve → chỉ cho hành động phê duyệt trong workflow
7. permission.sensitive → ghi audit_log
```

### Pseudocode guard

```python
def authorize(user, perm_key, resource=None):
    if not role_has_permission(user.role, perm_key):     # B2/B3 (tick)
        raise Forbidden(perm_key)
    if resource and resource.case_id:                    # B4
        if not case_role_allows(user, resource, perm_key):
            raise Forbidden("case_role")
    scope = default_scope(perm_key)                       # B5 (optional)
    if scope and not user.is_admin and not in_scope(user, resource, scope):
        raise Forbidden("scope")
    if perm_key.endswith(".approve") and action != "approve":   # B6
        raise Forbidden("approve_only")
    if is_sensitive(perm_key):                            # B7
        audit(user, perm_key, resource)
    return True
```

---

## 6. Quy ước Permission Key

- **Định dạng:** `domain.action` hoặc `domain.action.scope` — vd `ai.chat.history.read`, `genealogy.tree.read.all`.
- Lưu ở bảng `permissions`; gán role ở `role_permissions(role_id, permission_id)` **dạng boolean** (có bản ghi = ✔).
- Override theo nhân sự ở `user_permissions(user_id, permission_id, effect=allow|deny)` — phục vụ "gán quyền checkbox" của Admin (Module Quản lý quyền hạn).
- 86 key đầy đủ ở sheet **"Danh mục Permission Key"** (domain, key, mô tả, default_scope gợi ý, nhạy cảm/audit).

---

## 7. Workflow duyệt (`*.approve`)

`compensation.approve` (và mở rộng: duyệt xóa vụ việc, chi trả). Người duyệt không tự thực thi — họ phê duyệt request do người khác tạo. Lưu: người yêu cầu, người duyệt, thời điểm, kết quả, lý do.

---

## 8. Audit log (bắt buộc với key đánh dấu ●)

Trường tối thiểu: `actor_id, role, permission_key, resource_type, resource_id, action, before, after, ip, user_agent, timestamp`. Append-only.
Đọc: `audit.log.read` — Admin xem toàn bộ, Luật sư QL xem của cấp dưới.

---

## 9. Lược đồ DB tối thiểu (gợi ý)

```sql
users(id, identity_code UNIQUE, full_name, role_id, referrer_id, status, ...)   -- referrer_id → cây phả hệ
roles(id, code, name)
permissions(id, key UNIQUE, domain, sensitive BOOL, default_scope NULL)         -- default_scope tùy chọn
role_permissions(role_id, permission_id)                                        -- có bản ghi = ✔ (allow)
user_permissions(user_id, permission_id, effect)                                -- effect = allow|deny (override checkbox)
cases(id, code, name, status, ...)
case_participants(case_id, user_id, case_role, comp_percent)                    -- case_role enum
audit_logs(id, actor_id, permission_key, resource_type, resource_id, action, meta JSONB, created_at)
```

`default_scope` enum (NULL = không lọc): `all | downline | case | own | public`
`case_role` enum: `lead_manager | sub_manager | handler | approver | intern | third_party`

---

## 10. Lưu ý triển khai

- **Mã giới thiệu là thuộc tính của mọi account** (kể cả Admin), không phải vai trò; quyết định cây phả hệ (downline).
- Cache `downline_of(user)` và `cases_assigned_to(user)` (vd Redis) nếu dùng default_scope.
- Mọi endpoint **mặc định deny**; chỉ mở permission có ✔ trong ma trận.
- Soft-delete cho `account.delete`, `case.delete`, `case.file.delete`.
- Guest (R6) tách rõ ở tầng API gateway, không chạm tài nguyên nội bộ.

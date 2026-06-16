# Authentication API Docs

> **Lưu ý Đa ngôn ngữ (i18n):**
> Tất cả các API đều hỗ trợ tham số ngôn ngữ để dịch các thông báo lỗi hoặc thông báo thành công (trường `message`).
> Bạn có thể gửi ngôn ngữ mong muốn qua Query Parameter `?lan=TW` (hoặc `VI`) hoặc qua HTTP Header `Accept-Language: TW`. Mặc định nếu không gửi là tiếng Việt (`VI`).

## 1. Đăng ký tài khoản (Register)

**Endpoint:** `POST /api/v1/auth/register`

**Mô tả:** Đăng ký tài khoản người dùng mới.

**Request Body (`application/json`):**
```json
{
  "fullName": "Nguyen Van A",
  "workEmail": "nva@example.com",
  "position": "Luật sư tư vấn",
  "phoneNumber": "0123456789",
  "password": "Password123!",
  "confirmPassword": "Password123!",
  "invitedByCode": "REF123",
  "role": "LAWYER"
}
```

**Điều kiện Validate (Conditions):**
- `fullName`: Không được để trống.
- `workEmail`: Bắt buộc đúng định dạng Email (`@`).
- `position` (Chức danh): Không được để trống. Đây là text tự gõ mô tả chức danh làm việc thực tế tại văn phòng (VD: `Luật sư tư vấn`, `Luật sư tranh tụng`, `Trợ lý pháp lý`...), không ảnh hưởng đến phân quyền hệ thống.
- `password` / `confirmPassword`: Phải giống nhau.
- `phoneNumber`: Bắt buộc bắt đầu bằng `0`, và tối đa 15 ký tự số (Regex: `^0\d{1,14}$`).
- `role`: Bắt buộc gửi MÃ ROLE (Ví dụ: `LAWYER`, `INTERN_LAWYER`, `TRAINEE`, `PARTNER`). Không được phép truyền các role hệ thống (`ADMIN`, `SUPER_ADMIN`).
- `invitedByCode`: Có thể bỏ trống. Nếu có gửi thì phải tồn tại trong Database.

**Response (Success 200 OK / 201 Created):**
```json
{
  "status": 201,
  "message": "Đăng ký thành công",
  "data": {
    "id": "1",
    "email": "nva@example.com",
    "personalReferralCode": "A8X2B9"
  }
}
```

**Response (Error 400 Bad Request):**
```json
{
  "status": 400,
  "message": "Validation failed", // Hoặc "驗證失敗" nếu lan=TW
  "errors": {
    "role": "Role không hợp lệ",
    "phoneNumber": "Số điện thoại không đúng định dạng"
  }
}
```

---

## 2. Đăng nhập (Login)

**Endpoint:** `POST /api/v1/auth/login`

**Request Body (`application/json`):**
```json
{
  "email": "nva@example.com",
  "password": "Password123!"
}
```

**Response (Success 200 OK):**
```json
{
  "status": 200,
  "message": "Đăng nhập thành công",
  "data": {
    "accessToken": "eyJhbGciOiJIUzI1NiIsInR5cCI6Ik...",
    "expiresIn": 3600,
    "role": "Luật sư"
  }
}
```

**Response (Error 401 Unauthorized):**
```json
{
  "status": 401,
  "message": "Tài khoản hoặc mật khẩu không chính xác" // Hoặc "帳號或密碼不正確" nếu lan=TW
}
```

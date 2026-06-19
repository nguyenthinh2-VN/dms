# User Management & Dynamic Permissions API

Các API dành cho Quản lý User và Phân quyền động, yêu cầu token hợp lệ.

> [!NOTE] 
> **Tài khoản mặc định (Default Super Admin):** Khi hệ thống khởi động lần đầu, nếu chưa có, hệ thống sẽ tự động tạo một tài khoản Super Admin với toàn bộ quyền để có thể đăng nhập vào thao tác:
> - **Email:** `admin@dms.com`
> - **Mật khẩu:** `123456`
> - **Role:** `SUPER_ADMIN` (Quản trị viên cấp cao nhất)

## 1. Lấy danh sách Role
Lấy danh sách các chức danh (Role) có sẵn trong hệ thống để điền vào dropdown tạo/sửa User.

- **URL:** `/api/v1/users/roles`
- **Method:** `GET`
- **Quyền yêu cầu:** `user.list`
- **Response:**
```json
[
  {
    "code": "SUPER_ADMIN",
    "name": "Super Admin",
    "description": "Quản trị viên cấp cao nhất hệ thống"
  },
  ...
]
```

## 2. Lấy danh sách Permission (Tất cả)
Lấy danh sách các quyền (Permission) có sẵn trong hệ thống.

- **URL:** `/api/v1/users/permissions`
- **Method:** `GET`
- **Quyền yêu cầu:** `user.list`
- **Response:**
```json
[
  {
    "code": "user.create",
    "description": "Tạo tài khoản mới",
    "isGranted": false
  },
  ...
]
```

## 3. Lấy danh sách User (Có phân trang)

- **URL:** `/api/v1/users`
- **Method:** `GET`
- **Quyền yêu cầu:** `user.list`
- **Query Params:**
  - `page` (int): Trang số mấy (mặc định 0)
  - `size` (int): Số lượng trên một trang (mặc định 20)
  - `sort` (string): Cột sort và chiều sort, mặc định `createdAt,desc`
- **Response:** Dạng `Page<UserResponse>` của Spring Boot.
```json
{
  "content": [
    {
      "id": 1,
      "fullName": "Nguyen Van A",
      "workEmail": "a@abc.com",
      "position": "Nhân viên",
      "phoneNumber": "0123456789",
      "status": "ACTIVE",
      "roleCode": "ADMIN",
      "roleName": "Admin",
      "rankLevel": "Senior",
      "specialty": "Luật Doanh Nghiệp",
      "yearsOfExperience": 5,
      "createdAt": "2024-01-01T10:00:00Z"
    }
  ],
  "pageable": { ... },
  "totalElements": 1,
  "totalPages": 1
}
```

## 4. Tạo User mới

- **URL:** `/api/v1/users`
- **Method:** `POST`
- **Quyền yêu cầu:** `user.create`
- **Request Body:**
```json
{
  "fullName": "Nguyen Van B",
  "workEmail": "b@abc.com",
  "phoneNumber": "0987654321",
  "position": "Thực tập sinh",
  "roleCode": "INTERN_LAWYER",
  "password": "mySecurePassword"
}
```
*Lưu ý:* Có thể truyền thêm `password` để thiết lập mật khẩu trực tiếp. Nếu không truyền, mật khẩu mặc định khi tạo mới là `123456`. Status mặc định là `ACTIVE`.

## 5. Cập nhật thông tin User (Dành cho Admin)

- **URL:** `/api/v1/users/{id}`
- **Method:** `PUT`
- **Quyền yêu cầu:** `user.update`
- **Request Body:** Truyền vào các trường muốn đổi (không bắt buộc truyền đủ).
```json
{
  "fullName": "Nguyen Van B Updated",
  "phoneNumber": "0987654322",
  "position": "Luật sư chính",
  "roleCode": "LAWYER"
}
```

## 6. Cập nhật thông tin cá nhân (Dành cho Account đang đăng nhập)

- **URL:** `/api/v1/users/me`
- **Method:** `PUT`
- **Quyền yêu cầu:** Không yêu cầu quyền riêng (chỉ cần đăng nhập hợp lệ).
- **Request Body:**
```json
{
  "rankLevel": "Senior",
  "specialty": "Luật Dân Sự",
  "yearsOfExperience": 5
}
```

## 7. Khóa / Mở khóa tài khoản

- **URL:** `/api/v1/users/{id}/status`
- **Method:** `PATCH`
- **Quyền yêu cầu:** `user.update_status`
- **Request Body:**
```json
{
  "status": "INACTIVE"
}
```

## 8. Danh bạ luật sư (Lawyer Directory)

Danh bạ luật sư cho phép tất cả các người dùng đã đăng nhập có thể xem thông tin.

### 8.1 Xem danh sách luật sư (và Tìm kiếm/Lọc)
- **URL:** `/api/v1/directory/lawyers`
- **Method:** `GET`
- **Quyền yêu cầu:** Bất kỳ ai đã đăng nhập đều xem được (Không cần quyền Admin).

**Cách sử dụng (Gửi dữ liệu lên URL):**
Để tìm kiếm hoặc lọc, bạn gắn thêm các từ khóa vào sau dấu `?` trên đường dẫn URL. Bạn có thể truyền 1 hoặc nhiều điều kiện cùng lúc. Nếu không truyền gì, API sẽ trả về toàn bộ danh sách.

*Các tiêu chí có thể dùng:*
- `keyword`: Tìm từ khóa tự do (Hệ thống sẽ tự động tìm trùng khớp với Tên, Số điện thoại hoặc Email).
- `rankLevel`: Lọc chính xác theo Cấp bậc.
- `specialty`: Lọc chính xác theo Chuyên môn.
- `yearsOfExperience`: Lọc chính xác theo Số năm kinh nghiệm (điền số).
- `page`: Trang số mấy (đếm từ 0).
- `size`: Lấy bao nhiêu người trên 1 trang (mặc định 10).

*Ví dụ thực tế:*
- **Lấy tất cả luật sư (Trang 1, mỗi trang 10 người):** 
  `GET /api/v1/directory/lawyers`
- **Tìm các luật sư tên là "Nguyen" (Trang 1):** 
  `GET /api/v1/directory/lawyers?keyword=Nguyen`
- **Lọc các luật sư có chuyên môn "Dân sự" và Cấp bậc "Senior":**
  `GET /api/v1/directory/lawyers?specialty=Dân sự&rankLevel=Senior`
- **Tìm "Nguyen", chuyên môn "Dân sự", lấy trang số 2 (mỗi trang 5 người):**
  `GET /api/v1/directory/lawyers?keyword=Nguyen&specialty=Dân sự&page=1&size=5`

- **Response:**
```json
{
  "content": [
    {
      "id": 2,
      "fullName": "Nguyen Van B",
      "rankLevel": "Senior",
      "specialty": "Luật Doanh Nghiệp",
      "yearsOfExperience": 5
    }
  ],
  "pageable": { ... },
  "totalElements": 1,
  "totalPages": 1,
  ...
}
```

### 8.2 Xem chi tiết 1 luật sư
- **URL:** `/api/v1/directory/lawyers/{id}`
- **Method:** `GET`
- **Quyền yêu cầu:** Không yêu cầu quyền riêng (chỉ cần đăng nhập hợp lệ).
- **Response:** Trả về tất cả thông tin chi tiết của luật sư (trừ password).
```json
{
  "id": 2,
  "fullName": "Nguyen Van B",
  "workEmail": "b@abc.com",
  "position": "Luật sư chính",
  "phoneNumber": "0987654321",
  "status": "ACTIVE",
  "roleCode": "LAWYER",
  "roleName": "Luật sư",
  "rankLevel": "Senior",
  "specialty": "Luật Doanh Nghiệp",
  "yearsOfExperience": 5,
  "createdAt": "2024-01-01T10:00:00Z"
}
```

## 9. Xem danh sách Quyền của 1 User
Dùng để render giao diện checklist phân quyền, trả về toàn bộ Quyền trong hệ thống, quyền nào User đang có thì `isGranted = true`.

- **URL:** `/api/v1/users/{id}/permissions`
- **Method:** `GET`
- **Quyền yêu cầu:** `user.view`
- **Response:**
```json
[
  {
    "code": "user.list",
    "description": "Xem danh sách tài khoản",
    "isGranted": true
  },
  {
    "code": "contract_template.create",
    "description": "Tạo mẫu hợp đồng",
    "isGranted": false
  }
]
```

## 8. Cấp quyền cho User (Ghi đè hoàn toàn)
API này sẽ xóa tất cả các quyền cũ của User và thiết lập lại theo danh sách mới được gửi lên.

- **URL:** `/api/v1/users/{id}/permissions`
- **Method:** `PUT`
- **Quyền yêu cầu:** `user.update`
- **Request Body:**
```json
{
  "permissionCodes": ["user.list", "user.view"]
}
```
- **Response:** `200 OK` (Không có body).

## 9. Xóa tài khoản

- **URL:** `/api/v1/users/{id}`
- **Method:** `DELETE`
- **Quyền yêu cầu:** `user.delete`
- **Response:** `200 OK` (Không có body).

---

## Danh sách Mã Lỗi (Error Codes)
API trả về lỗi với cấu trúc bao gồm `status`, `code`, và `message`. Frontend có thể bắt theo `code` để translate thông báo lỗi.

- `VALIDATION_FAILED` (400): Dữ liệu gửi lên không hợp lệ (ví dụ thiếu họ tên, email sai format). Chi tiết lỗi sẽ nằm trong field `errors`.
- `USER_ALREADY_EXISTS` (400): Bị trùng Email hoặc Số điện thoại.
- `NOT_FOUND` (404): Không tìm thấy User, Role hoặc Permission tương ứng.
- `FORBIDDEN` (403): User đang đăng nhập không có quyền (`permissionChecker`) thực hiện hành động này.
- `BAD_REQUEST` (400): Các lỗi logic nghiệp vụ chung.
- `INTERNAL_ERROR` (500): Lỗi do server.

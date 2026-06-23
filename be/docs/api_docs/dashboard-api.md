# Dashboard API

Thống kê Dashboard cho người dùng và Admin.

## 1. Lấy số liệu toàn hệ thống (Dành cho Admin)
- **Endpoint:** `/api/v1/dashboard/admin-stats`
- **Method:** `GET`
- **Quyền yêu cầu:** `ADMIN` hoặc `SUPER_ADMIN`. (Nếu role khác gọi sẽ bị `403 Forbidden`).

### Response Thành công (200 OK)
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "totalCases": 150,
    "totalCaseValue": 1500000000.00,
    "totalContracts": 120
  }
}
```

## 2. Lấy số liệu cá nhân (Dành cho Luật sư, Partner)
- **Endpoint:** `/api/v1/dashboard/my-stats`
- **Method:** `GET`
- **Quyền yêu cầu:** Bất kỳ người dùng nào đã đăng nhập (`LAWYER`, `PARTNER`, ...). Trả về số liệu liên quan đến người đó.

### Response Thành công (200 OK)
```json
{
  "status": 200,
  "message": "Thành công",
  "data": {
    "totalCases": 5,
    "totalCaseValue": 50000000.00,
    "totalContracts": 2
  }
}
```

## Giải thích các thông số
- `totalCases`: 
  - Admin: Tổng số vụ việc trong toàn bộ hệ thống.
  - User: Tổng số vụ việc do user này tạo (user là `assignedLawyer`).
- `totalCaseValue`:
  - Admin: Tổng giá trị của tất cả vụ việc (`caseValue`).
  - User: Tổng giá trị của các vụ việc do user này tạo.
- `totalContracts`:
  - Admin: Tổng số hợp đồng trên toàn hệ thống.
  - User: Tổng số hợp đồng do user này tạo (`createdBy`).

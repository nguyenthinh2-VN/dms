# Implementation Plan: Dashboard API

## Overview
Xây dựng tính năng Dashboard (Trang chủ) cung cấp các API trả về số liệu thống kê. Dựa trên yêu cầu tách biệt, hệ thống sẽ cung cấp **2 API riêng biệt** thay vì gom chung:
1. **API dành cho ADMIN / SUPER_ADMIN**: Trả về thống kê tổng trên toàn bộ hệ thống.
2. **API dành cho LAWYER / PARTNER**: Trả về thống kê cá nhân của chính người dùng đó.

Các chỉ số cần lấy trong mỗi API:
1. **Tổng số vụ việc** (Total Cases)
2. **Tổng giá trị vụ việc** (Total Case Value)
3. **Tổng số hợp đồng** (Total Contracts)

## Architecture Decisions
- Tạo mới `DashboardController` chứa 2 endpoint riêng biệt:
    - `GET /api/v1/dashboard/admin-stats`
    - `GET /api/v1/dashboard/my-stats`
- Tạo mới `DashboardUseCase` chứa các hàm xử lý độc lập cho 2 luồng này.
- Dữ liệu trả về sử dụng DTO `DashboardResponse` dùng chung cấu trúc (gồm 3 trường `totalCases`, `totalCaseValue`, `totalContracts`), nhưng dữ liệu bên trong khác nhau tuỳ API.
- **Truy vấn DB:** Thêm các hàm đếm (`countBy...`) và tính tổng (`@Query("SELECT SUM(...)")`) vào `SpringDataLegalCaseRepository` và `SpringDataContractRepository` để tính toán dưới DB.

## Task List

### Phase 1: Foundation (Data & DTOs)
- [ ] Task 1: Tạo class DTO `DashboardResponse` với 3 trường: `totalCases` (Long), `totalCaseValue` (BigDecimal), `totalContracts` (Long).

### Checkpoint: Foundation
- [ ] Class DTO đã sẵn sàng.

### Phase 2: Core Features (Repository & UseCase)
- [ ] Task 2: Cập nhật `SpringDataLegalCaseRepository`:
    - Thêm query tính tổng `caseValue` toàn hệ thống (cho Admin).
    - Thêm query tính tổng `caseValue` theo `assignedLawyer.id` (cho User).
    - Query lấy tổng số vụ việc theo `assignedLawyer.id` (dùng hàm count).
- [ ] Task 3: Cập nhật `SpringDataContractRepository`:
    - Thêm query đếm số lượng hợp đồng theo `createdBy`.
- [ ] Task 4: Triển khai `DashboardUseCase.java`. Viết 2 hàm độc lập:
    - `getAdminStats()`: Gọi các query toàn hệ thống.
    - `getMyStats(Long userId)`: Gọi các query theo `userId`.

### Checkpoint: Core Features
- [ ] Code logic đã hoàn thiện và compile thành công.

### Phase 3: API Endpoint (Controller)
- [ ] Task 5: Tạo `DashboardController.java` với 2 endpoint:
    - `GET /api/v1/dashboard/admin-stats` (Kiểm tra quyền ADMIN/SUPER_ADMIN).
    - `GET /api/v1/dashboard/my-stats` (Không cần quyền Admin, lấy userId từ token hiện tại).
- [ ] Task 6: Cập nhật tài liệu API (file markdown) báo cho FE biết 2 endpoint này.

### Checkpoint: Complete
- [ ] Sẵn sàng test và tích hợp.

## Verification Plan
- Dùng tài khoản `ADMIN` gọi API `/admin-stats` -> Phải ra tổng toàn hệ thống. Cố gọi `/my-stats` cũng chỉ ra số liệu cá nhân của Admin đó (nếu có).
- Dùng tài khoản `LAWYER` gọi API `/my-stats` -> Phải ra số vụ việc, hợp đồng, và giá trị họ tự tạo.
- Dùng tài khoản `LAWYER` gọi API `/admin-stats` -> Phải bị chặn lỗi `403 Forbidden`.

# Nhắc Hạn Cá Nhân API (Personal Reminders)

API này dùng để quản lý các nhắc hạn (reminders) do user tự tạo dựa trên các vụ việc mà họ đang tham gia. 
Mỗi nhắc hạn chỉ được xem và quản lý bởi user tạo ra nó. Nhắc hạn có thể được đánh dấu là "đã hoàn thành" để ẩn đi.

---

## 1. Tạo Nhắc hạn mới
- **URL:** `/api/v1/reminders`
- **Method:** `POST`
- **Request Body (JSON):**
```json
{
  "legalCaseId": 10,
  "deadline": "25-06-2026",
  "note": "Cần nộp hồ sơ bổ sung lên tòa án."
}
```
> **Lưu ý:**
> - `legalCaseId`: Bắt buộc. ID của vụ việc liên quan.
> - `deadline`: Bắt buộc. Định dạng chuỗi `dd-MM-yyyy`. Hệ thống sẽ tự động chuyển mốc thời gian về `23:59:59` (cuối ngày) của ngày đó.
> - `note`: Tuỳ chọn. Nội dung ghi chú nhắc nhở.

- **Response (200 OK):**
```json
{
  "status": 200,
  "message": "Tạo nhắc hạn thành công",
  "data": {
    "id": 1,
    "legalCaseId": 10,
    "legalCaseTitle": "Tranh chấp đất đai tại Quận 1",
    "deadline": "2026-06-25T23:59:59+07:00",
    "note": "Cần nộp hồ sơ bổ sung lên tòa án.",
    "isCompleted": false,
    "createdAt": "2026-06-19T10:00:00Z"
  }
}
```

---

## 2. Lấy 3 Nhắc hạn sắp tới (Upcoming Reminders)
API này dùng để FE gọi nhằm lấy danh sách các nhắc hạn sắp tới để hiển thị thông báo (topup) cho user.
Nó chỉ trả về tối đa **3 nhắc hạn gần nhất**, có deadline **trong tương lai** (so với thời điểm hiện tại), và **chưa được đánh dấu hoàn thành** (`isCompleted = false`). Danh sách được sắp xếp theo thời hạn tăng dần (gần nhất lên đầu).

- **URL:** `/api/v1/reminders/upcoming`
- **Method:** `GET`
- **Response (200 OK):**
```json
{
  "status": 200,
  "message": "Thành công",
  "data": [
    {
      "id": 1,
      "legalCaseId": 10,
      "legalCaseTitle": "Tranh chấp đất đai tại Quận 1",
      "deadline": "2026-06-25T23:59:59+07:00",
      "note": "Cần nộp hồ sơ bổ sung lên tòa án.",
      "isCompleted": false,
      "createdAt": "2026-06-19T10:00:00Z"
    },
    {
      "id": 2,
      "legalCaseId": 12,
      "legalCaseTitle": "Hợp đồng kinh tế công ty A",
      "deadline": "2026-06-28T23:59:59+07:00",
      "note": "Họp chốt điều khoản bảo mật.",
      "isCompleted": false,
      "createdAt": "2026-06-19T10:05:00Z"
    }
  ]
}
```

---

## 3. Đánh dấu Hoàn thành Nhắc hạn
Khi user đã làm xong việc được nhắc, FE có thể gọi API này để đánh dấu hoàn thành, ẩn nhắc hạn đó đi khỏi danh sách Upcoming.

- **URL:** `/api/v1/reminders/{id}/complete`
- **Method:** `PUT`
- **Path Variables:**
  - `id`: ID của nhắc hạn.
- **Response (200 OK):**
```json
{
  "status": 200,
  "message": "Đánh dấu hoàn thành nhắc hạn thành công"
}
```
- **Lỗi có thể trả về:**
  - `400 Bad Request`: `{"status": 400, "message": "Không tìm thấy nhắc hạn."}`
  - `403 Forbidden`: `{"status": 403, "message": "Không có quyền truy cập."}` (nếu cố tình đánh dấu nhắc hạn của user khác).

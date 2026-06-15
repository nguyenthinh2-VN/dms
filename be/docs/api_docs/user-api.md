# User API Docs

## 1. Lấy danh sách bạn bè đã giới thiệu (Referrals)

**Endpoint:** `GET /api/v1/users/me/referrals`

**Mô tả:** Lấy mã giới thiệu của người dùng hiện tại, link giới thiệu, và danh sách những người dùng khác đã đăng ký thành công thông qua mã giới thiệu đó.

**Yêu cầu Xác thực:** 
- Phải truyền Token trong Header: `Authorization: Bearer <Access Token>`

**Response (Success 200 OK):**
```json
{
  "status": "success",
  "data": {
    "myReferralCode": "A8X2B9",
    "referralLink": "http://localhost:3000/register?ref=A8X2B9",
    "totalReferred": 2,
    "referredUsers": [
      {
        "id": 2,
        "fullName": "Nguyen Van B",
        "email": "userb@example.com",
        "createdAt": "2026-06-15T10:00:00Z"
      },
      {
        "id": 3,
        "fullName": "Tran Thi C",
        "email": "userc@example.com",
        "createdAt": "2026-06-15T11:00:00Z"
      }
    ]
  }
}
```

**Response (Error 401 Unauthorized):**
- Trả về khi không có JWT Token hoặc Token hết hạn/không hợp lệ.

# KẾ HOẠCH: API DANH SÁCH VỤ VIỆC RÚT GỌN (CASE LIST FOR DROPDOWN)

Plan hỗ trợ cho [`contract-from-template.md`](contract-from-template.md): cung cấp endpoint danh sách vụ việc rút gọn để FE đổ vào dropdown khi user gắn hợp đồng vào vụ việc.

> **Bối cảnh:** Module Quản lý vụ việc (`legal_cases`) đã có endpoint `GET /api/v1/cases` trả `CaseResponse` đầy đủ (kèm tính toán hoa hồng, tiền Net, danh sách người được phân công). Endpoint này không phù hợp để đổ dropdown: payload nặng, có những trường nhạy cảm về tài chính. Cần tách 1 endpoint nhẹ riêng cho mục đích chọn vụ việc trong các form (tạo hợp đồng, sau này có thể là tạo task, attach file…).

---

## PHẦN 1: TỔNG QUAN

### 1.1. Mục tiêu

- Cung cấp endpoint trả về danh sách vụ việc dạng **rút gọn** (`{id, generatedTitle, title, status}`) để FE render dropdown / autocomplete.
- Tôn trọng scope quyền giống `GET /api/v1/cases`:
  - `ADMIN` / `MANAGER_LAWYER` (hoặc có permission `case.list.all`) → xem hết.
  - User khác → chỉ thấy vụ việc mình có liên quan.
- Hỗ trợ tìm kiếm theo từ khóa và lọc theo status để FE làm autocomplete khi danh sách nhiều.

### 1.2. Tech Stack

Không thêm thư viện. Dùng đúng stack hiện tại.

### 1.3. Cấu trúc mã nguồn

```
application/dto/
  CaseLookupResponse.java            # rút gọn: {id, generatedTitle, title, status, statusLabel}

application/usecase/
  CaseUseCase.java                   # thêm method getCaseLookup(...)

domain/repository/
  LegalCaseRepository.java           # thêm method search(...)

infrastructure/persistence/repository/
  SpringDataLegalCaseRepository.java # thêm @Query
  JpaLegalCaseRepositoryImpl.java    # adapter

presentation/controller/
  CaseController.java                # thêm GET /api/v1/cases/lookup
```

---

## PHẦN 2: API CHI TIẾT

### API: Danh sách vụ việc rút gọn (cho dropdown)

- **Endpoint:** `GET /api/v1/cases/lookup`
- **Query params (tất cả tùy chọn):**
  - `keyword` — search theo `title` hoặc `generatedTitle` (LIKE `%kw%`, case-insensitive).
  - `status` — lọc theo `CaseStatus` (vd `IN_PROGRESS`, `CONTRACT_SIGNED`).
  - `limit` — mặc định `50`, max `200`.
- **Permission:** `case.list` (xem [`permission-keys.md`](../docs/contract/permission-keys.md)).
- **Scope đọc:**
  - Có `case.list.all` → trả toàn bộ matching.
  - Không có → chỉ trả vụ việc mà user là `assignedLawyer` / `partner` / `internLawyer` / `trainee` (dùng lại `findByRelatedUserId` + filter trên service).
- **Response:**
  ```json
  {
    "status": 200,
    "message": "Thành công",
    "data": [
      {
        "id": 42,
        "generatedTitle": "Tranh chấp đất đai_Tư vấn_150626_Nguyen Van A",
        "title": "Tranh chấp đất đai",
        "status": "IN_PROGRESS",
        "statusLabel": "Đang xử lý"
      }
    ]
  }
  ```
- **Lỗi:** `403` không đủ permission.

> Lý do tách endpoint riêng (không sửa `GET /api/v1/cases` cũ): tránh breaking change cho FE màn hình danh sách vụ việc đầy đủ; payload và quyền hiển thị khác nhau (lookup KHÔNG trả `caseValue`, hoa hồng, tiền Net).

---

## PHẦN 3: DTO

```java
// application/dto/CaseLookupResponse.java
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class CaseLookupResponse {
    private Long id;
    private String generatedTitle;
    private String title;
    private String status;       // enum name, ví dụ "IN_PROGRESS"
    private String statusLabel;  // i18n: lấy từ CaseStatus.getDescription() + MessageUtils
}
```

---

## PHẦN 4: TASK BREAKDOWN

### Phase 1: Repository
- [ ] `SpringDataLegalCaseRepository`: thêm `@Query` JPQL search:
  ```java
  @Query("""
    SELECT c FROM LegalCaseJpaEntity c
    WHERE (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(c.generatedTitle) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:status IS NULL OR c.status = :status)
    ORDER BY c.createdAt DESC
  """)
  List<LegalCaseJpaEntity> searchAll(@Param("keyword") String keyword,
                                     @Param("status") CaseStatus status,
                                     Pageable pageable);

  @Query("""
    SELECT c FROM LegalCaseJpaEntity c
    WHERE (c.assignedLawyer.id = :userId OR c.partner.id = :userId
           OR c.internLawyer.id = :userId OR c.trainee.id = :userId)
      AND (:keyword IS NULL OR LOWER(c.title) LIKE LOWER(CONCAT('%', :keyword, '%'))
           OR LOWER(c.generatedTitle) LIKE LOWER(CONCAT('%', :keyword, '%')))
      AND (:status IS NULL OR c.status = :status)
    ORDER BY c.createdAt DESC
  """)
  List<LegalCaseJpaEntity> searchByRelatedUser(@Param("userId") Long userId,
                                               @Param("keyword") String keyword,
                                               @Param("status") CaseStatus status,
                                               Pageable pageable);
  ```
- [ ] `LegalCaseRepository` (domain port): thêm 2 method tương ứng `searchAll(...)`, `searchByRelatedUser(...)`.
- [ ] `JpaLegalCaseRepositoryImpl`: implement 2 method, mapping `LegalCaseJpaEntity → LegalCase`.

### Phase 2: UseCase + Controller
- [ ] `CaseUseCase`: thêm method
  ```java
  public List<CaseLookupResponse> getCaseLookup(String keyword, String status,
                                                Integer limit, User currentUser) {
      permissionChecker.requirePermission(currentUser, "case.list");
      int safeLimit = Math.min(limit != null ? limit : 50, 200);
      CaseStatus statusEnum = status != null ? CaseStatus.valueOf(status) : null;

      List<LegalCase> cases;
      if (permissionChecker.hasPermission(currentUser, "case.list.all")) {
          cases = legalCaseRepository.searchAll(keyword, statusEnum, safeLimit);
      } else {
          cases = legalCaseRepository.searchByRelatedUser(currentUser.getId(),
                                                          keyword, statusEnum, safeLimit);
      }
      return cases.stream().map(this::toLookupResponse).toList();
  }

  private CaseLookupResponse toLookupResponse(LegalCase c) {
      return CaseLookupResponse.builder()
          .id(c.getId())
          .generatedTitle(c.getGeneratedTitle())
          .title(c.getTitle())
          .status(c.getStatus() != null ? c.getStatus().name() : null)
          .statusLabel(c.getStatus() != null
              ? MessageUtils.getMessage("CASE_STATUS_" + c.getStatus().name(),
                                        c.getStatus().getDescription())
              : null)
          .build();
  }
  ```
  > **Lưu ý:** dùng `permissionChecker` đã thêm ở Phase 0 của [`contract-template.md`](contract-template.md). Nếu Phase 0 chưa xong → tạm fallback `roleName.equals("ADMIN") || roleName.equals("MANAGER_LAWYER")`, ghi chú `// TODO: replace with permissionChecker` để dễ refactor.
- [ ] `CaseController`: thêm endpoint
  ```java
  @GetMapping("/lookup")
  public ResponseEntity<Map<String, Object>> getCaseLookup(
          @RequestParam(required = false) String keyword,
          @RequestParam(required = false) String status,
          @RequestParam(required = false) Integer limit,
          Authentication authentication) {
      User currentUser = (User) authentication.getPrincipal();
      List<CaseLookupResponse> data = caseUseCase.getCaseLookup(keyword, status, limit, currentUser);
      // envelope chuẩn {status, message, data}
  }
  ```

### Phase 3: Doc + Test
- [ ] Cập nhật `be/docs/api_docs/case-api.md`: thêm mục **8. Lấy danh sách vụ việc rút gọn (Lookup)** với request/response mẫu.
- [ ] Bổ sung permission `case.list` (đã có ngầm) và `case.list.all` vào seed nếu chưa có (xem `permission-keys.md`).
- [ ] Unit test `CaseUseCase.getCaseLookup`:
  - Admin có `case.list.all` → trả hết.
  - User thường → chỉ trả vụ việc liên quan.
  - Filter `keyword` + `status` hoạt động.
  - `limit` clamp ở 200.

---

## PHẦN 5: KIỂM THỬ

1. `./mvnw clean compile`.
2. Postman với token Admin: `GET /api/v1/cases/lookup?keyword=tranh chấp&status=IN_PROGRESS&limit=20` → trả mảng matching.
3. Postman với token user thường (không phải Admin): cùng query → chỉ trả vụ việc user có liên quan.
4. Test FE tạo hợp đồng (UC2): dropdown "Vụ việc liên kết" gọi endpoint này, nhập keyword → autocomplete OK.
5. Token user không có permission `case.list` → `403`.

---

## KẾT LUẬN

Plan này nhỏ, độc lập, có thể code song song với UC1. Cung cấp đầu vào quan trọng cho UC2 (`legalCaseId` ở `POST /api/v1/contracts`). Không phá vỡ endpoint `GET /api/v1/cases` cũ.

*Tài liệu này là bản chốt cho API danh sách vụ việc rút gọn.*
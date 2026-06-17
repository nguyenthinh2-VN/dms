# KẾ HOẠCH TỔNG THỂ: TẠO HỢP ĐỒNG TỪ MẪU (CONTRACT FROM TEMPLATE)

Tài liệu chốt cuối cùng cho **UC2 — Tạo hợp đồng từ mẫu**: chọn mẫu, FE render form động từ field schema, user nhập data, preview, lưu → hệ thống xuất ra **cả Word (.docx) và PDF**, đồng thời cho phép gắn vào vụ việc.

> **Tài liệu tham chiếu (đọc trước khi code):**
> - Quy ước đặt tên biến `{{...}}`: xem [`be/docs/contract/quy-uoc-dat-ten-bien.md`](../docs/contract/quy-uoc-dat-ten-bien.md).
> - Quy ước Permission Key & RBAC động: xem [`be/docs/contract/permission-keys.md`](../docs/contract/permission-keys.md).
> - Plan tiền đề (phải hoàn tất trước): [`contract-template.md`](contract-template.md).
> - Plan hỗ trợ (cho dropdown chọn vụ việc): [`case-list-api.md`](case-list-api.md).

---

## PHẦN 1: TỔNG QUAN (SPECIFICATION)

### 1.1. Mục tiêu (Objective)

Cho phép Luật sư / Admin tạo hợp đồng từ mẫu đã có:
1. Lấy danh sách mẫu `ACTIVE` → chọn 1 mẫu.
2. FE render form input động từ field schema của mẫu (Cách 1 — Dynamic hoàn toàn).
3. User nhập data; tùy chọn gắn `legalCaseId` (vụ việc liên quan).
4. Preview HTML → user xác nhận → lưu.
5. Hệ thống merge data vào master `.docx` → xuất `.docx` + `.pdf` → lưu snapshot.
6. User tải về cả 2 định dạng. Hợp đồng được tham chiếu ngược về vụ việc (nếu có gắn).

### 1.2. Công nghệ (Tech Stack)

- **Backend:** Spring Boot 4.1.0, Java 17 (Hexagonal).
- **Database:** MySQL.
- **Tài liệu Word → Word merge:** `org.docx4j:docx4j-JAXB-ReferenceImpl` (`VariablePrepare` + replace `{{key}}`).
- **Word → PDF:** `org.docx4j:docx4j-export-fo` (Apache FOP) — **chốt**: pipeline `.docx` → PDF qua FOP để Word/PDF khớp 100% (cùng 1 nguồn `.docx`, không qua HTML trung gian).
- **Font tiếng Việt cho PDF:** cấu hình font embed cho FOP (vd Noto Sans / Times New Roman) ở `fop.xconf`.

### 1.3. Cấu trúc mã nguồn

```
domain/entity/
  Contract.java                      # POJO domain
  ContractData.java
  ContractSnapshot.java
  ContractStatus.java                # enum DRAFT / FINALIZED

domain/repository/
  ContractRepository.java
  ContractDataRepository.java
  ContractSnapshotRepository.java

application/dto/
  PreviewContractRequest.java        # { templateId, data }
  CreateContractRequest.java         # { templateId, legalCaseId?, data }
  ContractResponse.java
  ContractListItemResponse.java

application/service/
  DocxMergeService.java              # docx4j merge {{key}} → data → .docx
  DocxToPdfConverter.java            # docx4j-export-fo → PDF
  ContractNumberGenerator.java       # format: HD_{template_code}_{ddMMyy}_{creator}
  FieldDataValidator.java            # validate required + type per fieldType

application/usecase/
  ContractUseCase.java               # preview / create / list / getById / download

infrastructure/persistence/entity/
  ContractJpaEntity.java
  ContractDataJpaEntity.java
  ContractSnapshotJpaEntity.java

infrastructure/persistence/repository/
  Spring­Data + Jpa...RepositoryImpl (3 cặp)

infrastructure/pdf/
  fop.xconf                          # cấu hình FOP (font tiếng Việt)

presentation/controller/
  ContractController.java            # /api/v1/contracts
```

---

## PHẦN 2: THIẾT KẾ DATABASE (ENTITY)

### 2.1. `contracts`

| Field | Kiểu | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `contractNo` | `String` | unique. Format: `HD_{templateCode}_{ddMMyy}_{creatorFullName}`. Sanitize space → `_`, bỏ dấu tiếng Việt. |
| `template_id` | `ManyToOne → contract_templates` | FK |
| `templateVersion` | `int` | Snapshot version lúc tạo — KHÔNG bị ảnh hưởng khi mẫu sửa sau. |
| `legalCase_id` | `ManyToOne → legal_cases` (nullable) | Vụ việc liên kết. Tùy chọn. |
| `status` | enum `ContractStatus` | `DRAFT` / `FINALIZED`. Mặc định `FINALIZED` khi save thành công. |
| `createdBy` | `ManyToOne → users` | Người tạo. |
| `createdAt`, `updatedAt` | `OffsetDateTime` | Auto. |

### 2.2. `contract_data`

| Field | Kiểu | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `contract_id` | `OneToOne → contracts` (unique) | FK |
| `dataJson` | `TEXT` | Toàn bộ data dạng JSON: `{"party_a_name":"Công ty ABC", ...}`. Phục vụ Cách 1 — Dynamic hoàn toàn. |

### 2.3. `contract_snapshots`

| Field | Kiểu | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `contract_id` | `ManyToOne → contracts` | FK |
| `renderedHtml` | `TEXT` | HTML đã merge (cho FE xem nhanh). |
| `docxPath` | `String` | Đường dẫn `.docx` đã xuất. |
| `pdfPath` | `String` | Đường dẫn `.pdf` đã xuất. |
| `createdAt` | `OffsetDateTime` | Auto. |

> Tách `contracts` ↔ `contract_data` ↔ `contract_snapshots` để: (1) data input và file output rõ ràng; (2) sau này muốn tạo lại snapshot không phải động vào bảng chính.

---

## PHẦN 3: CÁC API CHI TIẾT SẼ PHÁT TRIỂN

> Envelope chuẩn: `{ "status", "message", "data" }`. Lỗi 403 dùng `ForbiddenException` (đã thêm ở Phase 0 của UC1).

### API 1: Lấy schema mẫu để render form
- **Endpoint:** sử dụng lại `GET /api/v1/contract-templates/{id}` (đã làm ở UC1).
- **Mục đích:** FE đọc `data.fields[]` → render input động (`fieldType` quyết định widget: TEXT/NUMBER/DATE picker/MONEY format/PARAGRAPH textarea).

### API 2: Preview hợp đồng (không lưu)
- **Endpoint:** `POST /api/v1/contracts/preview`
- **Permission:** `contract.create`.
- **Request body:**
  ```json
  {
    "templateId": 1,
    "data": {
      "party_a_name": "Công ty ABC",
      "party_a_tax_code": "0123456789",
      "contract_value": 100000000
    }
  }
  ```
- **Luồng xử lý:**
  1. Check permission `contract.create`.
  2. Lấy mẫu — phải `ACTIVE`.
  3. Validate data theo field schema (xem PHẦN 4).
  4. Load master `.docx` → `VariablePrepare.prepare()` → replace `{{key}}` bằng giá trị (escape XML đầy đủ).
  5. Convert kết quả → HTML (docx4j HTML export) trả về.
- **Response data:** `{ "renderedHtml": "<html>..." }`.
- **Lỗi:** `400` thiếu/sai field · `400` mẫu đã ARCHIVED · `403`.

### API 3: Tạo hợp đồng (lưu + xuất file)
- **Endpoint:** `POST /api/v1/contracts`
- **Permission:** `contract.create`.
- **Request body:**
  ```json
  {
    "templateId": 1,
    "legalCaseId": 42,
    "data": { "party_a_name": "Công ty ABC", "contract_value": 100000000, "...": "..." }
  }
  ```
- **Luồng xử lý:**
  1. Check permission. Validate template `ACTIVE`. Validate data (PHẦN 4).
  2. Nếu có `legalCaseId` → kiểm tra vụ việc tồn tại + user có quyền view (dùng cùng `hasAccess` của `CaseUseCase`).
  3. Sinh `contractNo` qua `ContractNumberGenerator`: `HD_{templateCode}_{ddMMyy}_{creatorFullName}` — sanitize (bỏ dấu tiếng Việt, replace space `_`). Nếu trùng → thêm hậu tố `_{n}` (n bắt đầu 2).
  4. Load master `.docx` → merge `{{key}}` → ghi `.docx` final ra `contracts/{contractNo}.docx`.
  5. Convert `.docx` → PDF qua docx4j-export-fo → `contracts/{contractNo}.pdf`.
  6. Convert merged → HTML để lưu snapshot (FE xem nhanh).
  7. Persist `Contract` (status `FINALIZED`, snapshot `templateVersion`) + `ContractData` (`dataJson`) + `ContractSnapshot` (3 path + html).
- **Response (`201`):**
  ```json
  {
    "status": 201,
    "message": "Thành công",
    "data": {
      "id": 7,
      "contractNo": "HD_HD_DICH_VU_PL_150626_NguyenVanA",
      "templateId": 1,
      "templateVersion": 2,
      "legalCaseId": 42,
      "status": "FINALIZED",
      "downloadUrl": {
        "docx": "/api/v1/contracts/7/download/docx",
        "pdf":  "/api/v1/contracts/7/download/pdf"
      },
      "createdAt": "..."
    }
  }
  ```
- **Lỗi:** `400` validate fail · `403` thiếu permission / không có quyền vụ việc.

### API 4: Danh sách hợp đồng
- **Endpoint:** `GET /api/v1/contracts?legalCaseId=&templateId=`
- **Permission:** `contract.list` (xem `permission-keys.md` để biết scope).
- **Scope đọc:**
  - Có permission `contract.list.all` (mặc định cấp ADMIN/MANAGER_LAWYER) → xem hết.
  - Chỉ có `contract.list` → chỉ xem hợp đồng do mình tạo HOẶC gắn với vụ việc mình có liên quan (dùng `LegalCaseRepository.findByRelatedUserId`).
- **Response:** mảng `ContractListItemResponse` (rút gọn).

### API 5: Chi tiết hợp đồng
- **Endpoint:** `GET /api/v1/contracts/{id}`
- **Permission:** `contract.view` + scope như API 4.
- **Response:** `ContractResponse` đầy đủ (gồm `data` đã nhập, link download, snapshot html).

### API 6: Tải file `.docx`
- **Endpoint:** `GET /api/v1/contracts/{id}/download/docx`
- **Permission:** `contract.view` + scope.
- **Response:** stream binary với `Content-Type: application/vnd.openxmlformats-officedocument.wordprocessingml.document` + `Content-Disposition: attachment; filename={contractNo}.docx`.

### API 7: Tải file `.pdf`
- **Endpoint:** `GET /api/v1/contracts/{id}/download/pdf`
- **Permission:** `contract.view` + scope.
- **Response:** stream binary với `Content-Type: application/pdf`.

### API 8: Danh sách hợp đồng theo vụ việc (tích hợp cho FE màn hình vụ việc)
- **Endpoint:** đã gộp vào API 4 (`?legalCaseId=...`) — không tạo endpoint riêng.

---

## PHẦN 4: VALIDATION DATA INPUT (FieldDataValidator)

Mỗi field theo `fieldType`:

| `fieldType` | Quy tắc validate | Lỗi nếu sai |
|---|---|---|
| `TEXT` | `required` → không null/blank. Length ≤ 1000. | `400 field "X" bắt buộc` |
| `PARAGRAPH` | Tương tự TEXT, length ≤ 10000. | |
| `NUMBER` | Parse `BigDecimal`. | `400 field "X" phải là số` |
| `MONEY` | Parse `BigDecimal`, ≥ 0. | |
| `DATE` | Parse ISO `yyyy-MM-dd`. | `400 field "X" sai định dạng ngày` |

`required = true` mà thiếu key trong `data` → `400`. Field thừa (key không có trong schema) → bỏ qua + log warning, KHÔNG ghi vào template (chống injection placeholder ngoài ý muốn).

Trả lỗi field-level dạng:
```json
{
  "status": 400,
  "message": "Validation failed",
  "errors": [
    { "field": "party_a_tax_code", "message": "Bắt buộc" },
    { "field": "contract_value", "message": "Phải là số" }
  ]
}
```

---

## PHẦN 5: PHÂN QUYỀN (RBAC ĐỘNG)

Tuân thủ [`permission-keys.md`](../docs/contract/permission-keys.md):

- `contract.create` · `contract.view` · `contract.list` · `contract.list.all` (scope toàn hệ thống) · `contract.update` (giai đoạn sau) · `contract.delete` (giai đoạn sau).

Mọi entry-point ở `ContractUseCase` đều gọi `permissionChecker.requirePermission(currentUser, "contract.xxx")` — KHÔNG hardcode `roleName.equals("ADMIN")`.

Scope vụ việc: nếu có `legalCaseId`, tái sử dụng logic `hasAccess` từ `CaseUseCase` (cân nhắc tách thành `CaseAccessChecker` để dùng chung).

---

## PHẦN 6: PIPELINE TÀI LIỆU

```
master .docx (ContractFileStorage)
        │
        ▼
docx4j WordprocessingMLPackage.load
        │
        ▼
VariablePrepare.prepare()   ← gộp run bị Word cắt vụn
        │
        ▼
Replace {{key}} → escape(data[key])   ← regex hoặc duyệt run thủ công
        │
        ├──► save .docx  →  contracts/{contractNo}.docx   (Word output)
        │
        ├──► docx4j-export-fo (Apache FOP) → PDF  →  contracts/{contractNo}.pdf
        │
        └──► docx4j HTML export → renderedHtml (lưu snapshot + trả preview)
```

**Lưu ý kỹ thuật:**
- `docx4j` hỗ trợ native `${key}` cho `variableReplace`. Ta xử lý `{{key}}` bằng cách: (a) sau `VariablePrepare`, duyệt run text, replace bằng regex `\{\{\s*KEY\s*\}\}` → giá trị; HOẶC (b) chuyển hết `{{key}}` → `${key}` trước khi gọi `variableReplace`. Phương án (a) an toàn hơn vì không phải sửa cấu trúc Word.
- Escape XML cho giá trị: `<`, `>`, `&`, `"`, `'`.
- Cấu hình FOP qua `fop.xconf` để embed font Unicode (Noto Sans Vietnamese hoặc Times New Roman) — đặt ở `infrastructure/pdf/fop.xconf`, load qua classpath.

---

## PHẦN 7: THƯ VIỆN BỔ SUNG (be/pom.xml)

UC1 đã thêm `docx4j-JAXB-ReferenceImpl`. UC2 thêm tiếp:

```xml
<dependency>
  <groupId>org.docx4j</groupId>
  <artifactId>docx4j-export-fo</artifactId>
  <version>11.4.11</version>
</dependency>
```

`application-dev.yaml` / `application-prod.yaml` (đã có `app.contract.storage-path` từ UC1, không cần thêm):
```yaml
app:
  contract:
    storage-path: ./storage/contracts   # từ UC1
    pdf:
      fop-config: classpath:fop.xconf
```

---

## PHẦN 8: TASK BREAKDOWN

> **Tiền điều kiện:** đã hoàn tất Phase 0 + Phase 1 của [`contract-template.md`](contract-template.md). Plan [`case-list-api.md`](case-list-api.md) nên hoàn tất song song để FE có dropdown chọn vụ việc.

### Phase 2A: Hạ tầng PDF
- [ ] Thêm `docx4j-export-fo` vào `pom.xml`.
- [ ] Tạo `infrastructure/pdf/fop.xconf` (embed font tiếng Việt). Test load font sớm.
- [ ] `DocxToPdfConverter` (`@Component`): wrap docx4j-export-fo, đầu vào `byte[] docxBytes` → đầu ra `byte[] pdfBytes`.
- [ ] Bổ sung `seedPermissions()` cho `contract.*` trong `DataInitializer`.
- **Checkpoint:** unit test `DocxToPdfConverter` với 1 file `.docx` mẫu chứa tiếng Việt → PDF mở được, đúng font.

### Phase 2B: Entity + Repo + Merge
- [ ] Enum `ContractStatus` (kèm `getDescription()` + nhánh TW trong `MessageUtils`).
- [ ] Domain entities: `Contract`, `ContractData`, `ContractSnapshot` (POJO).
- [ ] JPA entities: 3 file `*JpaEntity` với `@CreationTimestamp`/`@UpdateTimestamp`.
- [ ] 3 cặp domain repo + SpringData + `Jpa...RepositoryImpl` (mapping tay).
- [ ] `DocxMergeService`: `merge(byte[] templateDocx, Map<String,String> data) → byte[] mergedDocx`. Dùng `VariablePrepare` + regex replace ở mức run.
- [ ] `FieldDataValidator`: validate theo bảng PHẦN 4, throw `IllegalArgumentException` (sẽ được map sang `400` qua `GlobalExceptionHandler`).
- [ ] `ContractNumberGenerator`: format + sanitize + check unique (loop + suffix `_{n}` nếu trùng).
- **Checkpoint:** `./mvnw clean compile` xanh; unit test `DocxMergeService` (case `{{key}}` cắt run, key trùng nhiều lần, escape XML).

### Phase 2C: UseCase + Controller
- [ ] `ContractUseCase`: `preview`, `create`, `list`, `getById`, `loadDocxBytes`, `loadPdfBytes`. Gọi `permissionChecker.requirePermission` đầu mỗi method. Scope vụ việc tái dùng `hasAccess` từ `CaseUseCase`.
- [ ] DTOs: `PreviewContractRequest`, `CreateContractRequest`, `ContractResponse`, `ContractListItemResponse` (`@Data @Builder`, validation).
- [ ] `ContractController` (`/api/v1/contracts`) — 7 endpoint Phần 3 (download trả `ResponseEntity<byte[]>` với header phù hợp).
- [ ] Doc API: `be/docs/api_docs/contract-api.md` (style theo `case-api.md`).
- **Checkpoint:** Postman end-to-end:
  1. `GET /contract-templates/{id}` lấy schema.
  2. `POST /contracts/preview` xem HTML.
  3. `POST /contracts` (kèm `legalCaseId`) → trả `id` + 2 link download.
  4. Tải `.docx` → mở Word kiểm tra placeholder đã thay đúng + tiếng Việt OK.
  5. Tải `.pdf` → mở Adobe Reader / browser kiểm tra font Việt + layout khớp Word.
  6. `GET /contracts?legalCaseId=42` → thấy hợp đồng vừa tạo.

### Phase 2D: Hoàn thiện
- [ ] Bổ sung enum mới vào `docs/code_specs/enum-mapping-docs.md` (`ContractStatus`).
- [ ] Unit test `FieldDataValidator`, `ContractNumberGenerator` (sanitize tên người tạo có dấu).
- **Checkpoint:** `./mvnw test` xanh.

---

## PHẦN 9: RỦI RO & GIẢM THIỂU

| Rủi ro | Tác động | Giảm thiểu |
|---|---|---|
| FOP thiếu font tiếng Việt | PDF lỗi hiển thị (□ □ □) | Embed font Noto Sans Vietnamese trong `fop.xconf`, test sớm ở Phase 2A. |
| `{{key}}` cắt run sau khi user chỉnh Word | Replace sót | Bắt buộc gọi `VariablePrepare.prepare()` trước replace. |
| Tên người tạo có dấu / khoảng trắng → `contractNo` xấu | URL / filename lỗi | `ContractNumberGenerator` chuẩn hóa: bỏ dấu (Normalizer NFD), space → `_`, giữ ASCII alphanumeric + `_`. |
| Trùng `contractNo` (cùng creator + ngày + template) | DB unique constraint vi phạm | Loop check `contractNo` + suffix `_{n}` đến khi không trùng. |
| Inject XSS qua data input → render preview HTML | Stored XSS | Escape XML khi merge `.docx`; FE phải escape khi render HTML preview. |
| File bị xoá khỏi storage | 500 khi download | Try-catch `IOException` ở download endpoint, trả `404` rõ ràng. |
| Hợp đồng đã tạo, mẫu sửa version mới | Inconsistency | Lưu `templateVersion` snapshot — load lại theo `(templateId, version)` cũ. |

---

## PHẦN 10: KIỂM THỬ (Verification)

1. `cd be && ./mvnw clean compile`.
2. `./mvnw spring-boot:run` (dev, MySQL).
3. **Happy path:**
   - Tạo mẫu qua UC1 (file `.docx` có `{{party_a_name}}`, `{{contract_value}}`, `{{contract_date}}`).
   - `POST /contracts/preview` → check `renderedHtml`.
   - `POST /contracts` (kèm `legalCaseId`) → trả `contractNo`.
   - `GET /contracts/{id}` → đầy đủ data, snapshot html, link download.
   - `GET /contracts/{id}/download/docx` → mở Word, các `{{...}}` đã thay đúng.
   - `GET /contracts/{id}/download/pdf` → PDF tiếng Việt OK, layout khớp Word.
4. **Negative:**
   - Thiếu field `required` → `400` field-level.
   - `legalCaseId` không tồn tại / không có quyền → `403`.
   - Mẫu đã ARCHIVED → `400`.
   - User không có permission `contract.create` → `403`.
5. **Versioning:** sau khi tạo hợp đồng từ template v1, sửa template tạo v2 → vẫn xem/tải hợp đồng cũ đúng v1.
6. `./mvnw test` — unit test `DocxMergeService`, `FieldDataValidator`, `ContractNumberGenerator`, `DocxToPdfConverter`.

---

## KẾT LUẬN

Plan UC2 phụ thuộc UC1 ([`contract-template.md`](contract-template.md)) và song song với UC3 ([`case-list-api.md`](case-list-api.md)). Pipeline tài liệu master = `.docx`, xuất Word + PDF qua FOP để đảm bảo nhất quán. Phân quyền hoàn toàn động qua `PermissionChecker`, không hardcode role.

*Tài liệu này là bản chốt cuối cùng cho UC2.*
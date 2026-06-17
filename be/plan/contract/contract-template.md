# KẾ HOẠCH TỔNG THỂ: TẠO MẪU HỢP ĐỒNG (CONTRACT TEMPLATE)

Tài liệu chốt cuối cùng cho **UC1 — Tạo mẫu hợp đồng**: upload file Word có placeholder `{{ten_bien}}`, hệ thống dò biến, sinh schema field, cho user review/đổi nhãn/đặt kiểu, rồi lưu mẫu kèm versioning.

> **Tài liệu tham chiếu (đọc trước khi code):**
> - Quy ước đặt tên biến `{{...}}`: xem [`be/docs/contract/quy-uoc-dat-ten-bien.md`](../docs/contract/quy-uoc-dat-ten-bien.md).
> - Quy ước Permission Key & RBAC động: xem [`be/docs/contract/permission-keys.md`](../docs/contract/permission-keys.md).

---

## PHẦN 1: TỔNG QUAN (SPECIFICATION)

### 1.1. Mục tiêu (Objective)

Cho phép Luật sư / Admin upload mẫu hợp đồng dưới dạng `.docx` có chèn `{{placeholder}}`. Hệ thống:
1. Dò danh sách biến distinct, validate cú pháp theo Quy ước đặt tên biến.
2. Convert `.docx` → HTML để FE preview.
3. Sinh field schema nháp; user chỉnh nhãn / kiểu / required / thứ tự.
4. Lưu mẫu kèm master `.docx`, hỗ trợ versioning.

### 1.2. Công nghệ (Tech Stack)

- **Backend:** Spring Boot 4.1.0, Java 17 (kiến trúc Hexagonal — domain POJO + JpaEntity + Port + Adapter + UseCase + Controller).
- **Database:** MySQL (Spring Data JPA, `ddl-auto: update` ở dev).
- **Tài liệu Word:** `org.docx4j:docx4j-JAXB-ReferenceImpl` (đọc/parse `.docx`, export HTML).
- **Upload:** `spring.servlet.multipart` (max-file-size 10MB).
- **i18n:** dùng `MessageUtils` (đã có) — bổ sung khóa cho enum mới.

### 1.3. Cấu trúc mã nguồn (Project Structure)

```
domain/entity/
  ContractTemplate.java              # POJO domain
  TemplateField.java
  TemplateStatus.java                # enum DRAFT / ACTIVE / ARCHIVED
  FieldType.java                     # enum TEXT / NUMBER / DATE / MONEY / PARAGRAPH

domain/repository/
  ContractTemplateRepository.java    # port
  TemplateFieldRepository.java
  ContractFileStorage.java           # port lưu file

application/dto/
  AnalyzeTemplateResponse.java       # { previewHtml, fields[], warnings[] }
  CreateTemplateRequest.java         # multipart: file + name + code? + fields[]
  TemplateFieldDto.java
  ContractTemplateResponse.java

application/service/
  PermissionChecker.java             # check rule động (xem permission-keys.md)
  DocxPlaceholderExtractor.java      # docx4j VariablePrepare + regex {{...}}
  DocxToHtmlConverter.java           # docx4j HTML export

application/usecase/
  ContractTemplateUseCase.java       # analyze / save / list / getById / update / archive

infrastructure/persistence/entity/
  ContractTemplateJpaEntity.java
  TemplateFieldJpaEntity.java

infrastructure/persistence/repository/
  SpringDataContractTemplateRepository.java
  JpaContractTemplateRepositoryImpl.java
  SpringDataTemplateFieldRepository.java
  JpaTemplateFieldRepositoryImpl.java

infrastructure/storage/
  LocalContractFileStorage.java      # adapter cho ContractFileStorage

presentation/controller/
  ContractTemplateController.java    # /api/v1/contract-templates
```

---

## PHẦN 2: THIẾT KẾ DATABASE (ENTITY)

### 2.1. `contract_templates`

| Field | Kiểu | Ghi chú |
|---|---|---|
| `id` | `Long` (auto) | PK |
| `code` | `String` | unique key của mẫu (vd `HD_DICH_VU_PL`). Không unique với `version` — cùng `code` khác `version` được phép. |
| `name` | `String` | Tên hiển thị. |
| `version` | `int` | Bắt đầu từ 1. Sửa mẫu → version+1. |
| `status` | enum `TemplateStatus` | `DRAFT` / `ACTIVE` / `ARCHIVED`. Mặc định `ACTIVE` khi save. |
| `originalFileName` | `String` | Tên file gốc user upload. |
| `storagePath` | `String` | Đường dẫn master `.docx` (vd `templates/HD_DICH_VU_PL_v1.docx`). |
| `htmlContent` | `TEXT` | HTML preview (docx4j export). |
| `createdBy` | `ManyToOne → users` | Người tạo. |
| `createdAt`, `updatedAt` | `OffsetDateTime` | `@CreationTimestamp` / `@UpdateTimestamp`. |

**Unique constraint:** `(code, version)` để tránh trùng version.

### 2.2. `template_fields`

| Field | Kiểu | Ghi chú |
|---|---|---|
| `id` | `Long` | PK |
| `template_id` | `ManyToOne → contract_templates` | FK |
| `fieldKey` | `String` | đúng tên `{{key}}` trong file Word. snake_case. |
| `label` | `String` | Nhãn hiển thị (user chỉnh được). |
| `fieldType` | enum `FieldType` | `TEXT` / `NUMBER` / `DATE` / `MONEY` / `PARAGRAPH`. |
| `required` | `boolean` | Mặc định `true`. |
| `displayOrder` | `int` | Thứ tự render trên form. |
| `defaultValue` | `String` (nullable) | Giá trị mặc định gợi ý. |

**Unique:** `(template_id, fieldKey)`.

### 2.3. Versioning

- Sửa mẫu đang `ACTIVE` → tạo bản ghi mới cùng `code`, `version+1`, status `ACTIVE`.
- Bản cũ chuyển `ARCHIVED` (giữ lại để hợp đồng cũ tham chiếu).
- Lệnh xóa = `PATCH /{id}/archive` (chuyển `ARCHIVED`), KHÔNG xóa cứng.

---

## PHẦN 3: CÁC API CHI TIẾT SẼ PHÁT TRIỂN

> Envelope chuẩn: `{ "status": <int>, "message": <string>, "data": <object|array> }`. Lỗi 403 dùng `ForbiddenException` (sẽ thêm ở Phase 0) bắt qua `GlobalExceptionHandler` — KHÔNG dùng hack `403_FORBIDDEN` string-match như `CaseController` cũ.

### API 1: Lấy danh sách kiểu trường (Field Types)
- **Endpoint:** `GET /api/v1/contract-templates/field-types`
- **Mục đích:** FE đổ dropdown chọn `fieldType` lúc user chỉnh field schema.
- **Response data:** `[ { "code": "TEXT", "description": "Văn bản" }, { "code": "DATE", "description": "Ngày" }, ... ]` (i18n VI/TW qua `MessageUtils`).

### API 2: Phân tích file Word (Analyze)
- **Endpoint:** `POST /api/v1/contract-templates/analyze` (multipart)
- **Permission:** `contract_template.create`.
- **Request:** form-data `file=<docx>`.
- **Luồng xử lý:**
  1. Validate đuôi `.docx` + MIME `application/vnd.openxmlformats-officedocument.wordprocessingml.document`.
  2. `docx4j` load `WordprocessingMLPackage` → chạy `VariablePrepare.prepare()` (gộp run bị Word cắt vụn).
  3. Quét body + header + footer + bảng. Regex: `\{\{\s*([a-zA-Z0-9_]+)\s*\}\}`. Distinct, **giữ thứ tự xuất hiện**.
  4. Validate cú pháp theo [`quy-uoc-dat-ten-bien.md`](../docs/contract/quy-uoc-dat-ten-bien.md). Biến sai → cho vào `warnings[]`, không block.
  5. Convert `.docx` → HTML qua docx4j HTML export.
  6. Sinh field nháp: `fieldKey` = tên biến, `label` = humanize (`party_a_name` → `Party A Name`), `fieldType` = `TEXT`, `required` = `true`, `displayOrder` = thứ tự xuất hiện.
- **Response data:**
  ```json
  {
    "previewHtml": "<html>...</html>",
    "fields": [
      { "fieldKey": "party_a_name", "label": "Party A Name", "fieldType": "TEXT", "required": true, "displayOrder": 1 }
    ],
    "warnings": []
  }
  ```
- **Lỗi:** `400` file không đúng định dạng / hỏng / không có biến (vẫn cho lưu nhưng cảnh báo).

### API 3: Lưu mẫu (Save)
- **Endpoint:** `POST /api/v1/contract-templates` (multipart)
- **Permission:** `contract_template.create`.
- **Request (multipart):**
  - `file=<docx>` — file gốc đã analyze.
  - `metadata=<json>`:
    ```json
    {
      "code": "HD_DICH_VU_PL",
      "name": "Hợp đồng dịch vụ pháp lý",
      "fields": [
        { "fieldKey": "party_a_name", "label": "Tên bên A", "fieldType": "TEXT", "required": true, "displayOrder": 1 }
      ]
    }
    ```
- **Luồng xử lý:**
  1. Check permission `contract_template.create`.
  2. Nếu `code` chưa có → version = 1. Nếu `code` đã có ACTIVE → version mới = max(version)+1, mẫu cũ → ARCHIVED.
  3. Sanitize tên file (chống path traversal). Lưu master `.docx` qua `ContractFileStorage` ở `templates/{code}_v{version}.docx`.
  4. Persist `ContractTemplate` (status `ACTIVE`) + các `TemplateField`.
- **Response:** `201` + `ContractTemplateResponse` đầy đủ.
- **Lỗi:** `403` thiếu permission · `400` thiếu field bắt buộc / fieldKey trùng / fieldKey không có trong file Word.

### API 4: Danh sách mẫu
- **Endpoint:** `GET /api/v1/contract-templates?status=ACTIVE`
- **Permission:** `contract_template.list`.
- **Response data:** mảng `ContractTemplateResponse` (rút gọn — không kèm `htmlContent`).

### API 5: Chi tiết mẫu
- **Endpoint:** `GET /api/v1/contract-templates/{id}`
- **Permission:** `contract_template.view`.
- **Response data:** `ContractTemplateResponse` đầy đủ (gồm field schema + `htmlContent` để UC2 render preview).

### API 6: Sửa mẫu (tạo version mới)
- **Endpoint:** `PUT /api/v1/contract-templates/{id}` (multipart, optional `file`)
- **Permission:** `contract_template.update`.
- **Luồng:**
  - Nếu có `file` mới → tạo version mới (mẫu cũ → ARCHIVED).
  - Nếu chỉ chỉnh metadata field schema (label/required/order/type) → cập nhật trực tiếp **chỉ khi mẫu chưa có hợp đồng nào** dùng; ngược lại bắt buộc tạo version mới.
- **Response:** `ContractTemplateResponse` của bản ghi cuối cùng.

### API 7: Archive mẫu
- **Endpoint:** `PATCH /api/v1/contract-templates/{id}/archive`
- **Permission:** `contract_template.archive`.
- **Luồng:** đổi `status` sang `ARCHIVED`. Hợp đồng đã tạo từ mẫu này vẫn xem/tải được (vì giữ `templateVersion` snapshot).

---

## PHẦN 4: PHÂN QUYỀN (RBAC ĐỘNG)

Tuân thủ [`permission-keys.md`](../docs/contract/permission-keys.md). **KHÔNG hardcode role** trong UseCase.

Permission keys của module này:
- `contract_template.create` · `contract_template.view` · `contract_template.list` · `contract_template.update` · `contract_template.archive`

Cấp mặc định cho role: xem mục 4 trong `permission-keys.md`.

`PermissionChecker` (mới) ở `application/service/PermissionChecker.java`:
```java
boolean hasPermission(User user, String permissionCode);
void requirePermission(User user, String permissionCode); // throw ForbiddenException
```
Đọc qua `RuleRepository` (đã có) — join `rules` × `permissions`, lấy danh sách `code` của user (chỉ rule `status = ACTIVE`).

---

## PHẦN 5: THƯ VIỆN & CẤU HÌNH (be/pom.xml + application yaml)

Thêm vào `be/pom.xml`:
```xml
<dependency>
  <groupId>org.docx4j</groupId>
  <artifactId>docx4j-JAXB-ReferenceImpl</artifactId>
  <version>11.4.11</version>
</dependency>
```

Thêm vào `application-dev.yaml` và `application-prod.yaml`:
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 12MB
app:
  contract:
    storage-path: ./storage/contracts   # dev: local disk
```

Thư mục lưu file: `./storage/contracts/templates/`.

---

## PHẦN 6: TASK BREAKDOWN

### Phase 0: Hạ tầng dùng chung (UC1 + UC2 + UC3 đều cần)
- [ ] Thêm dependency `docx4j-JAXB-ReferenceImpl` vào `pom.xml`.
- [ ] Cấu hình `multipart` + `app.contract.storage-path` trong `application-dev.yaml` và `application-prod.yaml`.
- [ ] Tạo `domain/exception/ForbiddenException.java`. Bổ sung handler trong `presentation/exception/GlobalExceptionHandler.java`. *Giữ controller cũ nguyên — chỉ thêm mới.*
- [ ] `domain/repository/ContractFileStorage.java` (port) + `infrastructure/storage/LocalContractFileStorage.java` (adapter): `store(byte[], String) → path`, `load(String) → byte[]`. Sanitize path (chỉ ghi trong `app.contract.storage-path`).
- [ ] `application/service/PermissionChecker.java` + bổ sung `seedPermissions()` trong `DataInitializer` cho các permission của UC1 (xem `permission-keys.md`).
- **Checkpoint:** `./mvnw clean compile` xanh; chạy được ứng dụng; `DataInitializer` seed thêm permission mới mà không lỗi.

### Phase 1: UC1 — Tạo mẫu hợp đồng
- [ ] Enums `TemplateStatus`, `FieldType` (kèm `getDescription()` VI). Bổ sung TW trong `MessageUtils`.
- [ ] `domain/entity/ContractTemplate.java`, `TemplateField.java` (POJO `@Data @Builder`).
- [ ] `infrastructure/persistence/entity/ContractTemplateJpaEntity.java`, `TemplateFieldJpaEntity.java` (`@CreationTimestamp`/`@UpdateTimestamp` kiểu `OffsetDateTime` — match `LegalCaseJpaEntity`).
- [ ] Domain repos + SpringData repos + `Jpa...RepositoryImpl` adapter (mapping tay, theo pattern `JpaLegalCaseRepositoryImpl`).
- [ ] `DocxPlaceholderExtractor` (docx4j `VariablePrepare` + regex distinct giữ thứ tự + validate cú pháp theo `quy-uoc-dat-ten-bien.md`).
- [ ] `DocxToHtmlConverter` (docx4j HTML export).
- [ ] `ContractTemplateUseCase`: `analyze`, `save`, `list`, `getById`, `update`, `archive`. Mọi entry-point gọi `permissionChecker.requirePermission(...)`.
- [ ] DTOs: `AnalyzeTemplateResponse`, `CreateTemplateRequest`, `UpdateTemplateRequest`, `TemplateFieldDto`, `ContractTemplateResponse` (`@Data @Builder`, validation `@NotBlank`, `@Min`...).
- [ ] `ContractTemplateController` (`/api/v1/contract-templates`) — 7 endpoint ở Phần 3.
- [ ] Doc API: `be/docs/api_docs/contract-template-api.md` (style theo `case-api.md`).
- **Checkpoint:** Postman:
  1. Upload `.docx` mẫu (chứa `{{party_a_name}}`, `{{contract_value}}`) vào `/analyze` → xem `data.fields` đúng + `previewHtml` hiển thị được.
  2. `POST` lưu → trả `id`. `GET /{id}` xác minh field schema lưu đúng.
  3. Upload lần 2 cùng `code` → tạo version mới, mẫu cũ chuyển ARCHIVED.
  4. User không có permission → `403`.

---

## PHẦN 7: RỦI RO & GIẢM THIỂU

| Rủi ro | Tác động | Giảm thiểu |
|---|---|---|
| Word cắt `{{key}}` thành nhiều run | Dò sót biến | Bắt buộc gọi `VariablePrepare.prepare()` trước khi quét. |
| File hỏng / không phải `.docx` | Crash app | Validate MIME + đuôi + try-catch khi `WordprocessingMLPackage.load()`. |
| Path traversal khi lưu file | Lỗ hổng bảo mật | Sanitize tên file ở `LocalContractFileStorage`; chỉ ghi trong `app.contract.storage-path`. |
| `ddl-auto: validate` ở prod | Thiếu bảng mới khi deploy | Chuẩn bị DDL thủ công cho prod (cân nhắc thêm Flyway sau). |
| Hardcode role làm phân quyền cứng | Khó thay đổi sau | Dùng `PermissionChecker` ngay từ đầu (xem `permission-keys.md`). |

---

## PHẦN 8: KIỂM THỬ (Verification)

1. `cd be && ./mvnw clean compile` — build xanh.
2. `./mvnw spring-boot:run` (profile dev, MySQL `dms_db`).
3. **Postman happy path:**
   - `POST /api/v1/contract-templates/analyze` (multipart `.docx` có `{{party_a_name}}`).
   - `POST /api/v1/contract-templates` (multipart + metadata).
   - `GET /api/v1/contract-templates?status=ACTIVE`.
   - `GET /api/v1/contract-templates/{id}`.
   - `PUT /api/v1/contract-templates/{id}` (file mới) → version+1.
   - `PATCH /api/v1/contract-templates/{id}/archive`.
4. **Negative:** upload `.txt` → `400`; user thiếu permission → `403`; `code` rỗng → `400`.
5. `./mvnw test` — unit test `DocxPlaceholderExtractor` (case run cắt vụn / nested `{{` / nhiều biến trùng).

---

## KẾT LUẬN

UC1 là nền cho UC2. Cần làm **xong Phase 0 trước**, sau đó Phase 1 mới chạy được. Sau khi Phase 1 done, tiếp tục theo plan [`contract-from-template.md`](contract-from-template.md) cho UC2 và [`case-list-api.md`](case-list-api.md) cho UC3 (chuẩn bị danh sách vụ việc cho UC2 attach).

*Tài liệu này là bản chốt cuối cùng cho UC1. Quy ước biến và permission đã tách ra docs riêng để tái sử dụng.*
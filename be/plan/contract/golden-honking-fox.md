# KẾ HOẠCH: TẠO MẪU HỢP ĐỒNG & TẠO HỢP ĐỒNG TỪ MẪU (Contract Template & Contract Generation)

## Bối cảnh (Context)

Hệ thống DMS hiện đã có module Quản lý vụ việc (`legal_cases`) chạy trên kiến trúc Hexagonal (Spring Boot 4.1.0 / Java 17 / MySQL). Nghiệp vụ pháp lý cần soạn hợp đồng lặp đi lặp lại với cùng một bố cục, chỉ khác phần thông tin các bên. Mục tiêu là cho phép:

1. **Tạo mẫu hợp đồng (UC1):** Upload 1 file Word (`.docx`), trong đó chỗ cần điền được đánh dấu bằng `{{ten_bien}}`. Hệ thống dò biến, sinh schema field, cho người dùng review (đổi nhãn, kiểu, bắt buộc/tùy chọn), rồi lưu mẫu kèm versioning.
2. **Tạo hợp đồng từ mẫu (UC2):** Chọn mẫu → form nhập liệu sinh động từ schema → nhập data → preview → lưu. Khi lưu, hệ thống merge data vào mẫu và **xuất ra cả file Word (.docx) lẫn PDF**.

Đây là vùng greenfield hoàn toàn: chưa có entity hợp đồng, chưa có thư viện xử lý tài liệu, chưa có upload file. Plan này chỉ làm **Backend + API docs** (thư mục `fe/` đang trống, để FE ráp sau theo API docs).

### Quyết định đã chốt (từ câu hỏi làm rõ)
- **Phạm vi:** Chỉ Backend + API docs. Không code FE.
- **Thư viện tài liệu:** `docx4j` (xử lý `.docx`, export HTML, export Word) + xuất PDF (xem mục Thư viện — khuyến nghị `docx4j-export-fo` để Word/PDF khớp nhau, có cân nhắc `openhtmltopdf`).
- **Nhận diện vùng điền:** CHỈ cú pháp `{{placeholder}}`. Không tự dò `[...]`, `___`, `......`.
- **Đầu ra hợp đồng:** Xuất **cả Word (.docx) và PDF**. Ưu tiên làm UC1 trước, nhưng cài sẵn thư viện cho cả 2 định dạng.

---

## PHẦN 1: USE CASE CHI TIẾT

### UC1 — Tạo mẫu hợp đồng (Create Contract Template)

- **Actor:** `ADMIN`, `LAWYER` (role-gate giống `CaseUseCase.createCase`).
- **Tiền điều kiện:** Đã đăng nhập, có JWT hợp lệ.

**Luồng chính:**
1. User upload file `.docx` (multipart) kèm `name` mẫu (tùy chọn `code`).
2. Hệ thống validate file: đúng đuôi `.docx` + MIME `application/vnd.openxmlformats-officedocument.wordprocessingml.document`, dưới giới hạn dung lượng, mở được bằng docx4j (không hỏng).
3. docx4j load `WordprocessingMLPackage`; chạy `VariablePrepare.prepare()` để gộp các run bị Word cắt vụn (xử lý case `{{`, `key`, `}}` nằm ở 3 run khác nhau).
4. Quét toàn bộ text body + header/footer + bảng, dùng regex `\{\{\s*([a-zA-Z0-9_]+)\s*\}\}` để lấy danh sách biến **distinct, giữ thứ tự xuất hiện**.
5. Validate biến theo Quy ước đặt tên (mục Phần 2): biến sai cú pháp (có dấu cách, dấu tiếng Việt, nested `{{`) → trả cảnh báo/lỗi field-level.
6. Convert `.docx` → HTML (docx4j HTML export) để preview trên FE.
7. Tự sinh schema field nháp: mỗi biến → `{ fieldKey, label (humanize từ key), fieldType: TEXT, required: true, displayOrder }`.
8. Trả về FE: `previewHtml` + danh sách field nháp + danh sách cảnh báo (nếu có). **Chưa lưu DB.**
9. User chỉnh: đổi `label`, chọn `fieldType` (TEXT/NUMBER/DATE/MONEY/PARAGRAPH), set `required`, sắp xếp `displayOrder` → submit (kèm `templateId` tạm hoặc lại gửi file token).
10. Hệ thống lưu: ghi master `.docx` vào storage, persist `ContractTemplate` (status `ACTIVE`) + các `TemplateField`. Sinh `code` nếu chưa có.
11. Trả về mẫu đã lưu.

**Luồng phụ / lỗi:**
- File không phải `.docx` / hỏng → `400`.
- Không tìm thấy `{{...}}` nào → trả cảnh báo "mẫu không có biến", vẫn cho lưu (mẫu tĩnh).
- `{{` không cân `}}` → `400` field-level.
- `code` trùng mẫu đang ACTIVE → **tạo version mới** (version+1), không ghi đè.
- Không đủ quyền → `403`.

### UC2 — Tạo hợp đồng từ mẫu (Create Contract From Template)

- **Actor:** `ADMIN`, `LAWYER`.

**Luồng chính:**
1. User lấy danh sách mẫu ACTIVE (`GET /contract-templates?status=ACTIVE`).
2. Chọn 1 mẫu → `GET /contract-templates/{id}` trả về field schema → FE render form động (Cách dynamic hoàn toàn).
3. User nhập data; tùy chọn liên kết `legalCaseId` (gắn vào vụ việc).
4. **Preview:** `POST /contracts/preview` → merge data vào HTML mẫu → trả `renderedHtml` (không lưu).
5. User lưu (`POST /contracts`). Hệ thống validate: đủ field `required`, đúng kiểu (DATE parse được, NUMBER/MONEY là số...).
6. Merge data vào master `.docx` (docx4j variable replace) → **final `.docx` (Word output)**.
7. Convert final → **PDF**.
8. Persist `Contract` + `ContractData (dataJson)` + `ContractSnapshot (renderedHtml, docxPath, pdfPath)`; sinh `contractNo` theo format.
9. Trả về contract kèm link tải `.docx` và `.pdf`.

**Luồng phụ / lỗi:**
- Thiếu field bắt buộc → `400` kèm danh sách field lỗi.
- Sai kiểu (vd DATE không parse được) → `400` field-level.
- Mẫu đã ARCHIVED → chặn `400`.
- Không đủ quyền → `403`.

---

## PHẦN 2: DOCS QUY ƯỚC ĐẶT TÊN BIẾN (Deliverable)

Tạo file `be/docs/quy-uoc-dat-ten-bien-hop-dong.md` theo style doc quy ước hiện có (`docs/quy-uoc-vu-viec.md`): heading số, bảng `| Label (vi) | Code |`, callout `> **Lưu ý:**`. Nội dung:

1. **Cú pháp:** `{{ten_bien}}` — chỉ chấp nhận trong cặp `{{ }}`. Bên trong: `snake_case`, chữ thường ASCII, số, gạch dưới. KHÔNG dấu cách, KHÔNG dấu tiếng Việt, KHÔNG nested.
2. **Tiền tố nhóm (prefix):**
   | Nhóm | Prefix | Ví dụ |
   |---|---|---|
   | Bên A | `party_a_` | `party_a_name`, `party_a_tax_code` |
   | Bên B | `party_b_` | `party_b_name`, `party_b_address` |
   | Thông tin hợp đồng | `contract_` | `contract_no`, `contract_date`, `contract_value` |
   | Thanh toán | `payment_` | `payment_term`, `payment_method` |
   | Vụ việc liên kết | `case_` | `case_title`, `case_no` |
3. **Hậu tố gợi ý kiểu (tùy chọn):** `_date` (ngày), `_value`/`_amount` (tiền), `_no` (số/mã), `_percent` (%).
4. **Bảng biến chuẩn dùng chung** (party_a_name, party_a_tax_code, party_a_address, party_a_representative, party_b_name, contract_no, contract_date, contract_value, payment_term...).
5. **Quy tắc bắt buộc:** unique trong 1 mẫu; cân `{{`/`}}`; không khoảng trắng trong tên; mapping `fieldKey` ↔ `{{key}}` là 1-1.
6. **Ví dụ đúng/sai** + callout lưu ý.

---

## PHẦN 3: THIẾT KẾ DATABASE (ENTITY)

Theo đúng pattern hiện có: mỗi entity có cặp `domain/entity/X.java` (POJO thuần) + `infrastructure/persistence/entity/XJpaEntity.java` (`@Data @Builder`, `@CreationTimestamp`/`@UpdateTimestamp` kiểu `OffsetDateTime`). Hibernate `ddl-auto: update` tự tạo bảng ở dev.

1. **`contract_templates`** — `ContractTemplate`
   - `id`, `code` (unique), `name`, `version` (int), `status` (enum `TemplateStatus`: `DRAFT`/`ACTIVE`/`ARCHIVED`)
   - `originalFileName`, `storagePath` (đường dẫn master `.docx`), `htmlContent` (TEXT — preview)
   - `createdBy` (ManyToOne→User), `createdAt`, `updatedAt`
2. **`template_fields`** — `TemplateField`
   - `id`, `template_id` (FK), `fieldKey` (= `{{key}}`), `label`, `fieldType` (enum `FieldType`: `TEXT`/`NUMBER`/`DATE`/`MONEY`/`PARAGRAPH`)
   - `required` (bool), `displayOrder` (int), `defaultValue` (nullable)
3. **`contracts`** — `Contract`
   - `id`, `contractNo` (unique), `template_id` (FK), `templateVersion` (int — snapshot version lúc tạo)
   - `legalCase_id` (FK→legal_cases, nullable), `status` (enum `ContractStatus`: `DRAFT`/`FINALIZED`)
   - `createdBy` (FK→User), `createdAt`, `updatedAt`
4. **`contract_data`** — `ContractData`
   - `id`, `contract_id` (FK, unique), `dataJson` (TEXT — lưu toàn bộ data dạng JSON cho dynamic hoàn toàn)
5. **`contract_snapshots`** — `ContractSnapshot`
   - `id`, `contract_id` (FK), `renderedHtml` (TEXT), `docxPath`, `pdfPath`, `createdAt`

**Versioning:** Sửa mẫu ACTIVE → tạo bản ghi mới cùng `code`, `version+1`, mẫu cũ chuyển `ARCHIVED`. Contract giữ `templateVersion` đã dùng để không bị ảnh hưởng khi mẫu đổi.

---

## PHẦN 4: THƯ VIỆN & CẤU HÌNH MỚI (pom.xml)

Thêm vào `be/pom.xml`:
- `org.docx4j:docx4j-JAXB-ReferenceImpl` (core, đọc/ghi/replace `.docx`, export HTML).
- `org.docx4j:docx4j-export-fo` (**khuyến nghị** cho PDF: `.docx` → PDF qua Apache FOP, đảm bảo Word và PDF khớp vì cùng 1 nguồn `.docx`).
- *(Cân nhắc thay thế)* `io.github.openhtmltopdf:openhtmltopdf-pdfbox` nếu muốn PDF render từ HTML — xem Open Questions.

Cấu hình `application-dev.yaml` / `application-prod.yaml`:
```yaml
spring:
  servlet:
    multipart:
      max-file-size: 10MB
      max-request-size: 12MB
app:
  contract:
    storage-path: ./storage/contracts   # thư mục lưu .docx/.pdf (dev: local disk)
```

**Pipeline tài liệu (master = .docx):**
- UC1: upload `.docx` → docx4j parse + dò biến → docx4j export HTML (preview) → lưu master `.docx`.
- UC2 merge: load master `.docx` → docx4j replace `{{key}}` → **final `.docx`** → docx4j-export-fo → **PDF**. HTML preview = docx4j export của final/template.
- Lưu ý docx4j native dùng `${key}`; ta tiền xử lý `{{key}}` → `${key}` trước khi gọi `variableReplace`, hoặc replace bằng regex trực tiếp trên run đã `VariablePrepare`.

---

## PHẦN 5: LƯU TRỮ FILE

- Dev: local disk theo `app.contract.storage-path`. Tạo `ContractFileStorage` (interface domain port) + `LocalContractFileStorage` (`@Component`) — `store(bytes, fileName) → path`, `load(path) → bytes`.
- Đặt tên file: `templates/{code}_v{version}.docx`, `contracts/{contractNo}.docx`, `contracts/{contractNo}.pdf`.
- **Bảo mật:** validate/sanitize tên file (chống path traversal), chỉ ghi trong thư mục cấu hình; giới hạn dung lượng ở multipart config; giới hạn loại file ở use case.

---

## PHẦN 6: API CHI TIẾT (envelope `{status, message, data}`, i18n qua `MessageUtils`)

**Contract Templates** (`/api/v1/contract-templates`):
- `GET /field-types` — list enum `FieldType` (code + label vi/TW) cho dropdown.
- `POST /analyze` — multipart upload `.docx` → trả `previewHtml` + field nháp + cảnh báo (chưa lưu).
- `POST` — lưu mẫu (file + name + field schema đã chỉnh). Role `ADMIN`/`LAWYER`.
- `GET` — list mẫu (lọc `?status=ACTIVE`).
- `GET /{id}` — chi tiết mẫu + field schema (cho UC2 render form).
- `PUT /{id}` — sửa metadata field schema (tạo version mới nếu cần).
- `DELETE /{id}` hoặc `PATCH /{id}/archive` — chuyển ARCHIVED.

**Contracts** (`/api/v1/contracts`):
- `POST /preview` — body `{templateId, data}` → trả `renderedHtml` (không lưu).
- `POST` — tạo hợp đồng (validate → merge → xuất .docx + .pdf → lưu). Trả contract + link tải.
- `GET` — list (role-scope giống case: Admin xem hết; user xem của mình).
- `GET /{id}` — chi tiết + data + link tải.
- `GET /{id}/download/docx` và `GET /{id}/download/pdf` — tải file.

---

## PHẦN 7: TASK BREAKDOWN (theo phase)

### Phase 0: Hạ tầng
- [ ] Thêm dependency docx4j + export-fo vào `pom.xml`; cấu hình multipart + storage path.
- [ ] `ContractFileStorage` port + `LocalContractFileStorage` adapter.
- [ ] Thêm `ForbiddenException extends RuntimeException` + xử lý trong `GlobalExceptionHandler` (thay hack `403_FORBIDDEN` string-match — dùng cho cả module mới). *Giữ controller cũ nguyên, chỉ thêm mới.*
- **Checkpoint:** `mvn clean compile` xanh.

### Phase 1: Tạo mẫu hợp đồng (UC1) — ưu tiên
- [ ] Enums `TemplateStatus`, `FieldType` (có `getDescription()` + nhánh TW trong `MessageUtils`).
- [ ] Entities: `ContractTemplate`/`TemplateField` (domain POJO + JpaEntity) + domain repo port + SpringData repo + `Jpa...RepositoryImpl` adapter.
- [ ] `DocxPlaceholderExtractor` (docx4j): `VariablePrepare` + regex dò biến distinct giữ thứ tự + validate cú pháp.
- [ ] `DocxToHtmlConverter` (docx4j HTML export).
- [ ] `ContractTemplateUseCase`: `analyze(file)`, `save(...)`, `list`, `getById`, `update`, `archive`. Role-gate + versioning.
- [ ] DTOs (`@Data @Builder`, Jakarta validation) + `ContractTemplateController`.
- [ ] Viết `docs/quy-uoc-dat-ten-bien-hop-dong.md`.
- **Checkpoint:** Upload `.docx` có `{{...}}` → trả đúng biến + preview HTML; lưu/lấy mẫu chạy được qua Postman.

### Phase 2: Tạo hợp đồng từ mẫu (UC2)
- [ ] Entities: `Contract`/`ContractData`/`ContractSnapshot` (đủ stack hexagonal) + FK `legal_cases`.
- [ ] Enum `ContractStatus` + i18n.
- [ ] `DocxMergeService` (docx4j replace `{{key}}`→data) → final `.docx`.
- [ ] `DocxToPdfConverter` (docx4j-export-fo).
- [ ] `ContractNumberGenerator` (format `HD_{template_code}_{ddMMyy}_{seq}` — chốt ở Open Questions).
- [ ] `ContractUseCase`: `preview`, `create` (validate required + type), `list` (role-scope), `getById`, download.
- [ ] DTOs + `ContractController` + download endpoints.
- **Checkpoint:** End-to-end: chọn mẫu → nhập data → preview → lưu → tải được cả `.docx` và `.pdf` đúng nội dung.

### Phase 3: API docs + hoàn thiện
- [ ] `docs/api_docs/contract-template-api.md` + `docs/api_docs/contract-api.md` (style như `case-api.md`).
- [ ] Cập nhật `docs/code_specs/enum-mapping-docs.md` cho enum mới.
- [ ] Unit test use case (JUnit5 + Mockito + AssertJ) cho extractor, merge, validate required/type.
- **Checkpoint:** `mvn clean test` xanh.

---

## PHẦN 8: RỦI RO & GIẢM THIỂU

| Rủi ro | Tác động | Giảm thiểu |
|---|---|---|
| Word cắt `{{key}}` thành nhiều run | Dò sót biến | `VariablePrepare.prepare()` trước khi quét |
| Word→HTML→? mất format | Preview/PDF lệch bản gốc | Merge & xuất trực tiếp trên `.docx`; PDF qua FOP từ chính `.docx` |
| docx4j-export-fo + font tiếng Việt | PDF lỗi font | Cấu hình font embed (FOP), test sớm với văn bản tiếng Việt |
| `ddl-auto: validate` ở prod | Thiếu bảng mới khi deploy | Chuẩn bị DDL thủ công cho prod (cân nhắc thêm Flyway) |
| Path traversal khi lưu file | Lỗ hổng bảo mật | Sanitize tên file, ghi trong thư mục cấu hình duy nhất |

---

## PHẦN 9: KIỂM THỬ (Verification)

1. `cd be && ./mvnw clean compile` sau mỗi phase — build xanh.
2. `./mvnw spring-boot:run` (profile dev, MySQL `dms_db`).
3. **UC1 Postman:** `POST /api/v1/contract-templates/analyze` (multipart .docx mẫu có `{{party_a_name}}`, `{{contract_value}}`) → kiểm tra `data.fields` + `data.previewHtml`. Sau đó `POST` lưu, `GET /{id}` xác minh schema.
4. **UC2 Postman:** `GET /{id}` lấy schema → `POST /contracts/preview` xem HTML → `POST /contracts` lưu → `GET /{id}/download/docx` và `/pdf` mở file kiểm tra nội dung đã điền đúng + tiếng Việt hiển thị OK.
5. Kiểm tra role-gate (`403` với role không hợp lệ) và validate required (`400` field-level).
6. `./mvnw test` — unit test extractor/merge/validate.

---

## OPEN QUESTIONS (cần chốt trước/khi code)

1. **PDF engine:** Khuyến nghị `docx4j-export-fo` (Word↔PDF khớp 100% vì cùng nguồn `.docx`). Bạn đã chọn `openhtmltopdf` ở câu hỏi trước — có đồng ý đổi sang FOP để Word/PDF nhất quán không? (Nếu giữ openhtmltopdf thì pipeline là `.docx`→HTML→PDF, có thể lệch nhẹ format.)
2. **Format `contractNo`:** Đề xuất `HD_{template_code}_{ddMMyy}_{seq}`. OK chứ?
3. **Quyền tạo mẫu/hợp đồng:** Dùng đúng `ADMIN`/`LAWYER` như tạo vụ việc, hay khác?
4. **Liên kết vụ việc:** `legalCaseId` ở UC2 là bắt buộc hay tùy chọn?

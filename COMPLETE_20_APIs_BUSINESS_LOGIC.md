# CHI TIẾT NGHIỆP VỤ 20 APIs - LIBRARY MANAGEMENT SYSTEM

---

## MODULE 1: CATEGORY (2 APIs)

### API 1: POST /api/v1/category - Tạo danh mục sách

**NGHIỆP VỤ THỰC TẾ:**
Thủ thư muốn tạo danh mục mới để phân loại sách (Công nghệ thông tin, Kinh tế, Văn học, ...)

**REQUEST:**
```
POST /api/v1/category
{
  "code": "IT",
  "name": "Công nghệ thông tin"
}
```

**VALIDATIONS:**
- `code`: Bắt buộc, max 20 ký tự, UNIQUE
- `name`: Bắt buộc, 3-100 ký tự

**BUSINESS LOGIC:**
1. Check code đã tồn tại chưa? → Nếu có → Error "Category code already exists"
2. Tạo Category mới
3. Save vào DB
4. Xử lý race condition (2 requests cùng lúc)

**RESPONSE SUCCESS (201):**
```json
{
  "status": 201,
  "message": "Category created successfully",
  "data": {
    "id": 1,
    "code": "IT",
    "name": "Công nghệ thông tin"
  }
}
```

**RESPONSE ERROR (400):**
```json
{
  "status": 400,
  "message": "Category code already exists"
}
```

**TÌNH HUỐNG:**
- ✅ Tạo danh mục mới thành công
- ❌ Code đã tồn tại → Báo lỗi
- ❌ Validation failed (code rỗng, name quá ngắn) → Báo lỗi

---

### API 2: GET /api/v1/category - Lấy tất cả danh mục

**NGHIỆP VỤ THỰC TẾ:**
Hiển thị danh sách tất cả danh mục sách trong thư viện

**REQUEST:**
```
GET /api/v1/category
```

**BUSINESS LOGIC:**
1. Query tất cả categories từ DB
2. Return danh sách (không cần pagination vì ít records)

**RESPONSE SUCCESS (200):**
```json
{
  "status": 200,
  "message": "Get categories successfully",
  "data": [
    {
      "id": 1,
      "code": "IT",
      "name": "Công nghệ thông tin"
    },
    {
      "id": 2,
      "code": "BIZ",
      "name": "Kinh tế"
    },
    {
      "id": 3,
      "code": "LIT",
      "name": "Văn học"
    }
  ]
}
```

**TÌNH HUỐNG:**
- ✅ Lấy danh sách thành công (có thể empty array nếu chưa có category)

---

## MODULE 2: STUDENT (5 APIs)

### API 3: POST /api/v1/students - Đăng ký sinh viên mới

**NGHIỆP VỤ THỰC TẾ:**
Sinh viên mới vào trường, đến thư viện đăng ký thẻ mượn sách

**REQUEST:**
```
POST /api/v1/students
{
  "fullName": "Nguyễn Văn A",
  "email": "a@student.edu.vn",
  "phone": "0901234567",
  "major": "INFORMATION_TECHNOLOGY",
  "yearOfStudy": "YEAR_1",
  "gender": "MALE"
}
```

**VALIDATIONS:**
- `fullName`: Bắt buộc, 2-100 ký tự
- `email`: Bắt buộc, format email hợp lệ, UNIQUE
- `phone`: Bắt buộc, format số VN (0xxxxxxxxx hoặc +84xxxxxxxxx), UNIQUE
- `major`: Bắt buộc, enum (INFORMATION_TECHNOLOGY, COMPUTER_SCIENCE, BUSINESS_ADMINISTRATION, ...)
- `yearOfStudy`: Bắt buộc, enum (YEAR_1, YEAR_2, YEAR_3, YEAR_4)
- `gender`: Bắt buộc, enum (MALE, FEMALE, OTHER)

**BUSINESS LOGIC:**
1. Check email đã tồn tại? → Nếu có → Error
2. Check phone đã tồn tại? → Nếu có → Error
3. **AUTO-GENERATE studentCode**: 
   - Đếm số students hiện tại
   - Tạo code theo format: `SV000001`, `SV000002`, `SV000003`, ...
4. Set defaults:
   - `status` = ACTIVE (sinh viên mới luôn ACTIVE)
   - `maxBorrowLimit` = 5 (giới hạn mượn tối đa 5 cuốn)
5. Save vào DB
6. Return thông tin đã tạo (bao gồm studentCode)

**RESPONSE SUCCESS (201):**
```json
{
  "status": 201,
  "message": "Student created successfully",
  "data": {
    "id": 1,
    "studentCode": "SV000001",
    "fullName": "Nguyễn Văn A",
    "email": "a@student.edu.vn",
    "phone": "0901234567",
    "major": "INFORMATION_TECHNOLOGY",
    "yearOfStudy": "YEAR_1",
    "status": "ACTIVE",
    "gender": "MALE",
    "maxBorrowLimit": 5,
    "currentBorrowingCount": 0,
    "createdAt": "2026-01-19T18:00:00",
    "updatedAt": "2026-01-19T18:00:00"
  }
}
```

**RESPONSE ERROR (400):**
```json
{
  "status": 400,
  "message": "Email already exists: a@student.edu.vn"
}
```

**TÌNH HUỐNG:**
- ✅ Đăng ký thành công → Nhận được mã SV (SV000001)
- ❌ Email đã được đăng ký → Báo lỗi
- ❌ Số điện thoại đã tồn tại → Báo lỗi
- ❌ Email không đúng format → Validation error
- ❌ Số điện thoại không đúng format VN → Validation error

---

### API 4: GET /api/v1/students - Tìm kiếm sinh viên

**NGHIỆP VỤ THỰC TẾ:**
Thủ thư cần tìm sinh viên để:
- Xem thông tin
- Kiểm tra số sách đang mượn
- Tìm theo tên/email/mã SV
- Lọc theo ngành, năm học, trạng thái

**REQUEST (Example 1 - Tìm tất cả SV ngành IT, đang ACTIVE):**
```
GET /api/v1/students?major=INFORMATION_TECHNOLOGY&status=ACTIVE&pageNo=0&pageSize=20
```

**REQUEST (Example 2 - Search "Nguyễn", sort theo tên tăng dần):**
```
GET /api/v1/students?search=Nguyễn&sortBy=fullName&sortDir=asc
```

**REQUEST (Example 3 - SV năm 3, trang 2, mỗi trang 10 items):**
```
GET /api/v1/students?yearOfStudy=YEAR_3&pageNo=2&pageSize=10
```

**QUERY PARAMETERS:**

| Param | Type | Required | Default | Mô tả |
|-------|------|----------|---------|-------|
| `search` | String | No | null | Tìm theo fullName/email/studentCode (tìm kiếm mờ, không phân biệt hoa thường) |
| `major` | Enum | No | null | Lọc theo ngành học (INFORMATION_TECHNOLOGY, COMPUTER_SCIENCE, ...) |
| `status` | Enum | No | null | Lọc theo trạng thái (ACTIVE, SUSPENDED) |
| `yearOfStudy` | Enum | No | null | Lọc theo năm học (YEAR_1, YEAR_2, YEAR_3, YEAR_4) |
| `pageNo` | int | No | 0 | Số trang (bắt đầu từ 0) |
| `pageSize` | int | No | 10 | Số items mỗi trang (max: 100) |
| `sortBy` | String | No | id | Field để sort (id, studentCode, fullName, createdAt) |
| `sortDir` | String | No | desc | Hướng sort (asc, desc) |

**BUSINESS LOGIC:**
1. Validate pageSize (nếu > 100 → set = 100)
2. Validate sortBy (chỉ cho phép: id, studentCode, fullName, createdAt)
3. Build dynamic query:
   - Nếu `search` không null → Tìm trong fullName OR email OR studentCode
   - Nếu `major` không null → AND major = ?
   - Nếu `status` không null → AND status = ?
   - Nếu `yearOfStudy` không null → AND yearOfStudy = ?
4. Apply pagination và sorting
5. Tính `currentBorrowingCount` cho mỗi student (số sách đang mượn)
6. Return kết quả dạng Page (có totalElements, totalPages, ...)

**RESPONSE SUCCESS (200):**
```json
{
  "status": 200,
  "message": "Get students successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "studentCode": "SV000001",
        "fullName": "Nguyễn Văn A",
        "email": "a@student.edu.vn",
        "phone": "0901234567",
        "major": "INFORMATION_TECHNOLOGY",
        "yearOfStudy": "YEAR_1",
        "status": "ACTIVE",
        "gender": "MALE",
        "maxBorrowLimit": 5,
        "currentBorrowingCount": 2,
        "createdAt": "2026-01-19T18:00:00"
      },
      {
        "id": 5,
        "studentCode": "SV000005",
        "fullName": "Nguyễn Thị B",
        "email": "b@student.edu.vn",
        "phone": "0901234568",
        "major": "INFORMATION_TECHNOLOGY",
        "yearOfStudy": "YEAR_2",
        "status": "ACTIVE",
        "gender": "FEMALE",
        "maxBorrowLimit": 5,
        "currentBorrowingCount": 0,
        "createdAt": "2026-01-19T19:00:00"
      }
    ],
    "pageNo": 0,
    "pageSize": 20,
    "totalElements": 156,
    "totalPages": 8,
    "last": false,
    "first": true
  }
}
```

**TÌNH HUỐNG:**
- ✅ Tìm theo tên "Nguyễn" → Trả về tất cả SV có tên chứa "Nguyễn"
- ✅ Lọc ngành IT + năm 1 → Chỉ trả về SV thỏa mãn
- ✅ Search "SV000001" → Tìm được SV có mã này
- ✅ Không có kết quả → Trả về empty array `content: []`
- ✅ pageNo = 10 nhưng chỉ có 3 trang → Trả về empty array

---

### API 5: GET /api/v1/students/{id} - Lấy chi tiết sinh viên

**NGHIỆP VỤ THỰC TẾ:**
Xem thông tin chi tiết 1 sinh viên cụ thể (khi click vào tên trong danh sách)

**REQUEST:**
```
GET /api/v1/students/1
```

**BUSINESS LOGIC:**
1. Tìm student theo ID
2. Nếu không tìm thấy → Error 400 "Student not found"
3. Tính `currentBorrowingCount` (số sách đang mượn)
4. Return đầy đủ thông tin

**RESPONSE SUCCESS (200):**
```json
{
  "status": 200,
  "message": "Get student successfully",
  "data": {
    "id": 1,
    "studentCode": "SV000001",
    "fullName": "Nguyễn Văn A",
    "email": "a@student.edu.vn",
    "phone": "0901234567",
    "major": "INFORMATION_TECHNOLOGY",
    "yearOfStudy": "YEAR_1",
    "status": "ACTIVE",
    "gender": "MALE",
    "maxBorrowLimit": 5,
    "currentBorrowingCount": 2,
    "createdAt": "2026-01-19T18:00:00",
    "updatedAt": "2026-01-19T20:30:00"
  }
}
```

**RESPONSE ERROR (400):**
```json
{
  "status": 400,
  "message": "Student not found with id: 999"
}
```

**TÌNH HUỐNG:**
- ✅ ID hợp lệ → Trả về thông tin đầy đủ
- ❌ ID không tồn tại → Error "Student not found"

---

### API 6: PUT /api/v1/students/{id} - Cập nhật thông tin sinh viên

**NGHIỆP VỤ THỰC TẾ:**
Sinh viên thay đổi thông tin (email mới, số điện thoại mới, chuyển ngành, lên năm, ...)

**REQUEST:**
```
PUT /api/v1/students/1
{
  "fullName": "Nguyễn Văn A Updated",
  "email": "new_email@student.edu.vn",
  "phone": "0909999999",
  "major": "COMPUTER_SCIENCE",
  "yearOfStudy": "YEAR_2"
}
```

**LƯU Ý:**
- Tất cả fields đều OPTIONAL (chỉ gửi field muốn update)
- **KHÔNG được phép update studentCode** (studentCode là immutable)

**BUSINESS LOGIC:**
1. Tìm student theo ID
2. Nếu không tìm thấy → Error
3. Nếu update email:
   - Check email mới có trùng với student khác không?
   - Nếu trùng → Error "Email already exists"
4. Nếu update phone:
   - Check phone mới có trùng với student khác không?
   - Nếu trùng → Error "Phone already exists"
5. Update các fields được gửi lên
6. Save và return thông tin mới

**RESPONSE SUCCESS (200):**
```json
{
  "status": 200,
  "message": "Student updated successfully",
  "data": {
    "id": 1,
    "studentCode": "SV000001",
    "fullName": "Nguyễn Văn A Updated",
    "email": "new_email@student.edu.vn",
    "phone": "0909999999",
    "major": "COMPUTER_SCIENCE",
    "yearOfStudy": "YEAR_2",
    "status": "ACTIVE",
    "gender": "MALE",
    "maxBorrowLimit": 5,
    "currentBorrowingCount": 2,
    "updatedAt": "2026-01-20T10:00:00"
  }
}
```

**RESPONSE ERROR (400):**
```json
{
  "status": 400,
  "message": "Email already exists: new_email@student.edu.vn"
}
```

**TÌNH HUỐNG:**
- ✅ Update fullName thành công
- ✅ Update email mới (chưa ai dùng) → Thành công
- ❌ Update email đã có người dùng → Error
- ✅ Chỉ update 1 field (vd: yearOfStudy) → Các field khác giữ nguyên
- ❌ Cố update studentCode → Field này bị ignore (không update)

---

### API 7: PATCH /api/v1/students/{id}/status - Đổi trạng thái sinh viên

**NGHIỆP VỤ THỰC TẾ:**
- Đình chỉ sinh viên vi phạm quy định (ACTIVE → SUSPENDED)
- Kích hoạt lại sinh viên sau khi hết thời gian đình chỉ (SUSPENDED → ACTIVE)

**REQUEST:**
```
PATCH /api/v1/students/1/status
{
  "status": "SUSPENDED",
  "reason": "Vi phạm quy định thư viện: Trả sách muộn quá 30 ngày"
}
```

**VALIDATIONS:**
- `status`: Bắt buộc, enum (ACTIVE, SUSPENDED)
- `reason`: Optional, max 500 ký tự (ghi lý do đổi trạng thái)

**BUSINESS LOGIC - QUAN TRỌNG:**
1. Tìm student theo ID
2. **KIỂM TRA NGHIỆP VỤ:**
   - Nếu đổi sang SUSPENDED:
     - Check xem sinh viên có đang mượn sách không?
     - Đếm số sách đang mượn (status = BORROWING)
     - **Nếu > 0 → KHÔNG CHO PHÉP đình chỉ!**
     - → Error: "Cannot suspend student who is currently borrowing X book(s). Please wait for return."
3. Nếu validation pass → Update status
4. Return thông tin mới

**RESPONSE SUCCESS (200):**
```json
{
  "status": 200,
  "message": "Student status updated successfully",
  "data": {
    "id": 1,
    "studentCode": "SV000001",
    "fullName": "Nguyễn Văn A",
    "status": "SUSPENDED",
    "currentBorrowingCount": 0,
    "updatedAt": "2026-01-20T11:00:00"
  }
}
```

**RESPONSE ERROR (400 - Đang mượn sách):**
```json
{
  "status": 400,
  "message": "Cannot suspend student who is currently borrowing 3 book(s). Please wait for return."
}
```

**TÌNH HUỐNG:**
- ✅ SV không mượn sách nào → Đình chỉ thành công
- ❌ SV đang mượn 3 cuốn → KHÔNG cho phép đình chỉ
- ✅ Kích hoạt lại SV (SUSPENDED → ACTIVE) → Thành công (không cần check)
- ✅ Đổi từ ACTIVE → ACTIVE (không thay đổi) → Vẫn thành công

**LÝ DO NGHIỆP VỤ:**
Phải chờ sinh viên trả hết sách trước khi đình chỉ, tránh trường hợp:
- SV bị đình chỉ nhưng vẫn giữ sách → Không theo dõi được
- Sách bị thất lạc do SV đã rời trường

---

## MODULE 3: BOOK (6 APIs)

### API 8: POST /api/v1/books - Thêm sách mới vào thư viện

**NGHIỆP VỤ THỰC TẾ:**
Thư viện mua sách mới, thủ thư nhập thông tin vào hệ thống

**REQUEST:**
```
POST /api/v1/books
{
  "title": "Clean Code",
  "author": "Robert C. Martin",
  "isbn": "9780132350884",
  "categoryId": 1,
  "quantityTotal": 10
}
```

**VALIDATIONS:**
- `title`: Bắt buộc, max 200 ký tự
- `author`: Bắt buộc, max 100 ký tự
- `isbn`: Bắt buộc, 10-13 ký tự, UNIQUE
- `categoryId`: Bắt buộc, phải tồn tại trong DB
- `quantityTotal`: Bắt buộc, min = 1

**BUSINESS LOGIC:**
1. Check categoryId có tồn tại không?
   - Nếu không → Error "Category not found"
2. Check ISBN đã tồn tại chưa?
   - Nếu có → Error "ISBN already exists"
3. Tạo Book mới với defaults:
   - `quantityAvailable` = quantityTotal (ban đầu tất cả đều available)
   - `status` = AVAILABLE (sách mới luôn AVAILABLE)
4. Save vào DB
5. Return thông tin sách đã tạo

**RESPONSE SUCCESS (201):**
```json
{
  "status": 201,
  "message": "Book created successfully",
  "data": {
    "id": 1,
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "9780132350884",
    "category": {
      "id": 1,
      "code": "IT",
      "name": "Công nghệ thông tin"
    },
    "quantityTotal": 10,
    "quantityAvailable": 10,
    "status": "AVAILABLE",
    "isAvailable": true,
    "createdAt": "2026-01-19T18:00:00"
  }
}
```

**RESPONSE ERROR (400):**
```json
{
  "status": 400,
  "message": "ISBN already exists: 9780132350884"
}
```

**TÌNH HUỐNG:**
- ✅ Nhập sách mới thành công
- ❌ ISBN đã có trong hệ thống → Error
- ❌ CategoryId không tồn tại → Error "Category not found"
- ❌ quantityTotal = 0 → Validation error

---

### API 9: GET /api/v1/books - Tìm kiếm sách

**NGHIỆP VỤ THỰC TẾ:**
- Sinh viên tìm sách để mượn
- Thủ thư tìm sách để kiểm tra tồn kho
- Lọc sách theo danh mục, trạng thái
- Chỉ hiển thị sách còn mượn được

**REQUEST (Example 1 - Tìm sách có từ "Clean", còn mượn được):**
```
GET /api/v1/books?search=Clean&onlyAvailable=true
```

**REQUEST (Example 2 - Sách danh mục IT, đang AVAILABLE):**
```
GET /api/v1/books?categoryId=1&status=AVAILABLE
```

**REQUEST (Example 3 - Search "Martin", sort theo title, trang 0):**
```
GET /api/v1/books?search=Martin&sortBy=title&sortDir=asc&pageSize=20
```

**QUERY PARAMETERS:**

| Param | Type | Required | Default | Mô tả |
|-------|------|----------|---------|-------|
| `search` | String | No | null | Tìm theo title/author/isbn (tìm kiếm mờ) |
| `categoryId` | Long | No | null | Lọc theo danh mục |
| `status` | Enum | No | null | Lọc theo trạng thái (AVAILABLE, MAINTENANCE, LOST, DAMAGED, ARCHIVED) |
| `onlyAvailable` | Boolean | No | false | `true` = Chỉ hiển thị sách còn mượn được (quantityAvailable > 0 AND status = AVAILABLE) |
| `pageNo` | int | No | 0 | Số trang |
| `pageSize` | int | No | 10 | Số items mỗi trang (max: 100) |
| `sortBy` | String | No | id | Field để sort (id, title, author, createdAt) |
| `sortDir` | String | No | desc | Hướng sort (asc, desc) |

**BUSINESS LOGIC:**
1. Validate pageSize (max 100)
2. Validate sortBy (chỉ cho phép: id, title, author, createdAt)
3. Build dynamic query:
   - Nếu `search` không null → Tìm trong title OR author OR isbn
   - Nếu `categoryId` không null → AND category.id = ?
   - Nếu `status` không null → AND status = ?
   - Nếu `onlyAvailable` = true → AND quantityAvailable > 0 AND status = AVAILABLE
4. Apply pagination và sorting
5. Tính field `isAvailable` cho mỗi sách (quantityAvailable > 0 AND status = AVAILABLE)
6. Return kết quả dạng Page

**RESPONSE SUCCESS (200):**
```json
{
  "status": 200,
  "message": "Get books successfully",
  "data": {
    "content": [
      {
        "id": 1,
        "title": "Clean Code",
        "author": "Robert C. Martin",
        "isbn": "9780132350884",
        "category": {
          "id": 1,
          "code": "IT",
          "name": "Công nghệ thông tin"
        },
        "quantityTotal": 10,
        "quantityAvailable": 7,
        "status": "AVAILABLE",
        "isAvailable": true
      },
      {
        "id": 5,
        "title": "Clean Architecture",
        "author": "Robert C. Martin",
        "isbn": "9780134494166",
        "category": {
          "id": 1,
          "code": "IT",
          "name": "Công nghệ thông tin"
        },
        "quantityTotal": 5,
        "quantityAvailable": 0,
        "status": "AVAILABLE",
        "isAvailable": false
      }
    ],
    "pageNo": 0,
    "pageSize": 20,
    "totalElements": 45,
    "totalPages": 3,
    "last": false,
    "first": true
  }
}
```

**TÌNH HUỐNG:**
- ✅ Tìm "Clean" → Trả về "Clean Code", "Clean Architecture"
- ✅ onlyAvailable=true → Chỉ trả về sách còn mượn được
- ✅ Lọc category IT + status AVAILABLE → Chỉ trả về sách thỏa mãn
- ✅ Không có kết quả → Trả về empty array

---

### API 10: GET /api/v1/books/{id} - Lấy chi tiết sách

**NGHIỆP VỤ THỰC TẾ:**
Xem thông tin chi tiết 1 cuốn sách (khi click vào tên sách)

**REQUEST:**
```
GET /api/v1/books/1
```

**BUSINESS LOGIC:**
1. Tìm book theo ID
2. Nếu không tìm thấy → Error "Book not found"
3. Tính `isAvailable` (còn mượn được không)
4. Return đầy đủ thông tin

**RESPONSE SUCCESS (200):**
```json
{
  "status": 200,
  "message": "Get book successfully",
  "data": {
    "id": 1,
    "title": "Clean Code",
    "author": "Robert C. Martin",
    "isbn": "9780132350884",
    "category": {
      "id": 1,
      "code": "IT",
      "name": "Công nghệ thông tin"
    },
    "quantityTotal": 10,
    "quantityAvailable": 7,
    "status": "AVAILABLE",
    "isAvailable": true,
    "createdAt": "2026-01-19T18:00:00",
    "updatedAt": "2026-01-20T10:00:00"
  }
}
```

**RESPONSE ERROR (400):**
```json
{
  "status": 400,
  "message": "Book not found with id: 999"
}
```

---

### API 11: PUT /api/v1/books/{id} - Cập nhật thông tin sách

**NGHIỆP VỤ THỰC TẾ:**
Sửa thông tin sách (tên, tác giả, số lượng, ...)

**REQUEST:**
```
PUT /api/v1/books/1
{
  "title": "Clean Code - Updated Edition",
  "author": "Robert C. Martin",
  "quantityTotal": 15
}
```

**LƯU Ý:**
- Tất cả fields đều OPTIONAL
- **KHÔNG được phép update ISBN** (ISBN là immutable)

**BUSINESS LOGIC:**
1. Tìm book theo ID
2. Nếu update quantityTotal:
   - **VALIDATION QUAN TRỌNG:**
   - Tính số sách đang được mượn: `borrowing = quantityTotal - quantityAvailable`
   - quantityTotal mới phải >= borrowing
   - Nếu < borrowing → Error "Cannot reduce quantity below currently borrowed books"
   - Ví dụ: quantityTotal=10, quantityAvailable=7 → borrowing=3
     - Có thể update quantityTotal = 8 (OK, vì 8 >= 3)
     - Không được update quantityTotal = 2 (Error, vì 2 < 3)
3. Update quantityAvailable nếu quantityTotal thay đổi:
   - quantityAvailable_new = quantityTotal_new - borrowing
4. Update các fields khác
5. Return thông tin mới

**RESPONSE SUCCESS (200):**
```json
{
  "status": 200,
  "message": "Book updated successfully",
  "data": {
    "id": 1,
    "title": "Clean Code - Updated Edition",
    "quantityTotal": 15,
    "quantityAvailable": 12,
    "updatedAt": "2026-01-20T11:00:00"
  }
}
```

**RESPONSE ERROR (400):**
```json
{
  "status": 400,
  "message": "Cannot reduce quantity to 2 because 3 books are currently borrowed"
}
```

**TÌNH HUỐNG:**
- ✅ Tăng quantityTotal (mua thêm sách) → Thành công
- ✅ Giảm quantityTotal nhưng vẫn đủ cho số đang mượn → Thành công
- ❌ Giảm quantityTotal xuống dưới số đang mượn → Error

---

### API 12: PATCH /api/v1/books/{id}/status - Đổi trạng thái sách

**NGHIỆP VỤ THỰC TẾ:**
- Sách hỏng → Chuyển sang MAINTENANCE
- Sách mất → Chuyển sang LOST
- Sách hư hỏng không sửa được → DAMAGED
- Sách cũ lưu kho → ARCHIVED

**REQUEST:**
```
PATCH /api/v1/books/1/status
{
  "status": "MAINTENANCE",
  "reason": "Bìa sách bị rách, cần sửa chữa"
}
```

**VALIDATIONS:**
- `status`: Bắt buộc, enum (AVAILABLE, MAINTENANCE, LOST, DAMAGED, ARCHIVED)
- `reason`: Optional, max 500 ký tự

**BUSINESS LOGIC:**
1. Tìm book theo ID
2. Update status
3. **LƯU Ý:**
   - Nếu đổi sang status != AVAILABLE → Sách không thể mượn được (ngay cả khi quantityAvailable > 0)
   - Không cần validation đặc biệt (khác với Student status)
4. Return thông tin mới

**RESPONSE SUCCESS (200):**
```json
{
  "status": 200,
  "message": "Book status updated successfully",
  "data": {
    "id": 1,
    "title": "Clean Code",
    "status": "MAINTENANCE",
    "isAvailable": false,
    "updatedAt": "2026-01-20T12:00:00"
  }
}
```

**TÌNH HUỐNG:**
- ✅ Đổi sang MAINTENANCE → Sách không mượn được
- ✅ Đổi về AVAILABLE sau khi sửa xong → Sách lại mượn được
- ✅ Đổi sang LOST → Sách bị mất, không mượn được

---

### API 13: GET /api/v1/books/most-borrowed - Top sách được mượn nhiều nhất

**NGHIỆP VỤ THỰC TẾ:**
Thống kê sách phổ biến để:
- Mua thêm sách được ưa chuộng
- Hiển thị "Top Books" cho sinh viên tham khảo

**REQUEST:**
```
GET /api/v1/books/most-borrowed?limit=10&timeRange=THIS_MONTH
```

**QUERY PARAMETERS:**

| Param | Type | Required | Default | Mô tả |
|-------|------|----------|---------|-------|
| `limit` | int | No | 10 | Số lượng top (max: 50) |
| `timeRange` | Enum | No | ALL_TIME | ALL_TIME, THIS_MONTH, THIS_YEAR |

**BUSINESS LOGIC:**
1. Query tất cả BorrowRecords trong timeRange
2. Group by bookId và đếm số lần mượn
3. Sort theo số lần mượn giảm dần
4. Limit theo param
5. Join với Book để lấy thông tin sách
6. Return danh sách top sách

**RESPONSE SUCCESS (200):**
```json
{
  "status": 200,
  "message": "Get most borrowed books successfully",
  "data": [
    {
      "rank": 1,
      "book": {
        "id": 5,
        "title": "Clean Code",
        "author": "Robert C. Martin",
        "isbn": "9780132350884"
      },
      "totalBorrowCount": 156,
      "currentBorrowingCount": 8
    },
    {
      "rank": 2,
      "book": {
        "id": 12,
        "title": "Design Patterns",
        "author": "Gang of Four",
        "isbn": "9780201633610"
      },
      "totalBorrowCount": 143,
      "currentBorrowingCount": 5
    },
    {
      "rank": 3,
      "book": {
        "id": 8,
        "title": "Refactoring",
        "author": "Martin Fowler",
        "isbn": "9780201485677"
      },
      "totalBorrowCount": 128,
      "currentBorrowingCount": 3
    }
  ]
}
```

**TÌNH HUỐNG:**
- ✅ Top 10 sách mượn nhiều nhất trong tháng này
- ✅ Top 5 sách mượn nhiều nhất từ trước đến nay
- ✅ Không có sách nào được mượn → Trả về empty array

---

## MODULE 4: BORROW (5 APIs) - CORE NHẤT!

### API 14: POST /api/v1/borrows - Sinh viên mượn sách

**NGHIỆP VỤ THỰC TẾ:**
Sinh viên đến thư viện, đưa thẻ SV và chọn sách muốn mượn.
Thủ thư quét thẻ SV, quét mã sách, hệ thống tự động kiểm tra và tạo phiếu mượn.

**REQUEST:**
```
POST /api/v1/borrows
{
  "studentId": 1,
  "bookId": 5,
  "notes": "Mượn để làm đồ án tốt nghiệp"
}
```

**VALIDATIONS:**
- `studentId`: Bắt buộc
- `bookId`: Bắt buộc
- `notes`: Optional, max 1000 ký tự

**BUSINESS LOGIC - 6 VALIDATIONS QUAN TRỌNG (THEO THỨ TỰ):**

**VALIDATION 1: Sinh viên tồn tại không?**
```
Query: SELECT * FROM tbl_student WHERE id = ?
Nếu không tìm thấy → Error (404): "Student not found with id: {id}"
```

**VALIDATION 2: Sinh viên đang ACTIVE không?**
```
Check: student.status == ACTIVE ?
Nếu SUSPENDED → Error (400): "Student is suspended, cannot borrow books"
```

**VALIDATION 3: Sách tồn tại không?**
```
Query: SELECT * FROM tbl_book WHERE id = ?
Nếu không tìm thấy → Error (404): "Book not found with id: {id}"
```

**VALIDATION 4: Sách có thể mượn không?**
```
Check 2 điều kiện:
a) book.quantityAvailable > 0
b) book.status == AVAILABLE

Nếu quantityAvailable = 0:
  → Error (400): "Book is out of stock. Please wait for return."

Nếu status == MAINTENANCE:
  → Error (400): "Book is under maintenance"

Nếu status == LOST:
  → Error (400): "Book is lost"

Nếu status == DAMAGED:
  → Error (400): "Book is damaged"

Nếu status == ARCHIVED:
  → Error (400): "Book is archived"
```

**VALIDATION 5: Đã đạt giới hạn mượn chưa?**
```
Query: SELECT COUNT(*) FROM tbl_borrow_record 
       WHERE student_id = ? AND status = 'BORROWING'
       
Count result = currentBorrowingCount

Check: currentBorrowingCount < student.maxBorrowLimit ?

Nếu đã đạt giới hạn (vd: đã mượn 5/5):
  → Error (400): "You have reached the borrowing limit (5/5). Please return books before borrowing more."
```

**VALIDATION 6: Đã đang mượn sách này chưa?**
```
Query: SELECT COUNT(*) FROM tbl_borrow_record
       WHERE student_id = ? AND book_id = ? AND status = 'BORROWING'

Nếu EXISTS (count > 0):
  → Error (400): "You are already borrowing this book. Please return it before borrowing again."
```

**NẾU TẤT CẢ 6 VALIDATIONS PASS:**

1. Tạo BorrowRecord:
```
borrowDate = today
dueDate = borrowDate + 14 days (hạn trả 2 tuần)
returnDate = null
status = BORROWING
notes = request.notes
```

2. **Giảm số lượng sách:**
```
book.quantityAvailable = book.quantityAvailable - 1
```

3. **TRANSACTION:**
```
@Transactional
BEGIN
  INSERT INTO tbl_borrow_record (...)
  UPDATE tbl_book SET quantity_available = quantity_available - 1 WHERE id = ?
COMMIT
```

4. Return thông tin phiếu mượn

**RESPONSE SUCCESS (201):**
```json
{
  "status": 201,
  "message": "Borrow book successfully",
  "data": {
    "id": 10,
    "student": {
      "id": 1,
      "studentCode": "SV000001",
      "fullName": "Nguyễn Văn A",
      "email": "a@student.edu.vn"
    },
    "book": {
      "id": 5,
      "title": "Clean Code",
      "author": "Robert C. Martin",
      "isbn": "9780132350884"
    },
    "borrowDate": "2026-01-19",
    "dueDate": "2026-02-02",
    "returnDate": null,
    "status": "BORROWING",
    "daysRemaining": 14,
    "isOverdue": false,
    "notes": "Mượn để làm đồ án tốt nghiệp",
    "createdAt": "2026-01-19T14:30:00"
  }
}
```

**RESPONSE ERRORS:**

**Error 1 - Student not found (404):**
```json
{
  "status": 404,
  "message": "Student not found with id: 999"
}
```

**Error 2 - Student SUSPENDED (400):**
```json
{
  "status": 400,
  "message": "Student is suspended, cannot borrow books"
}
```

**Error 3 - Book not found (404):**
```json
{
  "status": 404,
  "message": "Book not found with id: 999"
}
```

**Error 4a - Book out of stock (400):**
```json
{
  "status": 400,
  "message": "Book is out of stock. Please wait for return."
}
```

**Error 4b - Book MAINTENANCE (400):**
```json
{
  "status": 400,
  "message": "Book is under maintenance"
}
```

**Error 5 - Reached limit (400):**
```json
{
  "status": 400,
  "message": "You have reached the borrowing limit (5/5). Please return books before borrowing more."
}
```

**Error 6 - Already borrowing (400):**
```json
{
  "status": 400,
  "message": "You are already borrowing this book. Please return it before borrowing again."
}
```

**TÌNH HUỐNG:**
- ✅ Tất cả OK → Mượn thành công, quantity giảm 1
- ❌ SV không tồn tại → Error
- ❌ SV bị đình chỉ → Error
- ❌ Sách không tồn tại → Error
- ❌ Sách hết (quantityAvailable = 0) → Error
- ❌ Sách đang bảo trì → Error
- ❌ SV đã mượn 5/5 cuốn → Error
- ❌ SV đang mượn sách này rồi → Error

**TẠI SAO CẦN 6 VALIDATIONS THEO THỨ TỰ NÀY?**
- Check tồn tại trước (V1, V3) → Tránh NullPointerException
- Check status sau (V2, V4) → Vì cần object để check
- Check expensive queries cuối (V5, V6) → Query COUNT tốn tài nguyên, chỉ chạy khi cần
- **FAIL FAST principle:** Nếu lỗi ở V1 → Dừng ngay, không chạy V2-V6

---

### API 15: GET /api/v1/borrows - Danh sách phiếu mượn

**NGHIỆP VỤ THỰC TẾ:**
Thủ thư xem danh sách phiếu mượn để:
- Quản lý sách đang được mượn
- Tìm phiếu của 1 sinh viên cụ thể
- Lọc theo trạng thái (đang mượn/đã trả/quá hạn)
- Lọc theo khoảng thời gian

**REQUEST (Example 1 - Tất cả phiếu đang mượn):**
```
GET /api/v1/borrows?status=BORROWING
```

**REQUEST (Example 2 - Phiếu của SV có ID=1):**
```
GET /api/v1/borrows?studentId=1&sortBy=borrowDate&sortDir=desc
```

**REQUEST (Example 3 - Phiếu từ 01/01 đến 31/01):**
```
GET /api/v1/borrows?fromDate=2026-01-01&toDate=2026-01-31
```

**QUERY PARAMETERS:**

| Param | Type | Required | Default | Mô tả |
|-------|------|----------|---------|-------|
| `studentId` | Long | No | null | Lọc theo sinh viên |
| `bookId` | Long | No | null | Lọc theo sách |
| `status` | Enum | No | null | Lọc theo trạng thái (BORROWING, RETURNED, OVERDUE) |
| `fromDate` | Date | No | null | Từ ngày (yyyy-MM-dd) |
| `toDate` | Date | No | null | Đến ngày (yyyy-MM-dd) |
| `pageNo` | int | No | 0 | Số trang |
| `pageSize` | int | No | 10 | Kích thước trang |
| `sortBy` | String | No | borrowDate | Sort field (borrowDate, dueDate) |
| `sortDir` | String | No | desc | Sort direction |

**BUSINESS LOGIC:**
1. Build dynamic query với filters
2. Apply pagination và sorting
3. Tính các fields:
   - `daysRemaining` = dueDate - today (số ngày còn lại)
   - `isOverdue` = (dueDate < today AND status = BORROWING)
   - `daysOverdue` = today - dueDate (nếu quá hạn)
4. Return kết quả dạng Page

**RESPONSE SUCCESS (200):**
```json
{
  "status": 200,
  "message": "Get borrow records successfully",
  "data": {
    "content": [
      {
        "id": 10,
        "student": {
          "id": 1,
          "studentCode": "SV000001",
          "fullName": "Nguyễn Văn A"
        },
        "book": {
          "id": 5,
          "title": "Clean Code",
          "author": "Robert C. Martin"
        },
        "borrowDate": "2026-01-19",
        "dueDate": "2026-02-02",
        "returnDate": null,
        "status": "BORROWING",
        "daysRemaining": 14,
        "isOverdue": false
      },
      {
        "id": 8,
        "student": {
          "id": 3,
          "studentCode": "SV000003",
          "fullName": "Trần Văn C"
        },
        "book": {
          "id": 2,
          "title": "Design Patterns",
          "author": "Gang of Four"
        },
        "borrowDate": "2026-01-01",
        "dueDate": "2026-01-15",
        "returnDate": null,
        "status": "BORROWING",
        "daysRemaining": -4,
        "isOverdue": true,
        "daysOverdue": 4
      }
    ],
    "pageNo": 0,
    "pageSize": 10,
    "totalElements": 234,
    "totalPages": 24,
    "last": false,
    "first": true
  }
}
```

**TÌNH HUỐNG:**
- ✅ Lọc status=BORROWING → Chỉ phiếu đang mượn
- ✅ Lọc studentId=1 → Phiếu của SV này
- ✅ Lọc date range → Phiếu trong khoảng thời gian
- ✅ Kết hợp nhiều filters → AND logic

---

### API 16: GET /api/v1/borrows/{id} - Chi tiết phiếu mượn

**NGHIỆP VỤ THỰC TẾ:**
Xem chi tiết 1 phiếu mượn cụ thể

**REQUEST:**
```
GET /api/v1/borrows/10
```

**BUSINESS LOGIC:**
1. Tìm BorrowRecord theo ID
2. Nếu không tìm thấy → Error
3. Tính daysRemaining, isOverdue
4. Return đầy đủ thông tin

**RESPONSE SUCCESS (200):**
```json
{
  "status": 200,
  "message": "Get borrow record successfully",
  "data": {
    "id": 10,
    "student": {
      "id": 1,
      "studentCode": "SV000001",
      "fullName": "Nguyễn Văn A",
      "email": "a@student.edu.vn",
      "phone": "0901234567"
    },
    "book": {
      "id": 5,
      "title": "Clean Code",
      "author": "Robert C. Martin",
      "isbn": "9780132350884",
      "category": {
        "id": 1,
        "code": "IT",
        "name": "Công nghệ thông tin"
      }
    },
    "borrowDate": "2026-01-19",
    "dueDate": "2026-02-02",
    "returnDate": null,
    "status": "BORROWING",
    "daysRemaining": 14,
    "isOverdue": false,
    "notes": "Mượn để làm đồ án",
    "createdAt": "2026-01-19T14:30:00",
    "updatedAt": "2026-01-19T14:30:00"
  }
}
```

---

### API 17: POST /api/v1/borrows/{id}/return - Trả sách

**NGHIỆP VỤ THỰC TẾ:**
Sinh viên trả sách, thủ thư quét mã phiếu mượn, hệ thống cập nhật trạng thái

**REQUEST:**
```
POST /api/v1/borrows/10/return
```

**REQUEST BODY (Optional):**
```json
{
  "notes": "Sách nguyên vẹn"
}
```

**BUSINESS LOGIC - 2 VALIDATIONS:**

**VALIDATION 1: Phiếu mượn tồn tại không?**
```
Query: SELECT * FROM tbl_borrow_record WHERE id = ?
Nếu không tìm thấy → Error (404): "Borrow record not found with id: {id}"
```

**VALIDATION 2: Phiếu chưa trả chưa?**
```
Check: borrowRecord.returnDate == null ?
Nếu đã trả (returnDate != null):
  → Error (400): "This book has already been returned on {returnDate}"
```

**NẾU PASS:**

1. Update BorrowRecord:
```
returnDate = today
status = RETURNED
notes = merge(oldNotes, newNotes)
```

2. **Tăng số lượng sách (restore):**
```
book.quantityAvailable = book.quantityAvailable + 1
```

3. **TRANSACTION:**
```
@Transactional
BEGIN
  UPDATE tbl_borrow_record SET return_date = ?, status = 'RETURNED' WHERE id = ?
  UPDATE tbl_book SET quantity_available = quantity_available + 1 WHERE id = ?
COMMIT
```

4. Return thông tin phiếu đã trả

**RESPONSE SUCCESS (200):**
```json
{
  "status": 200,
  "message": "Return book successfully",
  "data": {
    "id": 10,
    "student": {
      "id": 1,
      "studentCode": "SV000001",
      "fullName": "Nguyễn Văn A"
    },
    "book": {
      "id": 5,
      "title": "Clean Code",
      "author": "Robert C. Martin"
    },
    "borrowDate": "2026-01-19",
    "dueDate": "2026-02-02",
    "returnDate": "2026-01-25",
    "status": "RETURNED",
    "daysRemaining": 0,
    "isOverdue": false,
    "updatedAt": "2026-01-25T10:00:00"
  }
}
```

**RESPONSE ERROR (400 - Đã trả rồi):**
```json
{
  "status": 400,
  "message": "This book has already been returned on 2026-01-25"
}
```

**TÌNH HUỐNG:**
- ✅ Trả sách đúng hạn → OK, quantity tăng 1
- ✅ Trả sách muộn → OK, quantity vẫn tăng 1 (có thể tính phí sau)
- ❌ Trả lần 2 → Error "Already returned"
- ❌ ID không tồn tại → Error "Not found"

**TẠI SAO CẦN TRANSACTION?**
Đảm bảo 2 operations (update borrow + restore quantity) xảy ra cùng lúc:
- Nếu update borrow thành công nhưng restore quantity fail → Inconsistent data
- Transaction đảm bảo: CẢ HAI thành công HOẶC CẢ HAI rollback

---

### API 18: GET /api/v1/borrows/overdue - Sách quá hạn

**NGHIỆP VỤ THỰC TẾ:**
Thủ thư xem danh sách sách quá hạn để nhắc nhở sinh viên trả

**REQUEST:**
```
GET /api/v1/borrows/overdue
```

**BUSINESS LOGIC:**
```
Query: SELECT * FROM tbl_borrow_record
       WHERE status = 'BORROWING'
       AND due_date < CURRENT_DATE
       ORDER BY due_date ASC
```

**Tính toán:**
- `daysOverdue` = today - dueDate
- Sort theo `daysOverdue` DESC (quá hạn nhiều nhất trước)

**RESPONSE SUCCESS (200):**
```json
{
  "status": 200,
  "message": "Get overdue books successfully",
  "data": [
    {
      "id": 8,
      "student": {
        "id": 3,
        "studentCode": "SV000003",
        "fullName": "Trần Văn C",
        "email": "c@student.edu.vn",
        "phone": "0901234568"
      },
      "book": {
        "id": 2,
        "title": "Design Patterns",
        "author": "Gang of Four"
      },
      "borrowDate": "2026-01-01",
      "dueDate": "2026-01-15",
      "returnDate": null,
      "status": "BORROWING",
      "daysRemaining": -4,
      "isOverdue": true,
      "daysOverdue": 4
    },
    {
      "id": 12,
      "student": {
        "id": 7,
        "studentCode": "SV000007",
        "fullName": "Phạm Thị D",
        "email": "d@student.edu.vn"
      },
      "book": {
        "id": 9,
        "title": "The Pragmatic Programmer",
        "author": "Andy Hunt"
      },
      "borrowDate": "2026-01-10",
      "dueDate": "2026-01-18",
      "returnDate": null,
      "status": "BORROWING",
      "daysRemaining": -1,
      "isOverdue": true,
      "daysOverdue": 1
    }
  ]
}
```

**TÌNH HUỐNG:**
- ✅ Có sách quá hạn → Trả về danh sách
- ✅ Không có sách quá hạn → Trả về empty array
- ✅ Sort theo mức độ quá hạn → Quá hạn nhiều nhất ở đầu

**ỨNG DỤNG THỰC TẾ:**
- Gửi email nhắc nhở sinh viên
- Tính phí phạt trả muộn
- Theo dõi vi phạm để đình chỉ

---

## MODULE 5: STATISTICS (1 API)

### API 19: GET /api/v1/stats/dashboard - Dashboard tổng quan

**NGHIỆP VỤ THỰC TẾ:**
Hiển thị dashboard tổng quan cho thủ thư trưởng/quản lý thư viện

**REQUEST:**
```
GET /api/v1/stats/dashboard
```

**BUSINESS LOGIC - AGGREGATE QUERIES:**

```sql
-- Total students
SELECT COUNT(*) FROM tbl_student

-- Active students
SELECT COUNT(*) FROM tbl_student WHERE status = 'ACTIVE'

-- Suspended students
SELECT COUNT(*) FROM tbl_student WHERE status = 'SUSPENDED'

-- Total books
SELECT COUNT(*) FROM tbl_book

-- Available books (có thể mượn)
SELECT SUM(quantity_available) FROM tbl_book WHERE status = 'AVAILABLE'

-- Total categories
SELECT COUNT(*) FROM tbl_category

-- Current borrowing
SELECT COUNT(*) FROM tbl_borrow_record WHERE status = 'BORROWING'

-- Overdue books
SELECT COUNT(*) FROM tbl_borrow_record 
WHERE status = 'BORROWING' AND due_date < CURRENT_DATE

-- Borrows this month
SELECT COUNT(*) FROM tbl_borrow_record 
WHERE DATE_FORMAT(borrow_date, '%Y-%m') = DATE_FORMAT(CURRENT_DATE, '%Y-%m')

-- Returns this month
SELECT COUNT(*) FROM tbl_borrow_record 
WHERE DATE_FORMAT(return_date, '%Y-%m') = DATE_FORMAT(CURRENT_DATE, '%Y-%m')
```

**Tính toán:**
- `borrowingRate` = (currentBorrowing / totalBooks) * 100

**RESPONSE SUCCESS (200):**
```json
{
  "status": 200,
  "message": "Get dashboard statistics successfully",
  "data": {
    "totalStudents": 1250,
    "totalActiveStudents": 1180,
    "totalSuspendedStudents": 70,
    "totalBooks": 5000,
    "totalAvailableBooks": 4544,
    "totalCategories": 12,
    "currentBorrowing": 456,
    "overdueBooks": 23,
    "totalBorrowsThisMonth": 678,
    "totalReturnsThisMonth": 645,
    "borrowingRate": 9.12,
    "lastUpdated": "2026-01-19T18:00:00"
  }
}
```

**INSIGHTS TỪ DỮ LIỆU:**
- totalStudents = 1250, activeStudents = 1180 → 70 SV bị đình chỉ (5.6%)
- currentBorrowing = 456, overdueBooks = 23 → 5% sách bị quá hạn
- borrowingRate = 9.12% → Tỷ lệ sách đang mượn thấp, có thể tăng marketing
- borrowsThisMonth > returnsThisMonth → Tích cực, sinh viên đang mượn nhiều

---

## MODULE 6: STUDENT HISTORY (1 API)

### API 20: GET /api/v1/students/{id}/history - Lịch sử mượn sách của sinh viên

**NGHIỆP VỤ THỰC TẾ:**
Sinh viên xem lịch sử mượn sách của bản thân hoặc thủ thư xem lịch sử của 1 sinh viên

**REQUEST:**
```
GET /api/v1/students/1/history?status=RETURNED&pageNo=0&pageSize=20
```

**QUERY PARAMETERS:**

| Param | Type | Required | Default | Mô tả |
|-------|------|----------|---------|-------|
| `status` | Enum | No | null | Lọc theo trạng thái (BORROWING, RETURNED) |
| `pageNo` | int | No | 0 | Số trang |
| `pageSize` | int | No | 10 | Kích thước trang |

**BUSINESS LOGIC:**
```sql
SELECT * FROM tbl_borrow_record
WHERE student_id = ?
  AND (:status IS NULL OR status = :status)
ORDER BY borrow_date DESC
```

**RESPONSE SUCCESS (200):**
```json
{
  "status": 200,
  "message": "Get student borrow history successfully",
  "data": {
    "content": [
      {
        "id": 25,
        "book": {
          "id": 12,
          "title": "Refactoring",
          "author": "Martin Fowler"
        },
        "borrowDate": "2026-01-15",
        "dueDate": "2026-01-29",
        "returnDate": "2026-01-20",
        "status": "RETURNED",
        "isOverdue": false
      },
      {
        "id": 18,
        "book": {
          "id": 5,
          "title": "Clean Code",
          "author": "Robert C. Martin"
        },
        "borrowDate": "2026-01-05",
        "dueDate": "2026-01-19",
        "returnDate": "2026-01-22",
        "status": "RETURNED",
        "isOverdue": true,
        "daysOverdue": 3
      }
    ],
    "pageNo": 0,
    "pageSize": 20,
    "totalElements": 45,
    "totalPages": 3,
    "last": false,
    "first": true
  }
}
```

**TÌNH HUỐNG:**
- ✅ Xem tất cả lịch sử → Không filter status
- ✅ Chỉ xem sách đã trả → filter status=RETURNED
- ✅ Chỉ xem sách đang mượn → filter status=BORROWING
- ✅ Không có lịch sử → Trả về empty array

---

## TỔNG KẾT 20 APIs

### MỨC ĐỘ QUAN TRỌNG:

**🔥🔥🔥 CỰC KỲ QUAN TRỌNG (10/10):**
- API 14: POST /borrows (6 validations + transaction)
- API 17: POST /borrows/{id}/return (transaction + restore quantity)

**🔥🔥 RẤT QUAN TRỌNG (9/10):**
- API 3: POST /students (auto-gen studentCode)
- API 4: GET /students (dynamic search + pagination)
- API 7: PATCH /students/{id}/status (validation: không suspend nếu đang mượn)
- API 9: GET /books (dynamic search + onlyAvailable filter)
- API 15: GET /borrows (dynamic search cho phiếu mượn)

**🔥 QUAN TRỌNG (8/10):**
- API 6: PUT /students (update với unique validation)
- API 11: PUT /books (validate quantity logic)
- API 13: GET /books/most-borrowed (aggregate query)
- API 18: GET /borrows/overdue (overdue tracking)
- API 19: GET /stats/dashboard (aggregate statistics)

**✅ CƠ BẢN (7/10):**
- API 1-2: Category CRUD
- API 5, 10, 16: Get detail APIs
- API 8: POST /books
- API 12: PATCH /books/status
- API 20: GET /students/history

---

## BUSINESS RULES SUMMARY

### SINH VIÊN:
1. studentCode auto-gen (SV000001, SV000002, ...)
2. Email, phone phải unique
3. Mặc định: status=ACTIVE, maxBorrowLimit=5
4. Không thể suspend nếu đang mượn sách

### SÁCH:
1. quantityAvailable = quantityTotal - (số đang mượn)
2. Chỉ mượn được khi: quantityAvailable > 0 AND status = AVAILABLE
3. Không thể giảm quantityTotal xuống dưới số đang mượn
4. ISBN phải unique

### MƯỢN SÁCH:
1. 6 validations theo thứ tự (fail fast)
2. Hạn trả: 14 ngày kể từ ngày mượn
3. Transaction: tạo borrow + giảm quantity
4. Không mượn được nếu:
   - SV suspended
   - Đã đạt giới hạn (5/5)
   - Sách hết hoặc không available
   - Đang mượn sách này rồi

### TRẢ SÁCH:
1. Transaction: update borrow + restore quantity
2. Không thể trả lần 2 (check returnDate != null)
3. returnDate = ngày trả thực tế (có thể muộn hơn dueDate)

---

**FILE NÀY CHI TIẾT HOÀN CHỈNH 20 APIs!** 🔥

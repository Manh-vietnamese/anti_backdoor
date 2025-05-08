
# Plugin Anti Backdoor OP for Minecraft

![License](https://img.shields.io/badge/License-MIT-green)  
**Bảo vệ server khỏi OP trái phép và hành vi giả mạo quyền Admin**


## 📥 Cài đặt
1. Tải file [anti-backdoor-op.jar](https://github.com/your-repo/releases) từ phần Releases.
2. Đặt file vào thư mục `plugins` của server Minecraft.
3. Khởi động server để tự động sinh file cấu hình.

---

## 🛡️ Tính năng nổi bật
- **Tự động phát hiện và xử lý OP trái phép**  
- **Quản lý whitelist OP bằng lệnh**  
- **Ban tạm thời/vĩnh viễn với lý do tùy chỉnh**  
- **Chặn lệnh `/op` mặc định của Minecraft**  
- **Ghi log chi tiết khi phát hiện vi phạm**  

---

## 📜 Lệnh và Quyền hạn
| Lệnh | Mô tả | Quyền |
|------|-------|-------|
| `/wop add <tên>` | Thêm người chơi vào whitelist OP | `Sunflower.SP.admin` |
| `/wop remove <tên>` | Xóa người chơi khỏi whitelist OP | `Sunflower.SP.admin` |
| `/wop ban <tên> <1d2h> <lý do>` | Ban người chơi (VD: `1d` = 1 ngày) | `Sunflower.SP.admin` |
| `/wop unban <tên>` | Gỡ ban cho người chơi | `Sunflower.SP.admin` |
| `/wop reload` | Tải lại toàn bộ cấu hình | `Sunflower.SP.admin` |
| `/wop list` | Xem danh sách OP hợp lệ | `Sunflower.SP.admin` |

---

## 🔍 Cơ chế phát hiện kẻ giả mạo
### 1. Quét OP định kỳ
- **Tần suất**: 5 giây/lần  
- **Hành động**:  
  → Kiểm tra toàn bộ danh sách OP server.  
  → Nếu phát hiện người chơi **KHÔNG** trong `whitelist_op.yml`:  
  ```yaml
  - Xóa quyền OP ngay lập tức
  - Áp dụng hình phạt (kick/ban) theo config.yml
  - Ghi log với UUID và thời gian vi phạm
  ```

### 2. Chặn lệnh `/op` gốc
- **Cơ chế**:  
  → Ghi đè lệnh `/op` mặc định của Minecraft.  
  → Chỉ cho phép OP nếu người chơi **ĐÃ** được thêm vào whitelist.  
- **Phản hồi**:  
  ```bash
  [System] Bạn không thể OP người chơi này!
  ```

### 3. Kiểm tra khi người chơi join server
- **Tự động kick** nếu người chơi đang trong danh sách ban (`bans.yml`).  
- **Thông báo ban chi tiết**:  
  ```java
  "§cBạn đã bị ban tạm thời!\n§fLý do: §eSử dụng OP trái phép"
  ```

---

## ⚙️ Cấu hình
### 📂 `config.yml`
```yaml
# Hình phạt khi vi phạm (kick/ban)
punishment_type: "ban"

# Thời gian ban (giây). -1 = Vĩnh viễn
ban_duration: 120
```

### 📂 `messages.yml`
```yaml
ban:
  temporary: |-
    &cBạn đã bị ban tạm thời!
    &fLý do: &e%reason%
    &fBan bởi: &e%admin%
    &fHết hạn: &e%time%
```

### 📂 `whitelist_op.yml`
```yaml
allowed_ops:
  - name: "Chanhne"
    uuid: "4bdcbc5b-af6d-3035-8026-30abd280ed66"
```

---

## 📌 Ví dụ thực tế
### 1. Thêm OP hợp lệ
```bash
/wop add Chanhne
→ [System] Đã thêm Chanhne vào whitelist OP!
```

### 2. Ban người chơi vi phạm
```bash
/wop ban Hacker 7d Khai thác OP
→ [System] Đã ban Hacker trong 7 ngày!
```

### 3. Khi phát hiện OP trái phép
```java
[LOG] §4[Security] §cĐã xóa OP và kick Hacker - UUID: 1234abcd...
[LOG] §4[Security] §cLý do: Sử dụng OP trái phép!
```

---

## ❓ Khắc phục sự cố
- **Lỗi không reload được plugin**:  
  → Kiểm tra quyền `Sunflower.SP.admin` của người dùng.  
  → Đảm bảo file `messages.yml` không bị lỗi cú pháp.  

- **Ban không hoạt động**:  
  → Kiểm tra định dạng thời gian ban (VD: `1d`, `2h`).  
  → Xác nhận quyền ghi file trong thư mục `plugins/anntibackdoor`.  

---

## 📄 Giấy phép
MIT License © 2023.  
Phát triển bởi [Your Name] - [GitHub Repository](https://github.com/your-repo)  
``` 

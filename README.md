# Plugin Anti Backdoor OP for Minecraft

![License](https://img.shields.io/badge/License-MIT-green)  
**Bảo vệ server khỏi BackDoor OP trái phép, giả mạo IP OP và hành vi đăng nhập bất thường**

---

## 📥 Cài đặt
1. Tải file [AntiBackDoor-1.0.0.jar](https://github.com/your-repo/releases) từ phần Releases.  
2. Đặt file vào thư mục `plugins` của server Minecraft.  
3. Khởi động server để tự động sinh file cấu hình.  

---

## 🛡️ Tính năng nổi bật  
- **Tự động phát hiện và xử lý backDoor OP trái phép**  
- **Quản lý whitelist OP + IP bằng lệnh**  
- **Chặn đăng nhập nếu IP không khớp với IP của OP đã cài đặt trong config**  
- **Ban tạm thời/vĩnh viễn với lý do tùy chỉnh**  
- **Ghi log chi tiết IP và thời gian đăng nhập**  

---

## 📜 Lệnh và Quyền hạn  
| Lệnh | Mô tả | Quyền |  
|------|-------|-------|  
| `/wop add <tên>` | Thêm OP và **tự động lưu IP** của người chơi | `Sunflower.SP.admin` |  
| `/wop remove <tên>` | Xóa OP và **xóa toàn bộ IP liên quan** | `Sunflower.SP.admin` |  
| `/wop ban <tên> <lý do> <1d2h>` | Ban người chơi (VD: `1d` = 1 ngày) | `Sunflower.SP.admin` |  
| `/wop list` | Xem danh sách OP và IP được phép | `Sunflower.SP.admin` |  
| `/wop reload` | Tải lại toàn bộ cấu hình | `Sunflower.SP.admin` |  

---

## 🔍 Cơ chế bảo mật nâng cao  
### 1. IP Whitelist  
- **Tự động lưu IP**: Khi dùng lệnh `/wop add`, IP hiện tại của người chơi được thêm vào `whitelist_op.yml` <***chỉ lần đầu, các lần sau phải thêm bằng tay qua file***>..  
- **Kiểm tra khi đăng nhập**:  
  → Nếu IP không khớp với danh sách đã lưu → **Kick ngay lập tức** với thông báo:  
  ```java
  "§cIP của bạn không được phép sử dụng OP!"
  ```  
- **Cấu hình IP**:  
  ```yaml
  allowed_ops:
    - name: "Chanhne"
      uuid: "4bdcbc5b-af6d-3035-8026-30abd280ed66"
      ips:
        - "192.168.1.1"  # IP được phép
  ```  

### 2. Ghi log đăng nhập  
- **File `ips.log`**: Lưu lại mọi lần đăng nhập với định dạng:  
  ```log
  [2023-10-01 14:30:00][Player1][uuid123][192.168.1.1]: đã đăng nhập.
  ```  
- **Mục đích**: Theo dõi hoạt động đăng nhập và phát hiện IP bất thường.  

### 3. Đồng bộ OP với `ops.json`  
- **Tự động thêm OP**: Nếu người chơi có trong `whitelist_op.yml` nhưng không có trong `ops.json` → **Tự động thêm OP**.  
- **Không đồng bộ ngược lại**: Thay đổi trong `ops.json` sẽ **không ảnh hưởng** đến `whitelist_op.yml`.  

---

## ⚙️ Cấu hình  
### 📂 `whitelist_op.yml`  
```yaml
allowed_ops:
  - name: "Chanhne"
    uuid: "4bdcbc5b-af6d-3035-8026-30abd280ed66"
    ips:  # Danh sách IP hợp lệ
      - "192.168.1.1"
      - "10.0.0.1"
```  

### 📂 `ips.log` (Tự động sinh)  
```log
[2023-10-01 14:30:00][Player1][uuid123][192.168.1.1]: đã đăng nhập.
```  

---

## 📌 Ví dụ thực tế  
### 1. Thêm OP và tự động lưu IP  
```bash
/wop add Player1  
> [01:53:31 INFO]: Đã thêm Player1 vào whitelist OP và cấp quyền OP!
```  

### 2. Kick khi IP không khớp  
```java
[01:55:20 INFO]: UUID of player Player1 is UUID
[01:55:21 INFO]: player lost connection: IP của bạn không được phép sử dụng OP!
[01:55:21 INFO]: player left the game
[01:55:21 WARN]: [anntibackdoor] player tried to login with invalid IP: 127.0.0.1
```  

### 3. Xem danh sách IP được phép  
```bash
/wop list  
→ UUID: 4bdcbc5b..., Tên: Chanhne, IP: 192.168.1.1, 10.0.0.1  
```  

--- 

## ❓ Khắc phục sự cố  
- **Lỗi IP không được lưu**:  
  → Kiểm tra xem người chơi đã online khi dùng `/wop add` chưa.  
  → Đảm bảo file `whitelist_op.yml` có quyền ghi.  

- **Log IP không hiển thị**:  
  → Kiểm tra thư mục `plugins/anntibackdoor` có file `ips.log`.  

--- 

## 📄 Giấy phép  
MIT License © 2023.  
Phát triển bởi [Your Name] - [GitHub Repository](https://github.com/your-repo)  
```

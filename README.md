# SushiZen – Android Sushi Restaurant Application

## 1. Giới thiệu

SushiZen là ứng dụng đặt món ăn Nhật Bản trên Android, được phát triển bằng Java và XML, kết hợp PHP REST API và MySQL để quản lý dữ liệu.

Hệ thống hỗ trợ hai vai trò chính:

- **Customer:** Đăng ký, đăng nhập, xem thực đơn, quản lý giỏ hàng, đặt món và theo dõi đơn hàng.
- **Driver:** Đăng nhập, xem đơn được phân công, nhận đơn, lấy hàng và xác nhận giao hàng thành công.

## 2. Công nghệ sử dụng

| Thành phần | Công nghệ |
|---|---|
| Mobile Application | Android Java, XML |
| Backend API | PHP |
| Database | MySQL / MariaDB |
| Development IDE | Android Studio |
| Local Server | XAMPP (Apache, MySQL) |
| API Testing | Postman |
| Version Control | Git, GitHub |

## 3. Cấu trúc Project

```text
524H0113_Project/
├── app/                  # Android application
│   └── src/main/
│       ├── java/         # Java source code
│       ├── res/          # XML layouts, images
│       └── AndroidManifest.xml
├── gradle/               # Gradle Wrapper
├── sushizen_api/         # PHP Backend
│   ├── auth/
│   ├── cart/
│   ├── driver/
│   ├── foods/
│   ├── order/
│   └── config/
├── build.gradle.kts
├── settings.gradle.kts
└── README.md
```

## 4. Hướng dẫn cài đặt

### Bước 1. Tải Project từ GitHub

Mở CMD hoặc PowerShell:

```bash
git clone https://github.com/MyNgoc19/524H0113_Project.git
```

Hoặc chọn **Code → Download ZIP** trên GitHub và giải nén.

### Bước 2. Cài đặt XAMPP

1. Cài đặt XAMPP.
2. Mở XAMPP Control Panel.
3. Khởi động **Apache** và **MySQL**.
4. Kiểm tra địa chỉ `http://localhost/`.

### Bước 3. Cấu hình PHP Backend

Sao chép thư mục `sushizen_api` từ project vào thư mục `htdocs` của XAMPP.

Ví dụ:

```text
C:\xampp\htdocs\sushizen_api\
```

Nếu XAMPP được cài đặt tại vị trí khác, sử dụng thư mục `htdocs` tương ứng.

Sau đó kiểm tra cấu hình kết nối MySQL trong:

```text
sushizen_api/config/database.php
```

Cập nhật host, tên database, username và password phù hợp với MySQL trên máy.

### Bước 4. Cài đặt Database

1. Khởi động MySQL trong XAMPP.
2. Mở phpMyAdmin hoặc MySQL Workbench.
3. Tạo database:

```sql
CREATE DATABASE IF NOT EXISTS sushizen_db;
```

4. Import file SQL của dự án vào database `sushizen_db`.
5. Kiểm tra các bảng và dữ liệu cần thiết đã được tạo.

**Lưu ý:** Cần có file SQL đầy đủ của database hiện tại, bao gồm các bảng liên quan đến khách hàng, món ăn, đơn hàng, tài xế và phân công giao hàng. Nếu repository chưa có file SQL, cần bổ sung trước khi người khác có thể chạy hệ thống đầy đủ.

### Bước 5. Kiểm tra PHP API

Sau khi cấu hình XAMPP và database, kiểm tra API bằng Postman hoặc trình duyệt.

Ví dụ:

```text
http://localhost/sushizen_api/foods/get_foods.php
```

Nếu API kết nối thành công, hệ thống sẽ trả về phản hồi JSON theo dữ liệu và logic của endpoint.

### Bước 6. Mở Android Project

1. Mở Android Studio.
2. Chọn **Open**.
3. Chọn thư mục `524H0113_Project`.
4. Chờ Android Studio hoàn tất Gradle Sync.
5. Cài đặt Android SDK nếu được yêu cầu.

### Bước 7. Cấu hình địa chỉ Backend

Mở file:

```text
app/src/main/java/com/example/project/api/ApiClient.java
```

Đối với Android Emulator, cấu hình Base URL:

```java
http://10.0.2.2/sushizen_api/
```

`10.0.2.2` là địa chỉ cho phép Android Emulator truy cập máy tính đang chạy XAMPP.

Nếu chạy trên điện thoại Android thật, cần sử dụng địa chỉ IP LAN của máy tính chạy XAMPP và đảm bảo hai thiết bị cùng mạng.

### Bước 8. Chạy ứng dụng

1. Khởi động Apache và MySQL.
2. Mở Android Studio.
3. Chọn Android Emulator hoặc thiết bị Android.
4. Nhấn **Run ▶**.
5. Đợi ứng dụng cài đặt và khởi chạy.
6. Đăng nhập bằng tài khoản kiểm thử được tạo trong database.

## 5. Kiểm thử chức năng

### Customer

- Đăng ký và đăng nhập.
- Xem danh sách món ăn.
- Thêm, cập nhật và xóa món trong giỏ hàng.
- Đặt hàng.
- Xem chi tiết và theo dõi trạng thái đơn hàng.

### Driver

- Đăng nhập bằng tài khoản Driver.
- Xem đơn hàng được phân công.
- Chấp nhận hoặc từ chối đơn hàng.
- Xác nhận lấy hàng.
- Xác nhận thu tiền COD nếu đơn hàng thanh toán COD.
- Hoàn thành giao hàng và kiểm tra lịch sử đơn đã giao.

## 6. Lưu ý

- XAMPP và MySQL phải đang chạy khi sử dụng các chức năng gọi API.
- Database cần được import trước khi chạy ứng dụng.
- Không đưa mật khẩu database thật, API key hoặc thông tin nhạy cảm lên GitHub.
- Khi chạy bằng Android Emulator, sử dụng `10.0.2.2` thay vì `localhost` để kết nối đến Backend trên máy tính.
- Việc chạy đầy đủ dự án yêu cầu mã nguồn Android, PHP API và file SQL tương thích.

# AsakaStats
Đây là plugin đầu tiên của mình tự làm (chắc vậy) nên nhìn ncc, cảm ơn vì da den

Phiên bản máy chủ Minecraft đã thử: 1.21.1
Phiên bản máy chủ Minecraft __CÓ THỂ SẼ DÙNG ĐƯỢC__: 1.19.x, 1.20.x

![image](https://github.com/user-attachments/assets/4b99af92-23f5-44c7-ada7-0f9cf5bdf659)

*btw, vào server của mình chơi nhé: [asakamc.net](https://asakamc.net/) :trollface:*

## Lệnh và quyền
- **/asakastats** -> Thông tin cơ bản (**Không có quyền**)
- **/resetstats <tên người chơi>** -> Reset stats của một player (**asakastats.admin**)
- **/stats** *hoặc* **/stats <tên người chơi>** -> Hiển thị stats của mình hoặc người chơi khác (**Không có quyền**)

## Placeholder API
- Yêu cầu: Cài plugin [Placeholder API](https://www.spigotmc.org/resources/placeholderapi.6245/)
- **%asakastats_**_kills_/_deaths_/_kdr_/_killstreak_/_topkillstreak_**%**
  + Mẫu:

    ![image](https://github.com/user-attachments/assets/54c975ea-6f1f-4e86-bf53-b7313dd9045c)
  + Preview:

    ![image](https://github.com/user-attachments/assets/6ab401be-c985-46bc-bf00-103692957724)



## Cách tự compile file
*Project dùng Java 17, API MC 1.20
- B1: Tải source code về: `git clone https://github.com/NotEnderVN/AsakaStats.git`
- B2: Dùng lệnh: `mvn clean package`
- B3: Sau khi báo thành công, vào folder `target` rồi lấy file `AsakaStats-x.x.jar`

Hoặc lấy file ở phần release

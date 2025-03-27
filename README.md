# AsakaStats [![CodeFactor](https://www.codefactor.io/repository/github/notendervn/asakastats/badge)](https://www.codefactor.io/repository/github/notendervn/asakastats)
Đây là plugin đầu tiên của mình tự làm (chắc vậy) nên nhìn ncc, cảm ơn vì da den

Phiên bản máy chủ Minecraft đã thử: 1.21.1/4

### Nếu muốn tùy chỉnh tin nhắn thì vào `AsakaStats.java` tự chỉnh r tự compile (như hướng dẫn bên dưới), lười làm file lang quá =)) *uoc ai lam giup*
## **YÊU CẦU [Placeholder API](https://www.spigotmc.org/resources/placeholderapi.6245/) để hoạt động**

![image](https://github.com/user-attachments/assets/4b99af92-23f5-44c7-ada7-0f9cf5bdf659)

## Lệnh và quyền
- **/asakastats** -> Thông tin cơ bản (**Không cần quyền**)
- **/stats** *hoặc* **/stats <tên người chơi>** -> Hiển thị stats của mình hoặc người chơi khác (**Không cần quyền**)
- **/resetstats <tên người chơi>** -> Reset stats của một player (**asakastats.admin**)

## Placeholder API
- **%asakastats_**_kills_/_deaths_/_kdr_/_killstreak_/_topkillstreak_**%**
  + Mẫu:

    ![image](https://github.com/user-attachments/assets/54c975ea-6f1f-4e86-bf53-b7313dd9045c)
  + Preview:

    ![image](https://github.com/user-attachments/assets/6ab401be-c985-46bc-bf00-103692957724)



## Cách tự compile file
*Project dùng Java 17, API MC 1.20
- B1: Clone source về: `git clone https://github.com/NotEnderVN/AsakaStats.git`
- B2: Compile bằng Maven: `mvn clean package`
- B3: Sau khi báo thành công, vào folder `target` rồi lấy file `AsakaStats-x.x.jar`

Hoặc lấy file ở phần release

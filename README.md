# 音乐应用系统

一个基于 Spring Boot + React 的全栈音乐播放平台。

## 项目结构

```
Music/
├── src/                    # 后端 Spring Boot 项目
├── frontend/               # 前端用户界面 (React + Vite)
├── admin/                  # 后台管理界面 (React + Vite)
├── pom.xml                 # Maven 配置文件
├── start.bat               # 启动脚本
└── stop.bat                # 停止脚本
```

## 技术栈

### 后端
- Java 21
- Spring Boot
- Spring Security + JWT
- JPA / Hibernate
- H2 数据库

### 前端
- React
- Vite
- Zustand (状态管理)

## 启动方式

### 1. 启动后端服务
```bash
# 在项目根目录执行
./mvnw spring-boot:run
```
或直接运行 `start.bat`

后端服务默认运行在: http://localhost:8080

### 2. 启动前端用户界面
```bash
cd frontend
npm install
npm run dev
```
前端用户界面: http://localhost:5173

### 3. 启动后台管理界面
```bash
cd admin
npm install
npm run dev
```
后台管理界面: http://localhost:5174

## 功能模块

### 用户端 (frontend)
- 用户注册/登录
- 音乐播放
- 歌单管理
- 搜索功能
- 个人中心
- 音乐上传

### 管理端 (admin)
- 管理员登录
- 用户管理
- 歌曲管理
- 数据统计

## API 接口

- `/api/auth/*` - 认证相关
- `/api/songs/*` - 歌曲管理
- `/api/playlists/*` - 歌单管理
- `/api/users/*` - 用户管理
- `/api/admin/*` - 管理员接口
- `/api/files/*` - 文件上传下载
- `/api/comments/*` - 评论功能

## 默认账户

系统初始化时会创建管理员账户，详见 `DataInitializer.java`

# Music - 自托管音乐流媒体平台

一个自托管全栈音乐流媒体平台，支持本地音乐上传管理，提供用户端和管理后台两个独立 SPA。

## 功能特性

### 用户端
- 音乐播放（播放/暂停、上下曲、进度拖拽、音量控制、随机播放、单曲/列表循环）
- 歌词面板
- 关键词搜索歌曲
- 歌单管理（创建、编辑、删除、添加/移除歌曲、上传封面）
- 收藏歌曲
- 播放历史记录
- 个人资料编辑（昵称、简介、头像、密码）
- 歌曲上传（支持 mp3/wav/flac/m4a/aac/ogg）
- 按流派浏览、每日推荐、热门歌曲、最新歌曲
- 验证码登录/注册

### 管理后台
- 数据概览仪表盘
- 用户管理（搜索、启用/禁用、角色变更、删除）
- 歌曲管理（搜索、上传、编辑、删除）
- 歌单管理（搜索、创建、编辑、歌曲管理、封面上传）
- 本地音乐批量导入
- 基于角色的访问控制（ADMIN）

## 技术栈

| 层级 | 技术 |
|------|------|
| 后端 | Java 21, Spring Boot 4.0.1, Spring Data JPA, Spring Security |
| 前端 | React 19, Vite 6, React Router 7, Zustand, Axios |
| 管理后台 | React 19, Vite 6, React Router 7, Zustand, Axios |
| 数据库 | MySQL 8.0 |
| 认证 | JWT (jjwt), BCrypt |
| 文件存储 | 本地文件系统 / MinIO / 阿里云 OSS（可切换） |
| 容器化 | Docker, Docker Compose, Nginx |
| CI/CD | GitHub Actions (CI + 自动部署到 GHCR) |

## 项目结构

```
Music/
├── src/main/java/com/example/music/   # Spring Boot 后端
│   ├── controller/                     # REST API 控制器
│   ├── service/                        # 业务逻辑
│   ├── entity/                         # JPA 实体
│   ├── repository/                     # 数据访问层
│   ├── security/                       # JWT 认证
│   ├── config/                         # 配置类
│   └── dto/                            # 数据传输对象
├── src/test/                           # 单元测试
├── frontend/                           # 用户端 React SPA (/frontend/)
├── admin/                              # 管理后台 React SPA (/admin/)
├── docker/                             # Nginx、MySQL 配置
├── .github/workflows/                  # CI/CD 工作流
├── Dockerfile                          # 多阶段构建
├── docker-compose.yml                  # 开发环境
├── docker-compose.prod.yml             # 生产环境
└── .env.example                        # 环境变量模板
```

## 快速开始

### 方式一：Windows 快速启动

```bash
start.bat    # 启动后端(8080)、前端(5173)、管理后台(5174)
stop.bat     # 停止所有服务
```

### 方式二：Docker Compose

```bash
cp .env.example .env    # 编辑环境变量
docker compose up -d
```

访问地址：
- 用户端：http://localhost:5173
- 管理后台：http://localhost:5174
- 后端 API：http://localhost:8080

### 方式三：手动启动

**前置要求：** Java 21+、Node.js 20+、MySQL 8.0

```bash
# 后端
./mvnw spring-boot:run

# 前端
cd frontend && npm install && npm run dev

# 管理后台
cd admin && npm install && npm run dev
```

## 环境变量

参考 `.env.example`，主要配置项：

| 变量 | 说明 |
|------|------|
| `DB_HOST` / `DB_PORT` / `DB_NAME` | 数据库连接 |
| `DB_USERNAME` / `DB_PASSWORD` | 数据库凭据 |
| `JWT_SECRET` | JWT 密钥（至少 256 位） |
| `STORAGE_TYPE` | 文件存储类型：`local` / `minio` / `oss` |
| `MINIO_ACCESS_KEY` / `MINIO_SECRET_KEY` | MinIO 凭据 |
| `CORS_ALLOWED_ORIGINS` | 允许的跨域来源 |

## 默认账号

首次启动会自动创建演示数据：

| 用户名 | 密码 | 角色 |
|--------|------|------|
| admin | admin123 | 管理员 |
| demo | demo123 | 普通用户 |
| test | test123 | 普通用户 |

## API 端点

| 路径 | 说明 |
|------|------|
| `POST /api/auth/login` | 登录 |
| `POST /api/auth/register` | 注册 |
| `GET /api/auth/captcha` | 获取验证码 |
| `GET /api/songs` | 歌曲列表（分页） |
| `GET /api/songs/search` | 搜索歌曲 |
| `GET /api/songs/genres` | 获取流派列表 |
| `GET /api/songs/genres/summary` | 流派摘要 |
| `GET /api/songs/daily` | 每日推荐 |
| `GET /api/songs/top` | 热门歌曲 |
| `GET /api/songs/latest` | 最新歌曲 |
| `GET /api/songs/random` | 随机歌曲 |
| `POST /api/songs/{id}/play` | 记录播放 |
| `POST /api/songs` | 上传歌曲 |
| `GET /api/playlists` | 公开歌单列表 |
| `GET /api/playlists/featured` | 推荐歌单 |
| `GET /api/playlists/my` | 我的歌单 |
| `GET /api/users/me` | 当前用户信息 |
| `GET /api/users/me/favorites` | 收藏列表 |
| `GET /api/users/me/history` | 播放历史 |
| `GET /api/music/stream/local/{id}` | 本地音乐流（支持 Range） |
| `GET /api/admin/**` | 管理接口（需 ADMIN 角色） |
| `GET /api/health` | 健康检查 |

## 生产部署

推送到 `main` 分支后，GitHub Actions 自动：

1. 构建后端 + 前端 Docker 镜像
2. 推送至 GitHub Container Registry (GHCR)
3. 通过 SSH 部署到服务器

生产环境使用 `docker-compose.prod.yml`，Nginx 作为反向代理，同时托管两个 SPA 并代理 API 请求。

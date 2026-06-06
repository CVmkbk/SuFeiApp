# 素扉 (SuFei)

中国传统诗词沉浸式阅读 Android 应用。

> **素** — 简约、留白、纯粹 · **扉** — 开启文化之门

---

## 功能

- **今日推荐** — 每日随机推荐一首诗词，竖排排版、印章效果，还原古籍美学
- **诗词详情** — 沉浸式阅读，支持 TTS 语音朗读（逐句高亮）、注释/译文/赏析
- **万卷探索** — 朝代/词牌/标签三级过滤 + 全文检索，毫秒级响应
- **诗人档案** — 收录 1 万+ 诗人，生平、成就、逸闻轶事
- **飞花令** — AI 诗词接龙，基于 DeepSeek 大语言模型
- **个人收藏** — 离线可用，左滑快速取消收藏
- **阅读偏好** — 全局字号、行间距、字体调节，Material You 动态色彩

---

## 技术栈

### Android 客户端
| 类别 | 技术 |
|------|------|
| 语言 | Kotlin |
| UI | Jetpack Compose + Material 3 |
| 导航 | Navigation 3 + 自定义返回栈 |
| 依赖注入 | Hilt |
| 网络 | Retrofit + OkHttp + kotlinx.serialization |
| 本地存储 | Room + Proto DataStore |
| 架构 | Clean Architecture + MVI |

### 后端服务
| 类别 | 技术 |
|------|------|
| 框架 | Ktor + Netty |
| 数据库 | MySQL + HikariCP |
| 迁移 | Flyway |
| 认证 | JWT + BCrypt |
| 搜索 | FULLTEXT 索引 + ngram 中文分词 |

---

## 项目结构

```
SuFeiApp/
├── app/                          # Android 客户端
│   └── src/main/java/dev/wceng/sufei/
│       ├── data/                 # 数据层（Repository/Network/Local/TTS）
│       ├── di/                   # Hilt 依赖注入模块
│       └── ui/                   # UI 层（Screen/Component/Navigation/Theme）
├── server/                       # Ktor 后端服务
│   └── src/main/kotlin/.../server/
│       ├── routes/               # API 路由
│       ├── service/              # 业务逻辑
│       ├── database/             # 连接池 + Flyway
│       └── data/                 # JSONL 数据导入
└── gradle/libs.versions.toml     # 统一依赖版本管理
```

---

## 快速开始

### 前置要求
- Android Studio Hedgehog+
- JDK 17+
- MySQL 8.0+（仅 Server 端需要）

### App 端
```bash
# 1. 设置 DeepSeek API Key（飞花令功能）
# 在 local.properties 中添加（不会被 git 追踪）：
# DEEPSEEK_API_KEY=sk-your-key-here

# 2. 用 Android Studio 打开 SuFeiApp/ 目录，直接运行
```

### Server 端
```bash
cd server

# 1. 配置数据库连接
# 复制 application.conf.example 并修改数据库密码
# 或设置环境变量：DATABASE_URL, DATABASE_USER, DATABASE_PASSWORD, JWT_SECRET

# 2. 运行
./gradlew :server:run
```

---

## 数据说明

项目内置 20 万余首诗词、1 万余名诗人，覆盖先秦至近现代。数据以 JSONL 格式分发：

- `app/src/main/assets/` — App 端数据（Room 离线模式）
- `server/src/main/resources/` — Server 端数据（MySQL 导入）

导入流程支持断点续传，首次启动自动完成。

---

## License

MIT

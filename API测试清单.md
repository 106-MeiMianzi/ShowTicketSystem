# API 测试接口清单

## 📋 测试前准备

### 1. 环境配置
- **服务器地址**：`http://localhost:8080`
- **管理员账号**：用户名 `admin`，密码 `admin123`
- **测试工具**：Apifox（或其他API测试工具）

### 2. Token 管理（重要！）
- 登录后会返回 `token`，**请务必保存**
- 在 Apifox 中设置环境变量：
  - 创建环境变量：`token`（用于用户接口）
  - 创建环境变量：`admin_token`（用于管理员接口）
- 在需要认证的接口请求头中添加：
  ```
  Authorization: Bearer {{token}}
  ```

### 3. 分类说明
- **正向**：正常业务流程，应该成功的场景
- **负向**：错误输入或异常情况，应该返回错误
- **边界值**：临界值测试（如0、负数、超长字符串等）
- **安全性**：权限验证、认证相关的测试
- **其他**：性能、兼容性等特殊场景

### 4. 标签说明
- **仅传必要字段**：只传必需的参数
- **语义合法**：参数值合法且有意义
- **覆盖枚举组合**：测试不同枚举值的组合
- **其他正向**：其他正常的正向场景
- **缺失必填字段**：缺少必需的参数
- **无效值**：参数值无效（如不存在的ID）
- **类型错误**：参数类型错误（如字符串传数字）
- **格式错误**：参数格式不正确（如邮箱格式错误）

---

## 🎯 测试顺序（按此顺序逐个测试）

### 第一阶段：用户认证（必须最先测试，获取Token）

#### ✅ 已完成：1. 用户注册/登录接口（成功案例）
**接口**：`POST /api/user/register-or-login`  
**请求方式**：`POST`  
**Content-Type**：`application/x-www-form-urlencoded`（表单提交）

**接口说明**：
- ✅ 支持用户名注册/登录
- ✅ 支持邮箱登录（如果`username`参数包含`@`符号，则识别为邮箱）
- ✅ 如果用户不存在则注册，存在则登录

**重要提示**：
- ⚠️ **参数名必须是 `username`**（不是 `account` 或其他名称）
- ⚠️ **请求方式必须是表单提交**（`application/x-www-form-urlencoded` 或 `form-data`），不是JSON
- ✅ 如果 `username` 参数包含 `@` 符号，系统会自动识别为邮箱登录

**Apifox设置步骤**：
1. 请求方式选择：`POST`
2. URL：`http://localhost:8080/api/user/register-or-login`
3. Body类型选择：`form-data` 或 `x-www-form-urlencoded`（不要选择JSON）
4. 添加参数：
   - 参数名：`username`，值：`testuser`
   - 参数名：`password`，值：`test123`
   - 参数名：`email`，值：`test@example.com`（可选）

**测试结果**：✅ 已成功
- username: `testuser`
- password: `test123`
- email: `test@example.com`（可选）

**获取到的Token**：请保存到环境变量 `token` 中

**注意**：
- 如果之前测试时创建了包含特殊字符的用户名，该用户仍可通过此接口正常登录（兼容旧数据），但新用户注册时会被拒绝。
- 邮箱登录：如果`username`参数是邮箱格式（包含`@`），则直接进行邮箱登录，不需要用户名格式验证。

---

#### 2. 用户注册/登录接口（失败案例）

**接口**：`POST /api/user/register-or-login`  
**请求方式**：`POST`  
**完整URL**：`http://localhost:8080/api/user/register-or-login`  
**Content-Type**：`application/x-www-form-urlencoded`（表单提交）

**重要提示**：
- ⚠️ **参数名必须是 `username`**（不是 `account` 或其他名称）
- ⚠️ **请求方式必须是表单提交**（`application/x-www-form-urlencoded`），不是JSON
- ✅ 如果 `username` 参数包含 `@` 符号，系统会自动识别为邮箱登录
- ✅ 如果 `username` 参数不包含 `@` 符号，系统会识别为用户名登录/注册

**请求参数说明**：
- `username`（必填）：用户名或邮箱（如果包含@则为邮箱）
- `password`（必填）：密码
- `email`（可选）：邮箱（仅在注册新用户时使用）

**用户名验证规则说明**：
- ✅ **长度限制**：3-20个字符
- ✅ **允许字符**：字母（a-z, A-Z）、数字（0-9）、下划线（_）、中划线（-）
- ✅ **首字符要求**：必须以字母或数字开头，不能是下划线或中划线
- ⚠️ **兼容性**：已有用户（即使格式不符合规范）仍可通过此接口登录（兼容旧数据）

| 序号 | 用例描述 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|---------|---------|---------|---------|
| 2.1 | 用户名为空 | `username: ""`<br>`password: test123` | 负向 | 缺失必填字段、无效值 | 返回200，message包含"用户名或密码不能为空" | ⬜ |
| 2.2 | 密码为空 | `username: newuser`<br>`password: ""` | 负向 | 缺失必填字段、无效值 | 返回200，message包含"用户名或密码不能为空" | ⬜ |
| 2.3 | 用户名和密码都为空 | `username: ""`<br>`password: ""` | 负向 | 缺失必填字段、无效值 | 返回200，message包含"用户名或密码不能为空" | ⬜ |
| 2.4 | 注册失败（邮箱已被使用） | `username: anotheruser`<br>`password: test123`<br>`email: test@example.com` | 负向 | 无效值、语义合法 | 返回200，message包含"操作失败：邮箱已被使用。" | ⬜ |
| 2.5 | 用户名包含特殊字符（@） | `username: test@user`<br>`password: test123` | 边界值 | 格式错误 | **新注册**：返回200，message包含"注册失败：用户名只能包含字母、数字、下划线和中划线"<br>**已有用户登录**：如果该用户名已存在且密码正确，返回200，包含token | ⬜ |
| 2.5.1 | 用户名包含特殊字符（#） | `username: test#user`<br>`password: test123` | 边界值 | 格式错误 | **新注册**：返回200，message包含"注册失败：用户名只能包含字母、数字、下划线和中划线" | ⬜ |
| 2.5.2 | 用户名包含空格 | `username: test user`<br>`password: test123` | 边界值 | 格式错误 | **新注册**：返回200，message包含"注册失败：用户名只能包含字母、数字、下划线和中划线" | ⬜ |
| 2.6 | 用户名长度过短（少于3字符） | `username: ab`<br>`password: test123` | 边界值 | 格式错误、无效值 | **新注册**：返回200，message包含"注册失败：用户名长度不能少于3个字符" | ⬜ |
| 2.7 | 用户名超长（超过20字符） | `username: verylongusernamethatexceeds20chars`<br>`password: test123` | 边界值 | 格式错误 | **新注册**：返回200，message包含"注册失败：用户名长度不能超过20个字符" | ⬜ |
| 2.8 | 用户名以下划线开头 | `username: _testuser`<br>`password: test123` | 边界值 | 格式错误 | **新注册**：返回200，message包含"注册失败：用户名必须以字母或数字开头" | ⬜ |
| 2.9 | 用户名以中划线开头 | `username: -testuser`<br>`password: test123` | 边界值 | 格式错误 | **新注册**：返回200，message包含"注册失败：用户名必须以字母或数字开头" | ⬜ |
| 2.10 | 用户名格式正确（包含下划线） | `username: test_user`<br>`password: test123` | 正向 | 语义合法 | 返回200，包含token，注册成功 | ⬜ |
| 2.11 | 用户名格式正确（包含中划线） | `username: test-user`<br>`password: test123` | 正向 | 语义合法 | 返回200，包含token，注册成功 | ⬜ |
| 2.12 | 用户名格式正确（纯字母） | `username: testuser123`<br>`password: test123` | 正向 | 语义合法 | 返回200，包含token，注册成功 | ⬜ |
| 2.13 | 邮箱登录成功 | `username: test@example.com`<br>`password: test123` | 正向 | 其他正向、语义合法 | 返回200，包含token，登录成功 | ⬜ |
| 2.14 | 邮箱登录失败（邮箱不存在） | `username: notexist@example.com`<br>`password: test123` | 负向 | 无效值 | 返回200，message包含"邮箱或密码错误" | ⬜ |
| 2.15 | 邮箱登录失败（密码错误） | `username: test@example.com`<br>`password: wrongpass` | 负向 | 无效值 | 返回200，message包含"邮箱或密码错误" | ⬜ |
| 2.16 | 用户名登录成功（已有用户） | `username: testuser`<br>`password: test123` | 正向 | 仅传必要字段、语义合法 | 返回200，包含token，登录成功 | ⬜ |
| 2.17 | 注册失败（邮箱已被使用） | `username: newuser`<br>`password: test123`<br>`email: test@example.com` | 负向 | 无效值、语义合法 | 返回200，message包含"操作失败：邮箱已被使用。" | ⬜ |
| 2.18 | 用户名登录失败（密码错误） | `username: testuser`<br>`password: wrongpass` | 负向 | 无效值 | 返回200，message包含"操作失败：用户名或密码错误。" | ⬜ |

**重要说明**：
- ⚠️ **此接口设计为"不存在则注册"**：如果使用不存在的用户名且格式正确，系统会自动注册新用户
- ✅ 用例2.17测试的是：用户名格式正确，但提供的邮箱已被其他用户使用，应该返回"邮箱已被使用"的错误
- ✅ 用例2.18测试的是：用户名存在但密码错误，应该返回"用户名或密码错误"
- ⚠️ **无法测试"账号不存在"的场景**：因为接口设计就是"不存在则注册"，这是接口的核心功能

**测试提示**：
- 2.13 和 2.16 成功后，更新环境变量 `token`（如果token更安全）
- 后续需要认证的接口都会使用这个token

---

#### 3. 获取当前用户信息

**接口**：`GET /api/user/current`  
**请求方式**：`GET`  
**完整URL**：`http://localhost:8080/api/user/current`  
**需要Token**：✅ 是

| 序号 | 用例描述 | 请求头 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|-------|---------|---------|---------|---------|
| 4.1 | 已登录用户获取信息 | `Authorization: Bearer {{token}}` | 正向 | 语义合法、其他正向 | 返回200，包含用户信息（id、username、email等） | ⬜ |
| 4.2 | 未携带Token | 无Authorization头 | 安全性 | 缺失必填字段 | 返回401，`{"error":"未授权：请先登录"}` | ⬜ |
| 4.3 | Token无效 | `Authorization: Bearer invalid_token` | 安全性 | 无效值 | 返回401，`{"error":"未授权：请先登录"}` | ⬜ |
| 4.4 | Token格式错误 | `Authorization: token_without_bearer` | 安全性 | 格式错误 | 返回401，`{"error":"未授权：请先登录"}` | ⬜ |

**测试提示**：
- 4.1 使用从登录接口获取的token
- 4.2-4.4 测试Token验证的安全性

---

#### 4. 修改个人信息

**接口**：`PUT /api/user/update`  
**请求方式**：`PUT`  
**完整URL**：`http://localhost:8080/api/user/update`  
**需要Token**：✅ 是  
**Content-Type**：`application/json`

| 序号 | 用例描述 | 请求头 | 请求体（JSON） | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|-------|---------------|---------|---------|---------|---------|
| 5.1 | 更新邮箱 | `Authorization: Bearer {{token}}` | `{"email": "newemail@example.com"}` | 正向 | 语义合法、其他正向 | 返回200，`{"message": "个人信息更新成功！", "success": true}` | ⬜ |
| 5.2 | 更新手机号 | `Authorization: Bearer {{token}}` | `{"phone": "13800138000"}` | 正向 | 语义合法、其他正向 | 返回200，`{"message": "个人信息更新成功！", "success": true}` | ⬜ |
| 5.3 | 更新真实姓名 | `Authorization: Bearer {{token}}` | `{"realName": "张三"}` | 正向 | 语义合法、其他正向 | 返回200，`{"message": "个人信息更新成功！", "success": true}` | ⬜ |
| 5.4 | 同时更新多个字段 | `Authorization: Bearer {{token}}` | `{"email": "new@example.com", "phone": "13800138000", "realName": "李四"}` | 正向 | 覆盖枚举组合、语义合法 | 返回200，`{"message": "个人信息更新成功！", "success": true}` | ⬜ |
| 5.5 | 未登录 | 无Authorization头 | `{"email": "test@example.com"}` | 安全性 | 缺失必填字段 | 返回401或`"更新失败：请先登录。"` | ⬜ |
| 5.6 | 邮箱格式错误 | `Authorization: Bearer {{token}}` | `{"email": "invalid-email"}` | 负向 | 格式错误 | 返回200，`{"message": "更新失败：邮箱格式不正确。", "success": false}` | ⬜ |
| 5.7 | 手机号格式错误 | `Authorization: Bearer {{token}}` | `{"phone": "123"}` | 负向 | 格式错误、无效值 | 返回200，`{"message": "更新失败：手机号格式不正确。手机号应为11位数字，以1开头。", "success": false}` | ⬜ |

**测试提示**：
- 5.1-5.4 测试正常更新流程
- 5.5 测试未登录情况
- 5.6-5.7 测试参数格式验证

---

### 第二阶段：收货地址管理

#### 5. 获取收货地址列表

**接口**：`GET /api/user/addresses`  
**请求方式**：`GET`  
**完整URL**：`http://localhost:8080/api/user/addresses`  
**需要Token**：✅ 是

| 序号 | 用例描述 | 请求头 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|-------|---------|---------|---------|---------|
| 6.1 | 有地址的用户获取列表 | `Authorization: Bearer {{token}}` | 正向 | 语义合法 | 返回200，地址列表数组 | ⬜ |
| 6.2 | 无地址的用户获取列表 | `Authorization: Bearer {{token}}` | 正向 | 语义合法、其他正向 | 返回200，空数组 `[]` | ⬜ |
| 6.3 | 未登录 | 无Authorization头 | 安全性 | 缺失必填字段 | 返回401 | ⬜ |

**测试提示**：
- **用例6.2（无地址的用户）**：
  - 使用新注册的用户（还没有添加过地址）
  - 或者确保当前用户没有任何地址
  - 预期返回空数组 `[]`
- **用例6.1（有地址的用户）**：
  - **先执行用例7.1（添加收货地址）**，添加至少一个地址
  - 然后再测试用例6.1，应该返回包含地址的数组
  - 或者使用之前已经添加过地址的用户
- **用例6.3**：测试权限验证（未登录情况）

---

#### 6. 添加收货地址

**接口**：`POST /api/user/address`  
**请求方式**：`POST`  
**完整URL**：`http://localhost:8080/api/user/address`  
**需要Token**：✅ 是  
**Content-Type**：`application/json`

| 序号 | 用例描述 | 请求头 | 请求体（JSON） | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|-------|---------------|---------|---------|---------|---------|
| 7.1 | 添加地址（仅必要字段） | `Authorization: Bearer {{token}}` | `{"receiverName": "张三", "receiverPhone": "13800138000", "province": "北京市", "city": "北京市", "district": "朝阳区", "detailAddress": "某某街道123号"}` | 正向 | 仅传必要字段、语义合法 | 返回200，`{"message": "收货地址添加成功！", "success": true}` | ⬜ |
| 7.2 | 添加默认地址 | `Authorization: Bearer {{token}}` | `{"receiverName": "李四", "receiverPhone": "13900139000", "province": "上海市", "city": "上海市", "district": "黄浦区", "detailAddress": "新地址456号", "isDefault": 1}` | 正向 | 覆盖枚举组合、语义合法 | 返回200，`{"message": "收货地址添加成功！", "success": true}` | ⬜ |
| 7.3 | 添加非默认地址 | `Authorization: Bearer {{token}}` | `{"receiverName": "王五", "receiverPhone": "13700137000", "province": "广州市", "city": "广州市", "district": "天河区", "detailAddress": "地址789号", "isDefault": 0}` | 正向 | 覆盖枚举组合、语义合法 | 返回200，`{"message": "收货地址添加成功！", "success": true}` | ⬜ |
| 7.4 | 未登录 | 无Authorization头 | `{"receiverName": "张三", ...}` | 安全性 | 缺失必填字段 | 返回401或`{"message": "添加失败：请先登录。", "success": false}` | ⬜ |
| 7.5 | 收货人姓名为空 | `Authorization: Bearer {{token}}` | `{"receiverName": "", "receiverPhone": "13800138000", ...}` | 负向 | 缺失必填字段 | 返回200，`{"message": "添加失败！", "success": false}` | ⬜ |
| 7.6 | 收货人电话为空 | `Authorization: Bearer {{token}}` | `{"receiverName": "张三", "receiverPhone": "", ...}` | 负向 | 缺失必填字段 | 返回200，`{"message": "添加失败！", "success": false}` | ⬜ |
| 7.7 | 电话号码格式错误 | `Authorization: Bearer {{token}}` | `{"receiverPhone": "123", ...}` | 负向 | 格式错误、无效值 | 返回200，`{"message": "添加失败！", "success": false}`（手机号格式验证：应为11位数字，以1开头，第二位是3-9） | ⬜ |

**测试提示**：
- 7.1-7.3 先添加几个地址，用于后续测试
- 7.4-7.7 测试错误情况和验证规则
- **手机号格式验证规则（7.7）**：
  - 手机号格式要求：11位数字，以1开头，第二位是3-9（中国手机号格式）
  - 如果格式不正确（如 `123`），系统会返回错误提示："添加失败！"

---

#### 7. 获取收货地址详情

**接口**：`GET /api/user/address/{id}`  
**请求方式**：`GET`  
**完整URL**：`http://localhost:8080/api/user/address/1`（id为地址ID）  
**需要Token**：✅ 是

| 序号 | 用例描述 | 路径参数 | 请求头 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|-------|---------|---------|---------|---------|
| 8.1 | 查询自己的地址详情 | `id: 1`（存在且属于自己的地址ID） | `Authorization: Bearer {{token}}` | 正向 | 语义合法 | 返回200，`{"message": "查询成功", "success": true, "data": {地址详情对象}}` | ⬜ |
| 8.2 | 地址不存在 | `id: 999999` | `Authorization: Bearer {{token}}` | 负向 | 无效值 | 返回200，`{"message": "查询失败：地址不存在或不属于您。", "success": false, "data": null}` | ⬜ |
| 8.3 | 地址不属于当前用户 | `id: 其他用户的地址ID` | `Authorization: Bearer {{token}}` | 安全性 | 无效值 | 返回200，`{"message": "查询失败：地址不存在或不属于您。", "success": false, "data": null}` | ⬜ |
| 8.4 | 未登录 | `id: 1` | 无Authorization头 | 安全性 | 缺失必填字段 | 返回401或`{"message": "查询失败：请先登录。", "success": false, "data": null}` | ⬜ |
| 8.5 | 路径参数为0 | `id: 0` | `Authorization: Bearer {{token}}` | 边界值 | 无效值 | 返回200，`{"message": "查询失败：地址不存在或不属于您。", "success": false, "data": null}` | ⬜ |
| 8.6 | 路径参数为负数 | `id: -1` | `Authorization: Bearer {{token}}` | 边界值 | 无效值 | 返回200，`{"message": "查询失败：地址不存在或不属于您。", "success": false, "data": null}` | ⬜ |

**测试提示**：
- 8.1 使用从接口7添加的地址ID
- 8.3 需要另一个用户的数据，可以暂时跳过

---

#### 8. 修改收货地址

**接口**：`PUT /api/user/address/{id}`  
**请求方式**：`PUT`  
**完整URL**：`http://localhost:8080/api/user/address/1`（id为地址ID）  
**需要Token**：✅ 是  
**Content-Type**：`application/json`

| 序号 | 用例描述 | 路径参数 | 请求头 | 请求体（JSON） | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|-------|---------------|---------|---------|---------|---------|
| 9.1 | 修改自己的地址 | `id: 1` | `Authorization: Bearer {{token}}` | `{"receiverName": "李四", "receiverPhone": "13900139000", "province": "上海市", "city": "上海市", "district": "黄浦区", "detailAddress": "新地址456号", "isDefault": 0}` | 正向 | 语义合法 | 返回200，`{"message": "收货地址更新成功！", "success": true}` | ⬜ |
| 9.2 | 地址不存在 | `id: 999999` | `Authorization: Bearer {{token}}` | `{...}` | 负向 | 无效值 | 返回200，`{"message": "更新失败！", "success": false}` | ⬜ |
| 9.3 | 地址不属于当前用户 | `id: 其他用户的地址ID` | `Authorization: Bearer {{token}}` | `{...}` | 安全性 | 无效值 | 返回200，`{"message": "更新失败！", "success": false}` | ⬜ |
| 9.4 | 未登录 | `id: 1` | 无Authorization头 | `{...}` | 安全性 | 缺失必填字段 | 返回401 | ⬜ |

**测试提示**：
- 9.1 修改刚才添加的地址
- 9.2-9.4 测试错误处理

---

#### 9. 删除收货地址

**接口**：`DELETE /api/user/address/{id}`  
**请求方式**：`DELETE`  
**完整URL**：`http://localhost:8080/api/user/address/1`（id为地址ID）  
**需要Token**：✅ 是

| 序号 | 用例描述 | 路径参数 | 请求头 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|-------|---------|---------|---------|---------|
| 10.1 | 删除自己的地址 | `id: 1`（存在的地址ID） | `Authorization: Bearer {{token}}` | 正向 | 语义合法 | 返回200，`{"message": "收货地址删除成功！", "success": true}` | ⬜ |
| 10.2 | 地址不存在 | `id: 999999` | `Authorization: Bearer {{token}}` | 负向 | 无效值 | 返回200，`{"message": "删除失败！", "success": false}` | ⬜ |
| 10.3 | 地址不属于当前用户 | `id: 其他用户的地址ID` | `Authorization: Bearer {{token}}` | 安全性 | 无效值 | 返回200，`{"message": "删除失败！", "success": false}` | ⬜ |
| 10.4 | 未登录 | `id: 1` | 无Authorization头 | 安全性 | 缺失必填字段 | 返回401 | ⬜ |

**测试提示**：
- 10.1 删除测试用的地址，注意保留至少一个地址用于后续订单测试

---

#### 10. 退出登录

**接口**：`POST /api/user/logout`  
**请求方式**：`POST`  
**完整URL**：`http://localhost:8080/api/user/logout`

| 序号 | 用例描述 | 请求头 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|-------|---------|---------|---------|---------|
| 11.1 | 退出登录 | `Authorization: Bearer {{token}}` | 正向 | 语义合法 | 返回200，`{"message": "退出登录成功！请客户端删除本地存储的token。", "success": true}` | ⬜ |

---

### 第三阶段：演出查询（公开接口，无需Token）

#### 11. 首页演出列表

**接口**：`GET /api/show/home`  
**请求方式**：`GET`  
**完整URL**：`http://localhost:8080/api/show/home`

| 序号 | 用例描述 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|---------|---------|---------|---------|
| 12.1 | 获取默认演出列表 | 无参数 | 正向 | 仅传必要字段、语义合法 | 返回200，演出列表 | ⬜ |
| 12.2 | 按地区筛选 | `region: 北京` | 正向 | 语义合法、其他正向 | 返回200，北京地区演出列表 | ⬜ |
| 12.3 | 按分类筛选 | `category: 演唱会` | 正向 | 语义合法、其他正向 | 返回200，演唱会分类列表 | ⬜ |
| 12.4 | 地区+分类组合 | `region: 北京`<br>`category: 演唱会` | 正向 | 覆盖枚举组合、语义合法 | 返回200，符合条件的列表 | ⬜ |
| 12.5 | 限制数量 | `limit: 10` | 正向 | 语义合法、其他正向 | 返回200，最多10条记录 | ⬜ |
| 12.6 | limit为0 | `limit: 0` | 边界值 | 无效值 | 返回200，自动重置为默认值20，返回最多20条记录 | ⬜ |
| 12.7 | limit为负数 | `limit: -1` | 边界值 | 无效值 | 返回200，自动重置为默认值20，返回最多20条记录 | ⬜ |

**测试提示**：
- 这些是公开接口，不需要Token
- 如果数据库中没有演出数据，可以先跳过，或先用管理员接口添加演出
- **limit参数业务规则**：
  - 如果 `limit` 为 `null`、`0` 或负数（≤0），系统会自动重置为默认值 `20`
  - 因此用例 12.6（limit=0）和 12.7（limit=-1）的实际行为是返回最多20条记录
  - 如果 `limit` 为正数，则按指定数量返回

---

#### 12. 搜索演出

**接口**：`GET /api/show/search`  
**请求方式**：`GET`  
**完整URL**：`http://localhost:8080/api/show/search?keyword=周杰伦`

| 序号 | 用例描述 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|---------|---------|---------|---------|
| 13.1 | 搜索存在的演出 | `keyword: 周杰伦` | 正向 | 仅传必要字段、语义合法 | 返回200，匹配的演出列表 | ⬜ |
| 13.2 | 搜索不存在的演出 | `keyword: 不存在的演出` | 正向 | 其他正向、语义合法 | 返回200，空数组 `[]` | ⬜ |
| 13.3 | 关键词为空 | `keyword: ""` | 负向 | 无效值 | 返回200，空数组 `[]`（Service层会检查空字符串并返回空列表） | ⬜ |
| 13.4 | 关键词包含特殊字符 | `keyword: @#$%` | 边界值 | 格式错误 | 返回200，空数组 `[]`（SQL会执行LIKE查询，`%`作为通配符，但由于一般不会有匹配结果，所以返回空数组，这是正常业务行为） | ⬜ |
| 13.5 | 关键词超长 | `keyword: 很长的字符串...`（超过100字符） | 边界值 | 格式错误 | 返回200，空数组 `[]`（代码没有长度限制，SQL会正常执行，但由于一般不会有匹配结果，所以返回空数组，这是正常业务行为） | ⬜ |

**测试提示**：
- **关键词业务逻辑说明**：
  - **空关键词（13.3）**：Service层会检查 `keyword` 是否为 `null` 或空字符串，如果是则直接返回空列表 `[]`
  - **特殊字符（13.4）**：代码使用 `LIKE CONCAT('%', #{keyword}, '%')` 进行模糊查询，特殊字符（如 `@#$%` 中的 `%`）会被当作 SQL 通配符。查询会正常执行，但一般不会有匹配结果，返回空数组是正常的业务行为
  - **超长字符串（13.5）**：代码中没有对关键词长度进行限制，SQL 会正常执行查询。但由于演出名称或场馆名一般不会有这么长的字符串，所以不会匹配到结果，返回空数组是正常的业务行为
  - **注意**：返回空数组 `[]` 不代表错误，而是表示"搜索不到匹配结果"，这是正常的业务行为

---

#### 13. 条件查询演出（分页）

**接口**：`GET /api/show/query`  
**请求方式**：`GET`  
**完整URL**：`http://localhost:8080/api/show/query?region=北京&category=演唱会&pageNum=1&pageSize=10`

| 序号 | 用例描述 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|---------|---------|---------|---------|
| 14.1 | 分页查询（默认值） | `pageNum: 1`<br>`pageSize: 10` | 正向 | 仅传必要字段、语义合法 | 返回200，分页结果 | ⬜ |
| 14.2 | 条件筛选 | `region: 北京`<br>`category: 演唱会` | 正向 | 覆盖枚举组合、语义合法 | 返回200，符合条件的列表 | ⬜ |
| 14.3 | 页码为0 | `pageNum: 0`<br>`pageSize: 10` | 边界值 | 无效值 | 返回200，`pageNum`被自动重置为`1`，返回第一页数据 | ⬜ |
| 14.4 | 页码为负数 | `pageNum: -1`<br>`pageSize: 10` | 边界值 | 无效值 | 返回200，`pageNum`被自动重置为`1`，返回第一页数据 | ⬜ |
| 14.5 | 页大小为0 | `pageNum: 1`<br>`pageSize: 0` | 边界值 | 无效值 | 返回200，`pageSize`被自动重置为`10`，返回最多10条记录 | ⬜ |
| 14.6 | 页大小为负数 | `pageNum: 1`<br>`pageSize: -1` | 边界值 | 无效值 | 返回200，`pageSize`被自动重置为`10`，返回最多10条记录 | ⬜ |
| 14.7 | 页大小过大 | `pageNum: 1`<br>`pageSize: 10000` | 边界值 | 无效值 | 返回200，`pageSize`被自动重置为最大值`100`，返回最多100条记录（防止恶意请求） | ⬜ |

**测试提示**：
- **分页参数业务逻辑说明**：
  - **页码边界值（14.3-14.4）**：当 `pageNum <= 0`（包括0和负数）时，系统会自动重置为 `1`，返回第一页数据。返回结果中的 `pageNum` 字段会显示为 `1`
  - **页大小边界值（14.5-14.6）**：当 `pageSize <= 0`（包括0和负数）时，系统会自动重置为默认值 `10`，返回最多10条记录
  - **页大小上限（14.7）**：当 `pageSize > 100` 时，系统会自动重置为最大值 `100`，返回最多100条记录。这是为了防止恶意请求导致数据库压力过大，保护系统性能
  - **所有分页接口**：上述边界值处理逻辑适用于所有分页接口（包括用户端和管理端）

---

#### 14. 获取演出详情

**接口**：`GET /api/show/details`  
**请求方式**：`GET`  
**完整URL**：`http://localhost:8080/api/show/details?showId=1`

| 序号 | 用例描述 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|---------|---------|---------|---------|
| 15.1 | 演出存在 | `showId: 1`（存在的演出ID） | 正向 | 仅传必要字段、语义合法 | 返回200，`{"show": {演出详情JSON对象}}` | ⬜ |
| 15.2 | 演出不存在 | `showId: 999999` | 负向 | 无效值 | 返回200，`{"show": null}`（确保始终返回有效的JSON对象） | ⬜ |
| 15.3 | showId为0 | `showId: 0` | 边界值 | 无效值 | 返回200，`{"show": null}`（确保始终返回有效的JSON对象） | ⬜ |
| 15.4 | showId为负数 | `showId: -1` | 边界值 | 无效值 | 返回200，`{"show": null}`（确保始终返回有效的JSON对象） | ⬜ |

**测试提示**：
- 需要先有演出数据，可以先看管理员接口部分添加演出
- **返回格式说明**：
  - 用例 15.1（演出存在）：返回 200，响应体格式为 `{"show": {演出详情JSON对象}}`
  - 用例 15.2-15.4（演出不存在或ID无效）：返回 200，响应体格式为 `{"show": null}`，确保始终返回有效的 JSON 对象，不会出现空响应体
  - **返回格式统一**：使用包装对象确保 Apifox 的 JSON 格式验证能够通过，解决空响应体的问题

---

### 第四阶段：订单管理（需要用户Token）

**重要**：测试订单前，确保：
1. 已有登录token（环境变量 `token`）
2. 数据库中已有演出数据（可以先用管理员接口添加）

---

#### 15. 创建订单（抢票）

**接口**：`POST /api/order/create`  
**请求方式**：`POST`  
**完整URL**：`http://localhost:8080/api/order/create`  
**需要Token**：✅ 是

| 序号 | 用例描述 | 请求头 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|-------|---------|---------|---------|---------|---------|
| 16.1 | 库存充足创建订单 | `Authorization: Bearer {{token}}` | `showId: 1`<br>`quantity: 2` | 正向 | 仅传必要字段、语义合法 | 返回200，`{"order": {订单对象}}`（status=1待支付） | ⬜ |
| 16.2 | 创建单张票订单 | `Authorization: Bearer {{token}}` | `showId: 1`<br>`quantity: 1` | 正向 | 语义合法、其他正向 | 返回200，`{"order": {订单对象}}` | ⬜ |
| 16.3 | 未登录 | 无Authorization头 | `showId: 1`<br>`quantity: 2` | 安全性 | 缺失必填字段 | 返回401或`{"order": null}` | ⬜ |
| 16.4 | 演出不存在 | `Authorization: Bearer {{token}}` | `showId: 999999`<br>`quantity: 2` | 负向 | 无效值 | 返回200，`{"order": null}`（确保始终返回有效的JSON对象） | ⬜ |
| 16.5 | 库存不足 | `Authorization: Bearer {{token}}` | `showId: 1`<br>`quantity: 10000` | 负向 | 无效值 | 返回200，`{"order": null}`（确保始终返回有效的JSON对象） | ⬜ |
| 16.6 | 数量为0 | `Authorization: Bearer {{token}}` | `showId: 1`<br>`quantity: 0` | 边界值 | 无效值 | 返回200，`{"order": null}`（确保始终返回有效的JSON对象） | ⬜ |
| 16.7 | 数量为负数 | `Authorization: Bearer {{token}}` | `showId: 1`<br>`quantity: -1` | 边界值 | 无效值 | 返回200，`{"order": null}`（确保始终返回有效的JSON对象） | ⬜ |
| 16.8 | showId为0 | `Authorization: Bearer {{token}}` | `showId: 0`<br>`quantity: 2` | 边界值 | 无效值 | 返回200，`{"order": null}`（确保始终返回有效的JSON对象） | ⬜ |

**测试提示**：
- 16.1 成功后，**记住订单ID**，用于后续测试
- 16.2-16.8 测试各种异常情况

---

#### 16. 查询订单详情

**接口**：`GET /api/order/details`  
**请求方式**：`GET`  
**完整URL**：`http://localhost:8080/api/order/details?orderId=1`  
**需要Token**：✅ 是

| 序号 | 用例描述 | 请求头 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|-------|---------|---------|---------|---------|---------|
| 17.1 | 查询自己的订单 | `Authorization: Bearer {{token}}` | `orderId: 1`（从16.1获取的订单ID） | 正向 | 仅传必要字段、语义合法 | 返回200，`{"order": {订单详情JSON对象}}` | ⬜ |
| 17.2 | 订单不存在 | `Authorization: Bearer {{token}}` | `orderId: 999999` | 负向 | 无效值 | 返回200，`{"order": null}`（确保始终返回有效的JSON对象） | ⬜ |
| 17.3 | 订单不属于当前用户 | `Authorization: Bearer {{token}}` | `orderId: 其他用户的订单ID` | 安全性 | 无效值 | 返回200，`{"order": null}`（确保始终返回有效的JSON对象） | ⬜ |
| 17.4 | 未登录 | 无Authorization头 | `orderId: 1` | 安全性 | 缺失必填字段 | 返回401或`{"order": null}` | ⬜ |
| 17.5 | orderId为0 | `Authorization: Bearer {{token}}` | `orderId: 0` | 边界值 | 无效值 | 返回200，`{"order": null}`（确保始终返回有效的JSON对象） | ⬜ |

---

#### 17. 取消订单

**接口**：`PUT /api/order/cancel`  
**请求方式**：`PUT`  
**完整URL**：`http://localhost:8080/api/order/cancel?orderId=1`  
**需要Token**：✅ 是

| 序号 | 用例描述 | 请求头 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|-------|---------|---------|---------|---------|---------|
| 18.1 | 取消待支付订单 | `Authorization: Bearer {{token}}` | `orderId: 1`（status=1的订单） | 正向 | 语义合法 | 返回200，`{"message": "订单取消成功。库存已返还。", "success": true}` | ⬜ |
| 18.2 | 订单不存在 | `Authorization: Bearer {{token}}` | `orderId: 999999` | 负向 | 无效值 | 返回200，`{"message": "取消失败！订单不存在、不属于您、或状态不可取消。", "success": false}` | ⬜ |
| 18.3 | 订单不属于当前用户 | `Authorization: Bearer {{token}}` | `orderId: 其他用户的订单ID` | 安全性 | 无效值 | 返回200，`{"message": "取消失败！订单不存在、不属于您、或状态不可取消。", "success": false}` | ⬜ |
| 18.4 | 订单状态不可取消（已支付） | `Authorization: Bearer {{token}}` | `orderId: X`（status=2已支付的订单） | 负向 | 无效值 | 返回200，`{"message": "取消失败！订单不存在、不属于您、或状态不可取消。", "success": false}` | ⬜ |
| 18.5 | 未登录 | 无Authorization头 | `orderId: 1` | 安全性 | 缺失必填字段 | 返回401或`{"message": "取消失败：请先登录。", "success": false}` | ⬜ |

**测试提示**：
- 18.1 先创建一个待支付订单（status=1），再取消
- 18.4 需要先支付一个订单，然后再尝试取消

---

#### 18. 支付订单

**接口**：`PUT /api/order/pay`  
**请求方式**：`PUT`  
**完整URL**：`http://localhost:8080/api/order/pay?orderId=1`  
**需要Token**：✅ 是

| 序号 | 用例描述 | 请求头 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|-------|---------|---------|---------|---------|---------|
| 19.1 | 支付待支付订单 | `Authorization: Bearer {{token}}` | `orderId: 1`（status=1的订单） | 正向 | 语义合法 | 返回200，`{"message": "订单支付成功！", "success": true}` | ⬜ |
| 19.2 | 订单不存在 | `Authorization: Bearer {{token}}` | `orderId: 999999` | 负向 | 无效值 | 返回200，`{"message": "支付失败！订单不存在、不属于您、或状态不可支付。", "success": false}` | ⬜ |
| 19.3 | 订单不属于当前用户 | `Authorization: Bearer {{token}}` | `orderId: 其他用户的订单ID` | 安全性 | 无效值 | 返回200，`{"message": "支付失败！订单不存在、不属于您、或状态不可支付。", "success": false}` | ⬜ |
| 19.4 | 订单状态不可支付（已支付） | `Authorization: Bearer {{token}}` | `orderId: X`（status=2的订单） | 负向 | 无效值 | 返回200，`{"message": "支付失败！订单不存在、不属于您、或状态不可支付。", "success": false}` | ⬜ |
| 19.5 | 订单状态不可支付（已取消） | `Authorization: Bearer {{token}}` | `orderId: X`（status=3的订单） | 负向 | 无效值 | 返回200，`{"message": "支付失败！订单不存在、不属于您、或状态不可支付。", "success": false}` | ⬜ |
| 19.6 | 未登录 | 无Authorization头 | `orderId: 1` | 安全性 | 缺失必填字段 | 返回401或`{"message": "支付失败：请先登录。", "success": false}` | ⬜ |

**测试提示**：
- 19.1 先创建待支付订单，再支付
- 19.4-19.5 需要不同状态的订单

---

#### 19. 分页查询订单列表

**接口**：`GET /api/order/list`  
**请求方式**：`GET`  
**完整URL**：`http://localhost:8080/api/order/list?pageNum=1&pageSize=10`  
**需要Token**：✅ 是

| 序号 | 用例描述 | 请求头 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|-------|---------|---------|---------|---------|---------|
| 20.1 | 有订单的用户 | `Authorization: Bearer {{token}}` | `pageNum: 1`<br>`pageSize: 10` | 正向 | 仅传必要字段、语义合法 | 返回200，分页结果（包含当前用户的订单） | ⬜ |
| 20.2 | 无订单的用户 | `Authorization: Bearer {{token}}` | `pageNum: 1`<br>`pageSize: 10` | 正向 | 其他正向、语义合法 | 返回200，分页结果（records为空数组） | ⬜ |
| 20.3 | 未登录 | 无Authorization头 | `pageNum: 1`<br>`pageSize: 10` | 安全性 | 缺失必填字段 | 返回401或null | ⬜ |
| 20.4 | 页码为0 | `Authorization: Bearer {{token}}` | `pageNum: 0`<br>`pageSize: 10` | 边界值 | 无效值 | 根据实现返回结果 | ⬜ |

---

#### 20. 条件查询订单列表

**接口**：`GET /api/order/query`  
**请求方式**：`GET`  
**完整URL**：`http://localhost:8080/api/order/query?status=1&pageNum=1&pageSize=10`  
**需要Token**：✅ 是

| 序号 | 用例描述 | 请求头 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|-------|---------|---------|---------|---------|---------|
| 21.1 | 按状态筛选（待支付） | `Authorization: Bearer {{token}}` | `status: 1`<br>`pageNum: 1`<br>`pageSize: 10` | 正向 | 覆盖枚举组合、语义合法 | 返回200，符合条件的订单列表 | ⬜ |
| 21.2 | 按状态筛选（已支付） | `Authorization: Bearer {{token}}` | `status: 2`<br>`pageNum: 1`<br>`pageSize: 10` | 正向 | 覆盖枚举组合、语义合法 | 返回200，符合条件的订单列表 | ⬜ |
| 21.3 | 查询所有订单 | `Authorization: Bearer {{token}}` | `pageNum: 1`<br>`pageSize: 10`（无status参数） | 正向 | 仅传必要字段、语义合法 | 返回200，所有订单列表 | ⬜ |
| 21.4 | 未登录 | 无Authorization头 | `status: 1`<br>`pageNum: 1`<br>`pageSize: 10` | 安全性 | 缺失必填字段 | 返回401或null | ⬜ |
| 21.5 | 无效状态值 | `Authorization: Bearer {{token}}` | `status: 99`<br>`pageNum: 1`<br>`pageSize: 10` | 边界值 | 无效值 | 返回200，空数组 `[]`（查询条件中的status无效值会被忽略，返回空结果，这是正常的业务行为） | ⬜ |

---

### 第五阶段：支付模块（需要用户Token）

#### 21. 创建支付订单

**接口**：`POST /api/payment/create`  
**请求方式**：`POST`  
**完整URL**：`http://localhost:8080/api/payment/create`  
**需要Token**：✅ 是

| 序号 | 用例描述 | 请求头 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|-------|---------|---------|---------|---------|---------|
| 22.1 | 创建支付订单 | `Authorization: Bearer {{token}}` | `orderId: 1`（待支付的订单） | 正向 | 仅传必要字段、语义合法 | 返回200，包含支付信息 | ⬜ |
| 22.2 | 未登录 | 无Authorization头 | `orderId: 1` | 安全性 | 缺失必填字段 | 返回401或`{"error":"请先登录"}` | ⬜ |
| 22.3 | 订单不存在 | `Authorization: Bearer {{token}}` | `orderId: 999999` | 负向 | 无效值 | 根据实现返回错误信息 | ⬜ |
| 22.4 | 订单不属于当前用户 | `Authorization: Bearer {{token}}` | `orderId: 其他用户的订单ID` | 安全性 | 无效值 | 根据实现返回错误信息 | ⬜ |

---

#### 22. 支付回调接口

**接口**：`POST /api/payment/notify`  
**请求方式**：`POST`  
**完整URL**：`http://localhost:8080/api/payment/notify`

**说明**：此接口由支付宝服务器调用，手动测试可简化

| 序号 | 用例描述 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|---------|---------|---------|---------|
| 23.1 | 支付成功回调 | 有效的回调参数 | 正向 | 语义合法 | 返回200，`"success"` | ⬜ |
| 23.2 | 参数错误 | 无效的回调参数 | 负向 | 无效值 | 返回200，`"failure"` | ⬜ |

**测试提示**：
- 此接口一般由第三方支付平台调用，手动测试可以简化

---

### 第六阶段：管理端接口（需要管理员Token）

**重要**：测试管理端前，必须先登录获取管理员token

#### 23. 管理员登录

**接口**：`POST /api/admin/login`  
**请求方式**：`POST`  
**完整URL**：`http://localhost:8080/api/admin/login`

| 序号 | 用例描述 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|---------|---------|---------|---------|
| 24.1 | 管理员登录成功 | `account: admin`<br>`password: admin123` | 正向 | 仅传必要字段、语义合法 | 返回200，包含token（role=2） | ⬜ |
| 24.2 | 账号或密码错误 | `account: admin`<br>`password: wrong` | 负向 | 无效值 | 返回200，message包含"登录失败" | ⬜ |
| 24.3 | 非管理员账号 | `account: testuser`<br>`password: test123` | 安全性 | 无效值 | 返回200，message包含"您不是管理员" | ⬜ |
| 24.4 | 账号为空 | `account: ""`<br>`password: admin123` | 负向 | 缺失必填字段、无效值 | 返回200，message包含"登录失败" | ⬜ |

**测试提示**：
- **24.1 成功后，务必保存token到环境变量 `admin_token`**，后续所有管理员接口都需要使用这个token
- 管理员账号：`admin` / `admin123`

---

#### 24. 分页查询用户列表（管理端）

**接口**：`GET /api/admin/users`  
**请求方式**：`GET`  
**完整URL**：`http://localhost:8080/api/admin/users`  
**需要Token**：✅ 是（管理员token）

| 序号 | 用例描述 | 请求头 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|-------|---------|---------|---------|---------|---------|
| 25.1 | 查询所有用户 | `Authorization: Bearer {{admin_token}}` | 无参数 | 正向 | 仅传必要字段、语义合法 | 返回200，分页结果 | ⬜ |
| 25.2 | 按用户名筛选 | `Authorization: Bearer {{admin_token}}` | `username: test` | 正向 | 语义合法、其他正向 | 返回200，匹配的用户列表 | ⬜ |
| 25.3 | 按状态筛选（正常用户） | `Authorization: Bearer {{admin_token}}` | `status: 1`<br>`pageNum: 1`<br>`pageSize: 10` | 正向 | 覆盖枚举组合、语义合法 | 返回200，符合条件的用户列表 | ⬜ |
| 25.4 | 按状态筛选（禁用用户） | `Authorization: Bearer {{admin_token}}` | `status: 0`<br>`pageNum: 1`<br>`pageSize: 10` | 正向 | 覆盖枚举组合、语义合法 | 返回200，符合条件的用户列表 | ⬜ |
| 25.5 | 未登录 | 无Authorization头 | 无参数 | 安全性 | 缺失必填字段 | 返回401 | ⬜ |
| 25.6 | 非管理员Token | `Authorization: Bearer {{token}}`（普通用户token） | 无参数 | 安全性 | 无效值 | 返回403，`{"error":"禁止访问：需要管理员权限"}` | ⬜ |

**测试提示**：
- 25.1-25.4 使用管理员token
- 25.6 使用普通用户的token，测试权限验证

---

#### 25. 查询用户详情（管理端）

**接口**：`GET /api/admin/users/{id}`  
**请求方式**：`GET`  
**完整URL**：`http://localhost:8080/api/admin/users/1`（id为用户ID）  
**需要Token**：✅ 是（管理员token）

| 序号 | 用例描述 | 路径参数 | 请求头 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|-------|---------|---------|---------|---------|
| 26.1 | 用户存在 | `id: 1` | `Authorization: Bearer {{admin_token}}` | 正向 | 仅传必要字段、语义合法 | 返回200，`{"user": {用户详情JSON对象}}` | ⬜ |
| 26.2 | 用户不存在 | `id: 999999` | `Authorization: Bearer {{admin_token}}` | 负向 | 无效值 | 返回200，`{"user": null}`（确保始终返回有效的JSON对象） | ⬜ |
| 26.3 | 未登录 | `id: 1` | 无Authorization头 | 安全性 | 缺失必填字段 | 返回401 | ⬜ |
| 26.4 | 非管理员 | `id: 1` | `Authorization: Bearer {{token}}`（普通用户token） | 安全性 | 无效值 | 返回403 | ⬜ |

---

#### 26. 修改用户信息（管理端）

**接口**：`PUT /api/admin/users/{id}`  
**请求方式**：`PUT`  
**完整URL**：`http://localhost:8080/api/admin/users/1`（id为用户ID）  
**需要Token**：✅ 是（管理员token）  
**Content-Type**：`application/json`

| 序号 | 用例描述 | 路径参数 | 请求头 | 请求体（JSON） | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|-------|---------------|---------|---------|---------|---------|
| 27.1 | 修改用户信息 | `id: 1` | `Authorization: Bearer {{admin_token}}` | `{"email": "newemail@example.com", "phone": "13800138000", "realName": "新名字"}` | 正向 | 语义合法 | 返回200，`{"message": "用户信息更新成功！", "success": true}` | ⬜ |
| 27.2 | 用户不存在 | `id: 999999` | `Authorization: Bearer {{admin_token}}` | `{...}` | 负向 | 无效值 | 返回200，`{"message": "更新失败！", "success": false}` | ⬜ |
| 27.3 | 未登录 | `id: 1` | 无Authorization头 | `{...}` | 安全性 | 缺失必填字段 | 返回401 | ⬜ |
| 27.4 | 非管理员 | `id: 1` | `Authorization: Bearer {{token}}`（普通用户token） | `{...}` | 安全性 | 无效值 | 返回403 | ⬜ |

---

#### 27. 启用/禁用用户（管理端）

**接口**：`PUT /api/admin/users/{id}/status`  
**请求方式**：`PUT`  
**完整URL**：`http://localhost:8080/api/admin/users/1/status?status=0`（id为用户ID）  
**需要Token**：✅ 是（管理员token）

| 序号 | 用例描述 | 路径参数 | 请求头 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|-------|---------|---------|---------|---------|---------|
| 28.1 | 禁用用户 | `id: 1` | `Authorization: Bearer {{admin_token}}` | `status: 0` | 正向 | 覆盖枚举组合、语义合法 | 返回200，`{"message": "用户状态更新成功！", "success": true}` | ⬜ |
| 28.2 | 启用用户 | `id: 1` | `Authorization: Bearer {{admin_token}}` | `status: 1` | 正向 | 覆盖枚举组合、语义合法 | 返回200，`{"message": "用户状态更新成功！", "success": true}` | ⬜ |
| 28.3 | 用户不存在 | `id: 999999` | `Authorization: Bearer {{admin_token}}` | `status: 0` | 负向 | 无效值 | 返回200，`{"message": "更新失败！", "success": false}` | ⬜ |
| 28.4 | 未登录 | `id: 1` | 无Authorization头 | `status: 0` | 安全性 | 缺失必填字段 | 返回401 | ⬜ |
| 28.5 | 非管理员 | `id: 1` | `Authorization: Bearer {{token}}`（普通用户token） | `status: 0` | 安全性 | 无效值 | 返回403 | ⬜ |
| 28.6 | status无效值 | `id: 1` | `Authorization: Bearer {{admin_token}}` | `status: 99` | 边界值 | 无效值 | 返回200，`{"message": "更新失败！状态值无效，只允许 0（禁用）或 1（正常）。", "success": false}` | ⬜ |

**测试提示**：
- 28.1 禁用用户后，该用户应该无法登录
- 28.2 重新启用用户
- **用户状态验证规则（28.6）**：
  - 用户状态只允许 `0`（禁用）或 `1`（正常）
  - 如果传入无效值（如 `99`），系统会返回错误提示："更新失败！状态值无效，只允许 0（禁用）或 1（正常）。"

---

#### 28. 添加演出信息（管理端）

**接口**：`POST /api/admin/shows`  
**请求方式**：`POST`  
**完整URL**：`http://localhost:8080/api/admin/shows`  
**需要Token**：✅ 是（管理员token）  
**Content-Type**：`application/json`

| 序号 | 用例描述 | 请求头 | 请求体（JSON） | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|-------|---------------|---------|---------|---------|---------|
| 29.1 | 添加演出（完整信息） | `Authorization: Bearer {{admin_token}}` | `{"name": "周杰伦演唱会", "venue": "北京鸟巢", "region": "北京", "category": "演唱会", "startTime": "2024-12-31T20:00:00", "endTime": "2024-12-31T23:00:00", "totalTickets": 1000, "availableTickets": 1000, "price": 580.00, "sessionInfo": "场次信息", "ticketTier": "票档信息", "isOnSale": 1}` | 正向 | 语义合法 | 返回200，`{"message": "演出添加成功！ID: X", "success": true}`（X为实际演出ID） | ⬜ |
| 29.2 | 添加演出（仅必要字段） | `Authorization: Bearer {{admin_token}}` | `{"name": "测试演出", "venue": "测试场馆", "startTime": "2024-12-31T20:00:00", "endTime": "2024-12-31T23:00:00", "totalTickets": 100, "price": 100.00}` | 正向 | 仅传必要字段、语义合法 | 返回200，`{"message": "演出添加成功！ID: X", "success": true}`（X为实际演出ID） | ⬜ |
| 29.3 | 未登录 | 无Authorization头 | `{...}` | 安全性 | 缺失必填字段 | 返回401 | ⬜ |
| 29.4 | 非管理员 | `Authorization: Bearer {{token}}`（普通用户token） | `{...}` | 安全性 | 无效值 | 返回403 | ⬜ |
| 29.5 | 名称为空 | `Authorization: Bearer {{admin_token}}` | `{"name": "", ...}` | 负向 | 缺失必填字段 | 返回200，`{"message": "添加失败！", "success": false}` | ⬜ |
| 29.6 | totalTickets为0 | `Authorization: Bearer {{admin_token}}` | `{"totalTickets": 0, ...}` | 边界值 | 无效值 | 返回200，`{"message": "添加失败！", "success": false}` | ⬜ |
| 29.7 | totalTickets为负数 | `Authorization: Bearer {{admin_token}}` | `{"totalTickets": -1, ...}` | 边界值 | 无效值 | 返回200，`{"message": "添加失败！", "success": false}` | ⬜ |
| 29.8 | 价格格式错误 | `Authorization: Bearer {{admin_token}}` | `{"price": "abc", ...}` | 负向 | 类型错误 | 返回400，`{"message": "添加失败！价格格式不正确，应为数字类型（如：100.00）。", "success": false}` | ⬜ |

**测试提示**：
- 29.1-29.2 添加几个测试用的演出数据，用于后续订单测试
- **记住演出ID**，后续创建订单时需要使用
- **价格格式验证（29.8）**：
  - 价格字段必须是数字类型（如：`100.00` 或 `100`）
  - 如果传入字符串（如：`"abc"`），系统会返回 400 错误，提示："添加失败！价格格式不正确，应为数字类型（如：100.00）。"
  - 这是由全局异常处理器捕获 JSON 反序列化异常后返回的友好错误提示

---

#### 29. 修改演出信息（管理端）

**接口**：`PUT /api/admin/shows/{id}`  
**请求方式**：`PUT`  
**完整URL**：`http://localhost:8080/api/admin/shows/1`（id为演出ID）  
**需要Token**：✅ 是（管理员token）  
**Content-Type**：`application/json`

| 序号 | 用例描述 | 路径参数 | 请求头 | 请求体（JSON） | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|-------|---------------|---------|---------|---------|---------|
| 30.1 | 修改演出信息 | `id: 1` | `Authorization: Bearer {{admin_token}}` | `{"name": "新名字", "price": 680.00, ...}` | 正向 | 语义合法 | 返回200，`{"message": "演出信息更新成功！", "success": true}` | ⬜ |
| 30.2 | 演出不存在 | `id: 999999` | `Authorization: Bearer {{admin_token}}` | `{...}` | 负向 | 无效值 | 返回200，`{"message": "更新失败！", "success": false}` | ⬜ |
| 30.3 | 未登录 | `id: 1` | 无Authorization头 | `{...}` | 安全性 | 缺失必填字段 | 返回401 | ⬜ |
| 30.4 | 非管理员 | `id: 1` | `Authorization: Bearer {{token}}`（普通用户token） | `{...}` | 安全性 | 无效值 | 返回403 | ⬜ |

---

#### 30. 分页查询演出列表（管理端）

**接口**：`GET /api/admin/shows`  
**请求方式**：`GET`  
**完整URL**：`http://localhost:8080/api/admin/shows`  
**需要Token**：✅ 是（管理员token）

| 序号 | 用例描述 | 请求头 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|-------|---------|---------|---------|---------|---------|
| 31.1 | 查询所有演出 | `Authorization: Bearer {{admin_token}}` | 无参数 | 正向 | 仅传必要字段、语义合法 | 返回200，分页结果 | ⬜ |
| 31.2 | 条件筛选 | `Authorization: Bearer {{admin_token}}` | `name: 周杰伦`<br>`region: 北京` | 正向 | 覆盖枚举组合、语义合法 | 返回200，符合条件的列表 | ⬜ |
| 31.3 | 未登录 | 无Authorization头 | 无参数 | 安全性 | 缺失必填字段 | 返回401 | ⬜ |
| 31.4 | 非管理员 | `Authorization: Bearer {{token}}`（普通用户token） | 无参数 | 安全性 | 无效值 | 返回403 | ⬜ |

---

#### 31. 查询演出详情（管理端）

**接口**：`GET /api/admin/shows/{id}`  
**请求方式**：`GET`  
**完整URL**：`http://localhost:8080/api/admin/shows/1`（id为演出ID）  
**需要Token**：✅ 是（管理员token）

| 序号 | 用例描述 | 路径参数 | 请求头 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|-------|---------|---------|---------|---------|
| 32.1 | 演出存在 | `id: 1` | `Authorization: Bearer {{admin_token}}` | 正向 | 仅传必要字段、语义合法 | 返回200，`{"show": {演出详情JSON对象}}` | ⬜ |
| 32.2 | 演出不存在 | `id: 999999` | `Authorization: Bearer {{admin_token}}` | 负向 | 无效值 | 返回200，`{"show": null}`（确保始终返回有效的JSON对象） | ⬜ |
| 32.3 | 未登录 | `id: 1` | 无Authorization头 | 安全性 | 缺失必填字段 | 返回401 | ⬜ |
| 32.4 | 非管理员 | `id: 1` | `Authorization: Bearer {{token}}`（普通用户token） | 安全性 | 无效值 | 返回403 | ⬜ |

---

#### 32. 删除演出信息（管理端）

**接口**：`DELETE /api/admin/shows/{id}`  
**请求方式**：`DELETE`  
**完整URL**：`http://localhost:8080/api/admin/shows/1`（id为演出ID）  
**需要Token**：✅ 是（管理员token）

| 序号 | 用例描述 | 路径参数 | 请求头 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|-------|---------|---------|---------|---------|
| 33.1 | 删除演出 | `id: 1` | `Authorization: Bearer {{admin_token}}` | 正向 | 语义合法 | 返回200，`{"message": "演出信息删除成功！", "success": true}` | ⬜ |
| 33.2 | 演出不存在 | `id: 999999` | `Authorization: Bearer {{admin_token}}` | 负向 | 无效值 | 返回200，`{"message": "删除失败！", "success": false}` | ⬜ |
| 33.3 | 未登录 | `id: 1` | 无Authorization头 | 安全性 | 缺失必填字段 | 返回401 | ⬜ |
| 33.4 | 非管理员 | `id: 1` | `Authorization: Bearer {{token}}`（普通用户token） | 安全性 | 无效值 | 返回403 | ⬜ |

**测试提示**：
- 注意：删除演出前，确保没有关联的订单

---

#### 33. 分页查询订单列表（管理端）

**接口**：`GET /api/admin/orders`  
**请求方式**：`GET`  
**完整URL**：`http://localhost:8080/api/admin/orders`  
**需要Token**：✅ 是（管理员token）

| 序号 | 用例描述 | 请求头 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|-------|---------|---------|---------|---------|---------|
| 34.1 | 查询所有订单 | `Authorization: Bearer {{admin_token}}` | 无参数 | 正向 | 仅传必要字段、语义合法 | 返回200，分页结果 | ⬜ |
| 34.2 | 条件筛选 | `Authorization: Bearer {{admin_token}}` | `userId: 1`<br>`status: 1` | 正向 | 覆盖枚举组合、语义合法 | 返回200，符合条件的列表 | ⬜ |
| 34.3 | 未登录 | 无Authorization头 | 无参数 | 安全性 | 缺失必填字段 | 返回401 | ⬜ |
| 34.4 | 非管理员 | `Authorization: Bearer {{token}}`（普通用户token） | 无参数 | 安全性 | 无效值 | 返回403 | ⬜ |

---

#### 34. 查询订单详情（管理端）

**接口**：`GET /api/admin/orders/{id}`  
**请求方式**：`GET`  
**完整URL**：`http://localhost:8080/api/admin/orders/1`（id为订单ID）  
**需要Token**：✅ 是（管理员token）

| 序号 | 用例描述 | 路径参数 | 请求头 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|-------|---------|---------|---------|---------|
| 35.1 | 订单存在 | `id: 1` | `Authorization: Bearer {{admin_token}}` | 正向 | 仅传必要字段、语义合法 | 返回200，`{"order": {订单详情JSON对象}}` | ⬜ |
| 35.2 | 订单不存在 | `id: 999999` | `Authorization: Bearer {{admin_token}}` | 负向 | 无效值 | 返回200，`{"order": null}`（确保始终返回有效的JSON对象） | ⬜ |
| 35.3 | 未登录 | `id: 1` | 无Authorization头 | 安全性 | 缺失必填字段 | 返回401 | ⬜ |
| 35.4 | 非管理员 | `id: 1` | `Authorization: Bearer {{token}}`（普通用户token） | 安全性 | 无效值 | 返回403 | ⬜ |

---

#### 35. 修改订单状态（管理端）

**接口**：`PUT /api/admin/orders/{id}/status`  
**请求方式**：`PUT`  
**完整URL**：`http://localhost:8080/api/admin/orders/1/status?newStatus=2`（id为订单ID）  
**需要Token**：✅ 是（管理员token）

**订单状态说明**：
- 1: 待支付
- 2: 已支付
- 3: 已取消
- 4: 已退款

| 序号 | 用例描述 | 路径参数 | 请求头 | 请求参数 | 目标分类 | 测试标签 | 预期结果 | 测试状态 |
|------|---------|---------|-------|---------|---------|---------|---------|---------|
| 36.1 | 更新订单状态为已支付 | `id: 1` | `Authorization: Bearer {{admin_token}}` | `newStatus: 2` | 正向 | 覆盖枚举组合、语义合法 | 返回200，`{"message": "订单状态更新成功！", "success": true}` | ⬜ |
| 36.2 | 更新订单状态为已取消 | `id: 1` | `Authorization: Bearer {{admin_token}}` | `newStatus: 3` | 正向 | 覆盖枚举组合、语义合法 | 返回200，`{"message": "订单状态更新成功！", "success": true}` | ⬜ |
| 36.3 | 更新订单状态为已退款 | `id: 1` | `Authorization: Bearer {{admin_token}}` | `newStatus: 4` | 正向 | 覆盖枚举组合、语义合法 | 返回200，`{"message": "订单状态更新成功！", "success": true}` | ⬜ |
| 36.4 | 订单不存在 | `id: 999999` | `Authorization: Bearer {{admin_token}}` | `newStatus: 2` | 负向 | 无效值 | 返回200，`{"message": "更新失败！", "success": false}` | ⬜ |
| 36.5 | 未登录 | `id: 1` | 无Authorization头 | `newStatus: 2` | 安全性 | 缺失必填字段 | 返回401 | ⬜ |
| 36.6 | 非管理员 | `id: 1` | `Authorization: Bearer {{token}}`（普通用户token） | `newStatus: 2` | 安全性 | 无效值 | 返回403 | ⬜ |
| 36.7 | 无效状态值 | `id: 1` | `Authorization: Bearer {{admin_token}}` | `newStatus: 99` | 边界值 | 无效值 | 返回200，`{"message": "更新失败！状态值无效，只允许 1（待支付）、2（已支付）、3（已取消）、4（已退款）。", "success": false}` | ⬜ |

**测试提示**：
- **订单状态验证规则（36.7）**：
  - 订单状态只允许 `1`（待支付）、`2`（已支付）、`3`（已取消）、`4`（已退款）
  - 如果传入无效值（如 `99`），系统会返回错误提示："更新失败！状态值无效，只允许 1（待支付）、2（已支付）、3（已取消）、4（已退款）。"

---

## 📝 测试记录模板

### 测试进度追踪

| 阶段 | 接口名称 | 测试用例数 | 已完成 | 未完成 | 备注 |
|------|---------|----------|--------|--------|------|
| 第一阶段 | 用户认证 | 29 | | | |
| 第二阶段 | 收货地址管理 | 20 | | | |
| 第三阶段 | 演出查询 | 20 | | | |
| 第四阶段 | 订单管理 | 30 | | | |
| 第五阶段 | 支付模块 | 6 | | | |
| 第六阶段 | 管理端接口 | 50 | | | |
| **总计** | | **161** | | | |

---

## 💡 测试技巧

### 1. Apifox 环境变量设置

**步骤**：
1. 打开 Apifox
2. 点击右上角「环境」按钮
3. 创建新环境或编辑现有环境
4. 添加变量：
   - 变量名：`token`，初始值：空
   - 变量名：`admin_token`，初始值：空

### 2. 自动保存Token

在登录接口的「后置操作」→「提取变量」中添加：

**用户注册/登录接口（POST /api/user/register-or-login）**：
```
变量名：token
提取表达式：$.token
```

**管理员登录接口（POST /api/admin/login）**：
```
变量名：admin_token
提取表达式：$.token
```

### 3. 使用环境变量

在需要Token的接口请求头中：
```
Authorization: Bearer {{token}}
```
或
```
Authorization: Bearer {{admin_token}}
```

### 4. 测试顺序建议

1. **先测试登录**：获取token
2. **测试公开接口**：验证基础功能
3. **测试用户接口**：验证用户功能
4. **测试管理端接口**：验证管理功能
5. **测试异常场景**：验证错误处理

### 5. 常见问题

**Q: Token无效怎么办？**
A: 重新登录获取新的token，并更新环境变量

**Q: 如何快速找到接口？**
A: 使用Apifox的搜索功能，搜索接口路径的关键词

**Q: 测试失败怎么办？**
A: 
1. 检查请求URL是否正确
2. 检查请求参数是否正确
3. 检查请求头（特别是Token）是否正确
4. 查看响应内容，了解错误原因
5. 查看服务器日志

---

## ✅ 测试完成检查清单

- [ ] 所有正向用例测试通过
- [ ] 所有负向用例测试通过
- [ ] 所有边界值用例测试通过
- [ ] 所有安全性用例测试通过
- [ ] 记录所有失败的用例及原因
- [ ] 验证错误提示信息是否友好
- [ ] 验证Token过期后的处理
- [ ] 验证权限控制是否正确

---

**祝你测试顺利！如有问题，请查看服务器日志或联系开发者。** 🎉


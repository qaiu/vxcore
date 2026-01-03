# User Registration Feature

## Overview

This document describes the user registration feature added to the VXCore framework.

## Feature Description

The user registration feature provides a secure and validated endpoint for new users to register in the system.

## API Endpoint

### POST /api/users/register

Registers a new user with validation.

#### Request Body

```json
{
  "username": "string",
  "email": "string",
  "password": "string",
  "confirmPassword": "string",
  "age": integer (optional)
}
```

#### Validation Rules

1. **Username**:
   - Required field
   - Length: 3-50 characters
   - Must be unique in the system

2. **Email**:
   - Required field
   - Must be a valid email format (e.g., user@example.com)
   - Must be unique in the system

3. **Password**:
   - Required field
   - Minimum 8 characters
   - Must contain at least 1 letter and 1 number
   - Supports special characters: @$!%*#?&

4. **Confirm Password**:
   - Required field
   - Must match the password field

5. **Age**:
   - Optional field
   - Integer value

#### Success Response

Status Code: 200

```json
{
  "code": 200,
  "msg": "注册成功",
  "success": true,
  "data": {
    "id": 1,
    "username": "testuser",
    "email": "test@example.com",
    "age": 25,
    "status": "ACTIVE",
    "createTime": "2025-10-13T01:00:00"
  },
  "timestamp": 1697145600000
}
```

#### Error Responses

Status Code: 200 (with error flag)

**Empty Username:**
```json
{
  "code": 500,
  "msg": "注册失败: 用户名不能为空",
  "success": false,
  "data": null,
  "timestamp": 1697145600000
}
```

**Invalid Email Format:**
```json
{
  "code": 500,
  "msg": "注册失败: 邮箱格式不正确",
  "success": false,
  "data": null,
  "timestamp": 1697145600000
}
```

**Weak Password:**
```json
{
  "code": 500,
  "msg": "注册失败: 密码必须至少8个字符,包含至少1个字母和1个数字",
  "success": false,
  "data": null,
  "timestamp": 1697145600000
}
```

**Password Mismatch:**
```json
{
  "code": 500,
  "msg": "注册失败: 两次输入的密码不一致",
  "success": false,
  "data": null,
  "timestamp": 1697145600000
}
```

**Duplicate Username:**
```json
{
  "code": 500,
  "msg": "注册失败: 用户名已被使用",
  "success": false,
  "data": null,
  "timestamp": 1697145600000
}
```

**Duplicate Email:**
```json
{
  "code": 500,
  "msg": "注册失败: 邮箱已被使用",
  "success": false,
  "data": null,
  "timestamp": 1697145600000
}
```

## Usage Example

### Using cURL

```bash
curl -X POST http://localhost:8080/api/users/register \
  -H "Content-Type: application/json" \
  -d '{
    "username": "johndoe",
    "email": "john.doe@example.com",
    "password": "SecurePass123",
    "confirmPassword": "SecurePass123",
    "age": 30
  }'
```

### Using JavaScript Fetch API

```javascript
fetch('http://localhost:8080/api/users/register', {
  method: 'POST',
  headers: {
    'Content-Type': 'application/json',
  },
  body: JSON.stringify({
    username: 'johndoe',
    email: 'john.doe@example.com',
    password: 'SecurePass123',
    confirmPassword: 'SecurePass123',
    age: 30
  })
})
.then(response => response.json())
.then(data => {
  if (data.success) {
    console.log('Registration successful:', data.data);
  } else {
    console.error('Registration failed:', data.msg);
  }
})
.catch(error => console.error('Error:', error));
```

## Implementation Details

### Files Modified/Created

1. **UserRegistrationRequest.java** (New)
   - DTO for registration requests
   - Location: `core-example/src/main/java/cn/qaiu/example/model/UserRegistrationRequest.java`

2. **UserService.java** (Modified)
   - Added `registerUser()` method with validation logic
   - Email and password pattern validation
   - Duplicate username/email checking
   - Location: `core-example/src/main/java/cn/qaiu/example/service/UserService.java`

3. **UserController.java** (Modified)
   - Added `/register` endpoint
   - Location: `core-example/src/main/java/cn/qaiu/example/controller/UserController.java`

4. **UserRegistrationTest.java** (New)
   - Unit tests for registration validation
   - Location: `core-example/src/test/java/cn/qaiu/example/service/UserRegistrationTest.java`

### Security Considerations

> **⚠️ Important Security Note**: 
> The current implementation stores passwords in plain text for demonstration purposes. 
> In a production environment, you MUST:
> - Hash passwords using a strong algorithm (e.g., BCrypt, Argon2)
> - Use salted hashing
> - Never store passwords in plain text
> - Implement rate limiting for registration attempts
> - Add CAPTCHA to prevent automated registration
> - Implement email verification

### Example Password Hashing (for production)

```java
// Add BCrypt dependency to pom.xml
// <dependency>
//   <groupId>org.mindrot</groupId>
//   <artifactId>jbcrypt</artifactId>
//   <version>0.4</version>
// </dependency>

import org.mindrot.jbcrypt.BCrypt;

// When registering:
String hashedPassword = BCrypt.hashpw(request.getPassword(), BCrypt.gensalt());
newUser.setPassword(hashedPassword);

// When authenticating:
if (BCrypt.checkpw(plainPassword, storedHashedPassword)) {
    // Password is correct
}
```

## Testing

Run the tests using Maven:

```bash
mvn test -Dtest=UserRegistrationTest
```

## Future Enhancements

1. Email verification workflow
2. Password reset functionality
3. Two-factor authentication
4. OAuth2 integration
5. Account activation via email
6. Rate limiting for registration attempts
7. CAPTCHA integration
8. Profile picture upload during registration

## Contributing

When contributing to this feature, please ensure:
- All validation rules are properly tested
- Error messages are clear and user-friendly
- Security best practices are followed
- Code is well-documented

## License

MIT License - Same as the VXCore framework

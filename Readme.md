# **DevToolkit Library**

### **Mục tiêu**
Thư viện `DevToolkit` cung cấp các tính năng dùng chung cho các dự án Spring Boot nhằm giảm thiểu **boilerplate code** và tối ưu hóa phát triển ứng dụng. Nó bao gồm:
- **CRUD**: Cơ sở cho các thao tác tạo, đọc, cập nhật và xóa.
- **Phân trang**: Công cụ hỗ trợ xử lý phân trang.
- **Truy vấn động**: Tích hợp query động với `JpaSpecificationExecutor`.
- **Xử lý quan hệ bảng (Join)**: Tạo truy vấn dựa trên quan hệ bảng.
- **Validation**: Kiểm tra tính hợp lệ của dữ liệu với custom annotation.
- **Xử lý lỗi chung**: Quản lý lỗi cho toàn bộ ứng dụng.

---

### **1. Dependency**

#### **Thêm vào `pom.xml`**
```xml
<dependencies>
    <!-- Spring Boot Starter Data JPA -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-data-jpa</artifactId>
    </dependency>

    <!-- Spring Boot Starter Validation -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-validation</artifactId>
    </dependency>

    <!-- Spring Boot Starter Web -->
    <dependency>
        <groupId>org.springframework.boot</groupId>
        <artifactId>spring-boot-starter-web</artifactId>
    </dependency>

    <!-- Lombok -->
    <dependency>
        <groupId>org.projectlombok</groupId>
        <artifactId>lombok</artifactId>
        <scope>provided</scope>
    </dependency>

    <!-- Hibernate Core -->
    <dependency>
        <groupId>org.hibernate</groupId>
        <artifactId>hibernate-core</artifactId>
    </dependency>
</dependencies>
```

---

### **2. Cấu trúc thư mục**
Thư viện được tổ chức theo cấu trúc như sau:

```plaintext
src/main/java/io/akitect/devtoolkit/
    ├── repository/        # Base Repository
    │   └── BaseRepository.java
    ├── service/           # Base Service
    │   └── BaseService.java
    ├── utils/             # Query Helper, Pagination Helper
    │   ├── QueryHelper.java
    │   ├── PaginationHelper.java
    ├── validation/        # Custom Validation
    │   ├── ValidField.java
    │   ├── CustomValidator.java
    ├── exception/         # Global Exception Handler
    │   └── GlobalExceptionHandler.java
```

---

### **3. Hướng dẫn sử dụng**

#### **3.1. Base Repository**
`BaseRepository` cung cấp khả năng CRUD cơ bản và hỗ trợ truy vấn động bằng `JpaSpecificationExecutor`.

```java
package io.akitect.devtoolkit.repository;

import org.springframework.data.jpa.domain.Specification;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.JpaSpecificationExecutor;

import java.util.List;

public interface BaseRepository<T, ID> extends JpaRepository<T, ID>, JpaSpecificationExecutor<T> {
    default List<T> findAllBySpecification(Specification<T> specification) {
        return findAll(specification);
    }
}
```

##### **Ví dụ sử dụng**
```java
package com.example.repository;

import io.akitect.devtoolkit.entity.User;
import io.akitect.devtoolkit.repository.BaseRepository;

public interface UserRepository extends BaseRepository<User, Long> {
}
```

---

#### **3.2. Base Service**
`BaseService` hỗ trợ thao tác CRUD và tìm kiếm theo `Specification`.

```java
package io.akitect.devtoolkit.service;

import io.akitect.devtoolkit.repository.BaseRepository;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.domain.Specification;

import java.util.List;
import java.util.Optional;

public abstract class BaseService<T, ID> {
    protected abstract BaseRepository<T, ID> getRepository();

    public T save(T entity) {
        return getRepository().save(entity);
    }

    public Optional<T> findById(ID id) {
        return getRepository().findById(id);
    }

    public List<T> findAll() {
        return getRepository().findAll();
    }

    public Page<T> findAll(Pageable pageable) {
        return getRepository().findAll(pageable);
    }

    public void deleteById(ID id) {
        getRepository().deleteById(id);
    }

    public List<T> findAllBySpecification(Specification<T> specification) {
        return getRepository().findAll(specification);
    }
}
```

##### **Ví dụ sử dụng**
```java
package com.example.service;

import com.example.entity.User;
import com.example.repository.UserRepository;
import io.akitect.devtoolkit.service.BaseService;
import org.springframework.stereotype.Service;

@Service
public class UserService extends BaseService<User, Long> {
    private final UserRepository userRepository;

    public UserService(UserRepository userRepository) {
        this.userRepository = userRepository;
    }

    @Override
    protected UserRepository getRepository() {
        return userRepository;
    }
}
```

---

#### **3.3. Query Helper**
`QueryHelper` hỗ trợ tạo truy vấn động với các điều kiện linh hoạt.

```java
package io.akitect.devtoolkit.utils;

import org.springframework.data.jpa.domain.Specification;
import jakarta.persistence.criteria.Predicate;
import java.util.ArrayList;
import java.util.List;

public class QueryHelper<T> {
    public static <T> Specification<T> createSpecification(List<QueryFilter> filters) {
        return (root, query, criteriaBuilder) -> {
            List<Predicate> predicates = new ArrayList<>();
            for (QueryFilter filter : filters) {
                switch (filter.operation()) {
                    case EQUAL -> predicates.add(criteriaBuilder.equal(root.get(filter.field()), filter.value()));
                    case LIKE -> predicates.add(criteriaBuilder.like(root.get(filter.field()), "%" + filter.value() + "%"));
                }
            }
            return criteriaBuilder.and(predicates.toArray(new Predicate[0]));
        };
    }

    public static record QueryFilter(String field, Object value, Operation operation) {}
    public enum Operation { EQUAL, LIKE }
}
```

##### **Ví dụ sử dụng**
```java
@GetMapping("/search")
public List<User> searchUsers(@RequestParam String name, @RequestParam Boolean active) {
    return userService.findAllBySpecification(QueryHelper.createSpecification(List.of(
            new QueryHelper.QueryFilter("name", name, QueryHelper.Operation.LIKE),
            new QueryHelper.QueryFilter("active", active, QueryHelper.Operation.EQUAL)
    )));
}
```

---

#### **3.4. Pagination Helper**
`PaginationHelper` cung cấp cách dễ dàng để phân trang và trả về dữ liệu.

```java
package io.akitect.devtoolkit.utils;

import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

import java.util.List;

public class PaginationHelper {
    public static Pageable createPageRequest(int page, int size, String sortBy, String direction) {
        Sort sort = direction.equalsIgnoreCase("desc") ? Sort.by(sortBy).descending() : Sort.by(sortBy).ascending();
        return PageRequest.of(page, size, sort);
    }

    public static <T> PaginationResponse<T> createPaginationResponse(Page<T> pageData) {
        return new PaginationResponse<>(
                pageData.getContent(),
                pageData.getNumber(),
                pageData.getSize(),
                pageData.getTotalElements()
        );
    }

    public static record PaginationResponse<T>(
            List<T> content, int page, int size, long totalElements) {}
}
```

##### **Ví dụ sử dụng**
```java
@GetMapping("/paged")
public PaginationHelper.PaginationResponse<User> getPagedUsers(
        @RequestParam int page, @RequestParam int size, @RequestParam String sortBy, @RequestParam String direction) {
    var pageable = PaginationHelper.createPageRequest(page, size, sortBy, direction);
    var pagedResult = userService.findAll(pageable);
    return PaginationHelper.createPaginationResponse(pagedResult);
}
```

---

#### **3.5. Validation**
Thêm validation vào các DTO bằng `jakarta.validation`.

##### **Custom Validation**
```java
package io.akitect.devtoolkit.validation;

import jakarta.validation.Constraint;
import jakarta.validation.Payload;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Constraint(validatedBy = { CustomValidator.class })
@Target({ ElementType.FIELD, ElementType.PARAMETER })
@Retention(RetentionPolicy.RUNTIME)
public @interface ValidField {
    String message() default "Invalid field";
    Class<?>[] groups() default {};
    Class<? extends Payload>[] payload() default {};
}
```

---

### **4. Xử lý lỗi**

Tạo lớp xử lý lỗi chung:

```java
package io.akitect.devtoolkit.exception;

import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;
import org.springframework.web.bind.annotation.ExceptionHandler;

@ControllerAdvice
public class GlobalExceptionHandler {
    @ExceptionHandler(Exception.class)
    public ResponseEntity<String> handleException(Exception ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.INTERNAL_SERVER_ERROR);
    }
}
```
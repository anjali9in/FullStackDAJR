# custom exception handling
## try catch throw throws using custom exception
* custom exception can be done on Checked and Unchecked exception.

```java

// 1. Extend RuntimeException (or Exception for a checked exception)
public class InvalidAgeException extends RuntimeException {
    
// 2. Default constructor
    public InvalidAgeException() {
        super();
    }

// 3. Constructor that accepts a custom error message
    public InvalidAgeException(String message) {
        super(message); // Passes the message to the parent RuntimeException class
    }
}

public class TestCustomException {

    public static void validateAge(int age) {
        if (age < 18) {
            throw new InvalidAgeException("Age must be 18 or older.");
        }
        System.out.println("Valid age: " + age);
    }

    public static void main(String[] args) {
        try {
            validateAge(15); // This will throw an InvalidAgeException
        } catch (InvalidAgeException e) {
            System.out.println("Caught an exception: " + e.getMessage());
        }
    }
}

```

## custom exception using checked exception

```java
// 1. Extend Exception for a checked exception
public class InvalidAgeCheckedException extends Exception { 
    // 2. Default constructor
    public InvalidAgeCheckedException() {
        super();
    }

    // 3. Constructor that accepts a custom error message
    public InvalidAgeCheckedException(String message) {
        super(message); // Passes the message to the parent Exception class
    }
}

```
## custom exception using unchecked exception

```java
// 1. Extend RuntimeException for an unchecked exception
public class InvalidAgeUncheckedException extends RuntimeException {
    // 2. Default constructor
    public InvalidAgeUncheckedException() {
        super();
    }

    // 3. Constructor that accepts a custom error message
    public InvalidAgeUncheckedException(String message) {
        super(message); // Passes the message to the parent RuntimeException class
    }
}
```


### Custom exception using controllerAdvice in Spring Boot 
steps - 
 * Create a custom exception class that extends RuntimeException or Exception.
 * Create a global exception handler class annotated with @ControllerAdvice.
 * Define methods in the global exception handler class to handle specific exceptions using @ExceptionHandler annotation.
 * Return appropriate HTTP responses from the exception handler methods.

how and where to use @ControllerAdvice and @ExceptionHandler in Spring Boot for custom exception handling:

```java
import org.springframework.http.HttpStatus;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.ControllerAdvice;        

import org.springframework.web.bind.annotation.ExceptionHandler;
 
@ControllerAdvice
public class GlobalExceptionHandler {

    @ExceptionHandler(InvalidAgeException.class)
    public ResponseEntity<String> handleInvalidAgeException(InvalidAgeException ex) {
        return new ResponseEntity<>(ex.getMessage(), HttpStatus.BAD_REQUEST);
    }

    // ... other exception handlers can be added here
    // so when InvalidAgeException is thrown in any controller, this handler will catch it and return a 400 Bad Request response with the exception message.
}

```
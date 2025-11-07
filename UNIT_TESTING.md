# VoiceBell Unit Testing Guide

This document provides a comprehensive guide to unit testing in the VoiceBell project.

---

## 📚 Table of Contents

1. [Overview](#overview)
2. [Test Structure](#test-structure)
3. [Running Tests](#running-tests)
4. [Writing Tests](#writing-tests)
5. [Testing Patterns](#testing-patterns)
6. [Tools & Libraries](#tools--libraries)
7. [Best Practices](#best-practices)
8. [Troubleshooting](#troubleshooting)

---

## Overview

### Current Test Status

- **Total Tests**: 310
- **Success Rate**: 100%
- **Build Time**: ~51 seconds (incremental)
- **Coverage**: ~40-45% overall

### Modules Tested

| Module | Tests | Status |
|--------|-------|--------|
| Alarm | 94 | ✅ 100% |
| Timer | 85 | ✅ 100% |
| Settings | 56 | ✅ 100% |
| Stopwatch | 0 | ❌ Not tested |
| World Clock | 0 | ❌ Not tested |

---

## Test Structure

### Directory Layout

```
app/src/test/java/com/voicebell/clock/
├── data/
│   └── repository/
│       ├── AlarmRepositoryImplTest.kt
│       ├── TimerRepositoryImplTest.kt
│       └── SettingsRepositoryImplTest.kt
├── domain/
│   └── usecase/
│       ├── alarm/
│       │   ├── CreateAlarmUseCaseTest.kt
│       │   ├── UpdateAlarmUseCaseTest.kt
│       │   ├── DeleteAlarmUseCaseTest.kt
│       │   ├── GetAlarmByIdUseCaseTest.kt
│       │   ├── ScheduleAlarmUseCaseTest.kt
│       │   ├── ToggleAlarmUseCaseTest.kt
│       │   └── SnoozeAlarmUseCaseTest.kt
│       ├── timer/
│       │   ├── StartTimerUseCaseTest.kt
│       │   ├── StopTimerUseCaseTest.kt
│       │   ├── PauseTimerUseCaseTest.kt
│       │   ├── GetActiveTimerUseCaseTest.kt
│       │   ├── GetTimersUseCaseTest.kt
│       │   └── DeleteTimerUseCaseTest.kt
│       └── settings/
│           ├── GetSettingsUseCaseTest.kt
│           └── UpdateUiModeUseCaseTest.kt
└── presentation/
    └── alarm/
        └── AlarmViewModelTest.kt
```

### Test File Naming

- **Repository Tests**: `{Name}RepositoryImplTest.kt`
- **Use Case Tests**: `{Name}UseCaseTest.kt`
- **ViewModel Tests**: `{Name}ViewModelTest.kt`

---

## Running Tests

### Quick Commands

```bash
# Fast incremental testing (recommended)
./run_tests_incremental.sh

# Full clean build testing
./run_tests_safe.sh

# Run via Gradle
./gradlew testDebugUnitTest

# Run specific test class
./gradlew test --tests "*.CreateAlarmUseCaseTest"

# Run specific test method
./gradlew test --tests "*.CreateAlarmUseCaseTest.invoke should create alarm*"
```

### Test Scripts

#### `run_tests_incremental.sh` (Fast - 51s)
- Uses existing build cache
- No clean build
- Single worker, no parallel execution
- 1.5GB heap, 384MB metaspace
- 5-minute timeout
- **Use for**: Quick iteration during development

#### `run_tests_safe.sh` (Thorough - 2-4 min)
- Clean build before tests
- Ensures no cache pollution
- Same memory limits as incremental
- **Use for**: Verification before commits

### Viewing Results

```bash
# Open HTML report in browser
open app/build/reports/tests/testDebugUnitTest/index.html

# View terminal log
cat test_incremental_run.log

# Count passing tests
grep -h "tests=" app/build/test-results/testDebugUnitTest/*.xml | \
  sed 's/.*tests="\([0-9]*\)".*/\1/' | awk '{sum+=$1} END {print sum}'
```

---

## Writing Tests

### Basic Test Template

```kotlin
package com.voicebell.clock.domain.usecase.{module}

import com.google.common.truth.Truth.assertThat
import com.voicebell.clock.domain.model.{Model}
import com.voicebell.clock.domain.repository.{Repository}
import io.mockk.coEvery
import io.mockk.coVerify
import io.mockk.mockk
import kotlinx.coroutines.test.runTest
import org.junit.Before
import org.junit.Test

/**
 * Unit tests for {ClassName}.
 */
class {ClassName}Test {

    private lateinit var repository: {Repository}
    private lateinit var useCase: {ClassName}

    private val testData = {Model}(
        // ... test data
    )

    @Before
    fun setup() {
        repository = mockk()
        useCase = {ClassName}(repository)
    }

    @Test
    fun `descriptive test name`() = runTest {
        // Given
        coEvery { repository.operation(any()) } returns value

        // When
        val result = useCase(testData)

        // Then
        assertThat(result.isSuccess).isTrue()
        coVerify { repository.operation(testData) }
    }
}
```

### Repository Test Example

```kotlin
@Test
fun `getAllTimers should return mapped domain models`() = runTest {
    // Given
    every { timerDao.getAllTimers() } returns flowOf(listOf(testTimerEntity))

    // When
    val result = repository.getAllTimers()

    // Then
    result.test {
        val timers = awaitItem()
        assertThat(timers).hasSize(1)
        assertThat(timers.first().label).isEqualTo("Test Timer")
        awaitComplete()
    }
}
```

### Use Case Test Example

```kotlin
@Test
fun `invoke should create alarm and calculate next trigger time`() = runTest {
    // Given
    val expectedId = 42L
    coEvery { alarmRepository.createAlarm(any()) } returns expectedId
    coEvery { alarmRepository.updateNextTriggerTime(any(), any()) } returns Unit

    // When
    val result = useCase(testAlarm)

    // Then
    assertThat(result.isSuccess).isTrue()
    assertThat(result.getOrNull()).isEqualTo(expectedId)
    coVerify { alarmRepository.createAlarm(testAlarm) }
    coVerify { alarmRepository.updateNextTriggerTime(expectedId, any()) }
}
```

### Flow Testing Example

```kotlin
@Test
fun `getActiveTimers should return active timers flow`() = runTest {
    // Given
    val activeTimers = listOf(runningTimer, pausedTimer)
    every { timerRepository.getActiveTimers() } returns flowOf(activeTimers)

    // When
    val result = useCase()

    // Then
    result.test {
        val timers = awaitItem()
        assertThat(timers).hasSize(2)
        assertThat(timers).containsExactly(runningTimer, pausedTimer)
        awaitComplete()
    }
}
```

---

## Testing Patterns

### 1. AAA Pattern (Arrange-Act-Assert)

```kotlin
@Test
fun `example test`() = runTest {
    // Arrange (Given) - Setup test data and mocks
    val testData = createTestData()
    coEvery { repository.save(any()) } returns testData.id

    // Act (When) - Execute the code under test
    val result = useCase(testData)

    // Assert (Then) - Verify the results
    assertThat(result.isSuccess).isTrue()
    coVerify { repository.save(testData) }
}
```

### 2. Test Data Builders

```kotlin
private val baseAlarm = Alarm(
    id = 1,
    time = LocalTime.of(7, 0),
    isEnabled = true,
    label = "Test Alarm",
    alarmTone = AlarmTone.DEFAULT,
    repeatDays = emptySet(),
    vibrate = true,
    gradualVolumeIncrease = true,
    snoozeEnabled = true,
    snoozeDuration = 5,
    maxSnoozeCount = 3
)

@Test
fun `test with custom alarm`() = runTest {
    // Create variations easily
    val weekdayAlarm = baseAlarm.copy(
        repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
    )
    // ... test with weekdayAlarm
}
```

### 3. MockK Matchers

```kotlin
// Match any argument
coEvery { repository.save(any()) } returns 1L

// Match specific value
coEvery { repository.save(testAlarm) } returns 1L

// Match with predicate
coVerify {
    repository.save(match { alarm ->
        alarm.isEnabled && alarm.time == LocalTime.of(7, 0)
    })
}

// Match argument count
coVerify(exactly = 3) { repository.save(any()) }
coVerify(atLeast = 1) { repository.save(any()) }
coVerify(exactly = 0) { repository.delete(any()) }
```

### 4. Testing Exceptions

```kotlin
@Test
fun `invoke should fail when repository throws exception`() = runTest {
    // Given
    val exception = RuntimeException("Database error")
    coEvery { repository.save(any()) } throws exception

    // When
    val result = useCase(testData)

    // Then
    assertThat(result.isFailure).isTrue()
    assertThat(result.exceptionOrNull()).isEqualTo(exception)
}
```

### 5. Testing Flow Emissions

```kotlin
@Test
fun `flow should emit multiple updates`() = runTest {
    // Given
    val flow = flowOf(value1, value2, value3)
    every { repository.observe() } returns flow

    // When
    val result = useCase()

    // Then
    result.test {
        assertThat(awaitItem()).isEqualTo(value1)
        assertThat(awaitItem()).isEqualTo(value2)
        assertThat(awaitItem()).isEqualTo(value3)
        awaitComplete()
    }
}
```

### 6. Testing State Transitions

```kotlin
@Test
fun `timer should transition from running to paused`() = runTest {
    // Given - timer starts running
    val runningTimer = testTimer.copy(isRunning = true, isPaused = false)
    coEvery { repository.getTimerById(1) } returns runningTimer
    coEvery { repository.updateTimer(any()) } returns Unit

    // When - pause is called
    val result = pauseUseCase(1)

    // Then - timer transitions to paused
    assertThat(result.isSuccess).isTrue()
    coVerify {
        repository.updateTimer(match { timer ->
            !timer.isRunning && timer.isPaused
        })
    }
}
```

---

## Tools & Libraries

### JUnit 4
- **Purpose**: Test framework
- **Import**: `org.junit.Test`, `org.junit.Before`
- **Usage**: Test annotations and lifecycle

### MockK
- **Purpose**: Mocking library for Kotlin
- **Import**: `io.mockk.*`
- **Usage**: Mock dependencies, stub methods, verify calls
- **Key Functions**:
  - `mockk<T>()` - Create mock
  - `every { ... } returns ...` - Stub method
  - `coEvery { ... } returns ...` - Stub suspend function
  - `verify { ... }` - Verify call
  - `coVerify { ... }` - Verify suspend call

### Truth
- **Purpose**: Fluent assertions
- **Import**: `com.google.common.truth.Truth.assertThat`
- **Usage**: Readable assertions
- **Examples**:
  ```kotlin
  assertThat(result).isTrue()
  assertThat(list).hasSize(3)
  assertThat(list).contains(item)
  assertThat(list).containsExactly(item1, item2)
  assertThat(value).isEqualTo(expected)
  assertThat(value).isNull()
  ```

### Turbine
- **Purpose**: Flow testing
- **Import**: `app.cash.turbine.test`
- **Usage**: Test Flow emissions
- **Examples**:
  ```kotlin
  flow.test {
      assertThat(awaitItem()).isEqualTo(value1)
      assertThat(awaitItem()).isEqualTo(value2)
      awaitComplete()
  }
  ```

### Kotlin Coroutines Test
- **Purpose**: Test coroutines
- **Import**: `kotlinx.coroutines.test.runTest`
- **Usage**: Run suspend tests
- **Example**:
  ```kotlin
  @Test
  fun `test suspend function`() = runTest {
      val result = suspendFunction()
      assertThat(result).isNotNull()
  }
  ```

---

## Best Practices

### 1. Use Descriptive Test Names

✅ **Good:**
```kotlin
@Test
fun `invoke should create alarm and calculate next trigger time`()

@Test
fun `getActiveTimers should return only running and paused timers`()

@Test
fun `updateSettings should fail when repository throws exception`()
```

❌ **Bad:**
```kotlin
@Test
fun testCreate()

@Test
fun test1()

@Test
fun testGetTimers()
```

### 2. Follow AAA Pattern

```kotlin
@Test
fun `example`() = runTest {
    // Given - Setup (clear and concise)
    val testData = createTestData()
    coEvery { repository.save(any()) } returns 1L

    // When - Execute (single action)
    val result = useCase(testData)

    // Then - Verify (specific assertions)
    assertThat(result.isSuccess).isTrue()
    coVerify { repository.save(testData) }
}
```

### 3. Test One Thing Per Test

✅ **Good:**
```kotlin
@Test
fun `create should save to repository`() { /* ... */ }

@Test
fun `create should return ID`() { /* ... */ }

@Test
fun `create should validate input`() { /* ... */ }
```

❌ **Bad:**
```kotlin
@Test
fun `create should save and return ID and validate`() {
    // Testing too many things
}
```

### 4. Use Meaningful Test Data

```kotlin
// Good - clear intent
private val weekdayAlarm = baseAlarm.copy(
    label = "Weekday Morning Alarm",
    time = LocalTime.of(7, 0),
    repeatDays = setOf(DayOfWeek.MONDAY, DayOfWeek.FRIDAY)
)

// Bad - unclear
private val alarm1 = baseAlarm.copy(id = 1)
private val alarm2 = baseAlarm.copy(id = 2)
```

### 5. Test Edge Cases

```kotlin
// Boundary conditions
@Test fun `should handle zero duration`()
@Test fun `should handle negative duration`()
@Test fun `should handle maximum duration`()

// Null/Empty cases
@Test fun `should handle empty list`()
@Test fun `should handle null value`()
@Test fun `should handle empty string`()

// State transitions
@Test fun `should transition from running to paused`()
@Test fun `should transition from paused to finished`()
```

### 6. Don't Mock What You Don't Own

✅ **Mock:**
- Your repositories
- Your use cases
- External services (when testing higher layers)

❌ **Don't Mock:**
- Domain models (use real instances)
- Value objects
- Simple data classes
- Kotlin standard library

### 7. Verify Interactions

```kotlin
@Test
fun `should call repository methods in correct order`() = runTest {
    // Given
    coEvery { repository.create(any()) } returns 1L
    coEvery { repository.updateTriggerTime(any(), any()) } returns Unit

    // When
    useCase(testAlarm)

    // Then - verify order
    coVerify(ordering = Ordering.ORDERED) {
        repository.create(testAlarm)
        repository.updateTriggerTime(1L, any())
    }
}
```

### 8. Keep Tests Fast

- Use `runTest` for coroutines (runs immediately)
- Mock external dependencies
- Don't use `Thread.sleep()` or `delay()`
- Use Turbine's `awaitItem()` instead of waiting

### 9. Make Tests Independent

- Each test should set up its own data
- Don't rely on test execution order
- Use `@Before` to reset state
- Don't share mutable state between tests

### 10. Write Tests First (TDD)

When adding new features:
1. Write failing test
2. Implement feature
3. Verify test passes
4. Refactor

---

## Troubleshooting

### Common Issues

#### 1. Tests Timeout During Build

**Problem**: KSP/Hilt compilation takes too long

**Solution**:
```bash
# Use incremental build
./run_tests_incremental.sh

# Or increase timeout
timeout 600 ./gradlew testDebugUnitTest
```

#### 2. MockK "No Answer Found"

**Problem**: Method called on mock without stub

**Solution**:
```kotlin
// Add missing stub
coEvery { repository.method(any()) } returns value

// Or use relaxed mock (returns defaults)
repository = mockk(relaxed = true)
```

#### 3. Turbine Timeout

**Problem**: Flow doesn't emit expected items

**Solution**:
```kotlin
// Check if Flow completes
flow.test {
    val item = awaitItem()
    // Don't forget awaitComplete() or expectNoEvents()
    awaitComplete()
}
```

#### 4. Compilation Errors

**Problem**: Test code doesn't compile

**Solutions**:
```bash
# Clean and rebuild
./gradlew clean testDebugUnitTest

# Invalidate caches
# In Android Studio: File → Invalidate Caches → Restart
```

#### 5. Tests Pass Locally But Fail in CI

**Problem**: Tests are flaky or order-dependent

**Solution**:
- Make tests independent
- Don't rely on system time (use fixed timestamps)
- Mock all external dependencies
- Avoid shared mutable state

---

## Contributing New Tests

### Checklist

When adding new tests:

- [ ] Follow naming convention (`{Name}Test.kt`)
- [ ] Use descriptive test names with backticks
- [ ] Follow AAA pattern (Given-When-Then)
- [ ] Test happy path
- [ ] Test error cases
- [ ] Test edge cases
- [ ] Mock only external dependencies
- [ ] Verify method calls with `coVerify`
- [ ] Run tests locally before committing
- [ ] Update documentation if needed

### Example PR Description

```markdown
## Add Tests for XYZ Feature

### Tests Added
- `XYZUseCaseTest.kt` (12 tests)
  - Happy path scenarios
  - Error handling
  - Edge cases

### Coverage
- Before: 85%
- After: 92%

### Checklist
- [x] All tests pass locally
- [x] Follows existing patterns
- [x] Descriptive test names
- [x] Updated documentation
```

---

## Resources

### Documentation
- [MockK Documentation](https://mockk.io/)
- [Truth Assertions](https://truth.dev/)
- [Turbine](https://github.com/cashapp/turbine)
- [Kotlin Coroutines Test](https://kotlinlang.org/api/kotlinx.coroutines/kotlinx-coroutines-test/)

### Internal
- [TESTING_SUMMARY.md](./TESTING_SUMMARY.md) - Overall testing status
- [ARCHITECTURE.md](./ARCHITECTURE.md) - Architecture overview
- [CONTRIBUTING.md](./CONTRIBUTING.md) - Contribution guidelines

---

**Last Updated**: 2025-11-07
**Version**: 0.1.5

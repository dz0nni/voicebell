# Contributing to VoiceBell

Thank you for your interest in contributing to VoiceBell! This document provides guidelines and instructions for contributing.

## Table of Contents

- [Code of Conduct](#code-of-conduct)
- [Getting Started](#getting-started)
- [Development Process](#development-process)
- [Coding Standards](#coding-standards)
- [Pull Request Process](#pull-request-process)
- [Reporting Bugs](#reporting-bugs)
- [Suggesting Features](#suggesting-features)
- [Testing](#testing)

## Code of Conduct

### Our Pledge

We are committed to providing a welcoming and inclusive environment for everyone. We expect all contributors to:

- Be respectful and considerate
- Accept constructive criticism gracefully
- Focus on what is best for the project and community
- Show empathy towards other community members

### Our Standards

**Acceptable behavior:**
- Using welcoming and inclusive language
- Being respectful of differing viewpoints and experiences
- Gracefully accepting constructive criticism
- Focusing on what is best for the community

**Unacceptable behavior:**
- Trolling, insulting/derogatory comments, and personal or political attacks
- Public or private harassment
- Publishing others' private information without permission
- Other conduct which could reasonably be considered inappropriate

## Getting Started

### Prerequisites

- **Android Studio**: Arctic Fox (2020.3.1) or newer
- **JDK**: 17 or higher
- **Git**: For version control
- **Physical Android device** or emulator running Android 10+ (for testing)

### Setting Up Development Environment

1. **Fork the repository**
   ```bash
   git clone https://github.com/dz0nni/voicebell.git
   cd voicebell
   ```

2. **Open in Android Studio**
   - File → Open → Select the `voicebell` directory
   - Wait for Gradle sync to complete

3. **Download Vosk model** (for voice recognition testing)
   - Download `vosk-model-small-en-us-0.15` from [Vosk Models](https://alphacephei.com/vosk/models)
   - Place in `app/src/main/assets/models/`

4. **Build the project**
   ```bash
   ./gradlew assembleDebug
   ```

5. **Run tests**
   ```bash
   ./gradlew test
   ```

### Project Structure

```
app/src/main/java/com/voicebell/clock/
├── di/                 # Dependency injection modules
├── data/              # Data layer (repositories, database)
├── domain/            # Business logic (use cases, models)
├── presentation/      # UI layer (Compose, ViewModels)
├── service/           # Android services
├── receiver/          # Broadcast receivers
├── worker/            # Background workers
└── util/              # Utility classes
```

See [ARCHITECTURE.md](ARCHITECTURE.md) for detailed technical documentation.

## Development Process

### Branching Strategy

- `main` - Stable release branch
- `develop` - Development branch (default)
- `feature/feature-name` - New features
- `bugfix/bug-name` - Bug fixes
- `hotfix/critical-fix` - Critical production fixes

### Workflow

1. **Create a branch** from `develop`
   ```bash
   git checkout develop
   git pull origin develop
   git checkout -b feature/your-feature-name
   ```

2. **Make your changes**
   - Write clean, readable code
   - Follow Kotlin coding conventions
   - Add tests for new functionality
   - Update documentation if needed

3. **Commit your changes**
   ```bash
   git add .
   git commit -m "feat: add amazing feature"
   ```

4. **Push to your fork**
   ```bash
   git push origin feature/your-feature-name
   ```

5. **Open a Pull Request** to the `develop` branch

### Commit Message Convention

We follow [Conventional Commits](https://www.conventionalcommits.org/) specification:

```
<type>(<scope>): <subject>

<body>

<footer>
```

**Types:**
- `feat`: New feature
- `fix`: Bug fix
- `docs`: Documentation changes
- `style`: Code style changes (formatting, etc.)
- `refactor`: Code refactoring
- `test`: Adding or updating tests
- `chore`: Maintenance tasks
- `perf`: Performance improvements

**Examples:**
```
feat(alarm): add pre-alarm functionality

Add support for configurable pre-alarms with interval settings.
Users can now set 1-10 pre-alarms before main alarm.

Closes #42
```

```
fix(voice): improve command recognition accuracy

Update Vosk model and refine parsing logic for "set alarm" commands.
```

## Coding Standards

### Kotlin Style Guide

We follow the [official Kotlin coding conventions](https://kotlinlang.org/docs/coding-conventions.html).

**Key points:**

1. **Naming**
   - Classes: `PascalCase`
   - Functions/properties: `camelCase`
   - Constants: `UPPER_SNAKE_CASE`
   - Composables: `PascalCase`

2. **Formatting**
   - Indentation: 4 spaces (no tabs)
   - Line length: 120 characters max
   - Use trailing commas in multi-line declarations

3. **Documentation**
   - Add KDoc for public APIs
   - Comment complex logic
   - Use `//` for single-line comments

**Example:**
```kotlin
/**
 * Schedules an alarm using AlarmManager.
 *
 * @param alarm The alarm to schedule
 * @throws SecurityException if exact alarm permission is not granted
 */
fun scheduleAlarm(alarm: Alarm) {
    // Implementation
}
```

### Jetpack Compose Guidelines

1. **State Management**
   - Hoist state when possible
   - Use `remember` for UI state
   - Use ViewModel for business logic state

2. **Composable Structure**
   ```kotlin
   @Composable
   fun AlarmScreen(
       state: AlarmState,
       onEvent: (AlarmEvent) -> Unit,
       modifier: Modifier = Modifier
   ) {
       // Implementation
   }
   ```

3. **Preview Functions**
   - Add `@Preview` for all major composables
   - Include light and dark theme previews

   ```kotlin
   @Preview(name = "Light")
   @Preview(name = "Dark", uiMode = Configuration.UI_MODE_NIGHT_YES)
   @Composable
   private fun AlarmCardPreview() {
       VoiceBellTheme {
           AlarmCard(/* ... */)
       }
   }
   ```

### Architecture Guidelines

We use **MVI (Model-View-Intent)** + **Clean Architecture**

1. **Data Layer**
   - Repository pattern
   - Room for local storage
   - No business logic

2. **Domain Layer**
   - Use Cases for business logic
   - Pure Kotlin (no Android dependencies)
   - Domain models

3. **Presentation Layer**
   - ViewModels manage state
   - Composables are stateless when possible
   - Events flow from UI to ViewModel
   - State flows from ViewModel to UI

**Example:**
```kotlin
// State
data class AlarmScreenState(
    val alarms: List<Alarm> = emptyList(),
    val isLoading: Boolean = false
)

// Events
sealed class AlarmEvent {
    data class ToggleAlarm(val id: Long) : AlarmEvent()
    data class DeleteAlarm(val id: Long) : AlarmEvent()
}

// ViewModel
class AlarmViewModel @Inject constructor(
    private val getAlarmsUseCase: GetAlarmsUseCase
) : ViewModel() {
    private val _state = MutableStateFlow(AlarmScreenState())
    val state: StateFlow<AlarmScreenState> = _state.asStateFlow()

    fun onEvent(event: AlarmEvent) {
        when (event) {
            is AlarmEvent.ToggleAlarm -> toggleAlarm(event.id)
            is AlarmEvent.DeleteAlarm -> deleteAlarm(event.id)
        }
    }
}
```

## Pull Request Process

### Before Submitting

1. **Run all tests**
   ```bash
   ./gradlew test connectedAndroidTest
   ```

2. **Run lint checks**
   ```bash
   ./gradlew lint
   ```

3. **Format code**
   - Android Studio: Code → Reformat Code (Ctrl+Alt+L)

4. **Update documentation** if you've changed:
   - Public APIs
   - User-facing features
   - Build process
   - Architecture

### PR Checklist

- [ ] Code follows Kotlin style guide
- [ ] All tests pass
- [ ] New tests added for new features
- [ ] Documentation updated
- [ ] Commit messages follow convention
- [ ] No merge conflicts with `develop`
- [ ] Screenshots/videos added for UI changes
- [ ] Privacy implications considered

### PR Description Template

```markdown
## Description
Brief description of changes

## Type of Change
- [ ] Bug fix
- [ ] New feature
- [ ] Breaking change
- [ ] Documentation update

## Testing
How has this been tested?

## Screenshots (if applicable)
Add screenshots for UI changes

## Checklist
- [ ] Tests pass
- [ ] Code follows style guide
- [ ] Documentation updated
```

### Review Process

1. A maintainer will review your PR within 3-5 days
2. Address any requested changes
3. Once approved, a maintainer will merge your PR
4. Your contribution will be included in the next release

## Reporting Bugs

### Before Reporting

1. **Search existing issues** to avoid duplicates
2. **Try latest version** to see if bug is already fixed
3. **Gather information**:
   - Android version
   - Device model
   - VoiceBell version
   - Steps to reproduce
   - Expected vs actual behavior

### Bug Report Template

```markdown
**Describe the bug**
Clear and concise description

**To Reproduce**
Steps to reproduce:
1. Go to '...'
2. Click on '...'
3. See error

**Expected behavior**
What you expected to happen

**Screenshots**
If applicable

**Device Information:**
- Device: [e.g. Pixel 7]
- Android Version: [e.g. 14]
- VoiceBell Version: [e.g. 1.0.0]

**Additional context**
Any other relevant information
```

## Suggesting Features

We welcome feature suggestions! Please:

1. **Check existing issues** to see if feature is already requested
2. **Open a GitHub Discussion** to discuss the idea first
3. **Create a feature request issue** if community agrees

### Feature Request Template

```markdown
**Is your feature request related to a problem?**
Clear description of the problem

**Describe the solution you'd like**
Clear description of desired feature

**Describe alternatives you've considered**
Other solutions or features considered

**Additional context**
Screenshots, mockups, or examples
```

## Testing

### Unit Tests

Located in `app/src/test/`:

```bash
./gradlew test
```

**Write tests for:**
- ViewModels (state management)
- Use Cases (business logic)
- Utilities (parsing, formatting)
- Repository logic

**Example:**
```kotlin
class AlarmViewModelTest {
    @Test
    fun `toggleAlarm updates alarm state`() = runTest {
        // Given
        val viewModel = AlarmViewModel(/* ... */)

        // When
        viewModel.onEvent(AlarmEvent.ToggleAlarm(1))

        // Then
        val state = viewModel.state.value
        assertTrue(state.alarms.first().isEnabled)
    }
}
```

### Instrumentation Tests

Located in `app/src/androidTest/`:

```bash
./gradlew connectedAndroidTest
```

**Test:**
- UI interactions
- Database operations
- Service integrations

### Manual Testing

For features requiring manual testing:

1. **Voice Recognition**: Test in quiet and noisy environments
2. **Alarms**: Test overnight for reliability
3. **Gradual Volume**: Verify volume increases smoothly
4. **Pre-alarms**: Verify timing accuracy

## Additional Resources

- [Android Developers Guide](https://developer.android.com/)
- [Jetpack Compose Documentation](https://developer.android.com/jetpack/compose)
- [Kotlin Documentation](https://kotlinlang.org/docs/home.html)
- [Vosk Documentation](https://alphacephei.com/vosk/)

## Questions?

- **General questions**: [GitHub Discussions](https://github.com/dz0nni/voicebell/discussions)
- **Technical support**: [GitHub Issues](https://github.com/dz0nni/voicebell/issues)

---

Thank you for contributing to VoiceBell!

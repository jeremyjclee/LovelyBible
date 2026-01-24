# 📦 MASTER PLAN: Phase 1 - Project Foundation (Compose Desktop)

**Version**: v2.0.0  
**Feature**: Compose Multiplatform 프로젝트 기반 설정  
**Created**: 2026-01-19  
**Status**: ⚪ Not Started  
**Total Estimated Time**: 2-2.5 hours

---

## 📋 Overview

### Objective
Compose Multiplatform Desktop 프로젝트를 초기화하고 Clean Architecture + MVI 패턴 기반 구조를 설정한다.

### Key Deliverables
- [ ] Gradle 프로젝트 구조 설정 (desktopMain, commonMain)
- [ ] Compose Material3 테마 및 디자인 시스템
- [ ] Koin DI 모듈 설정
- [ ] JUnit5 테스트 환경 구성
- [ ] 40:60 비율의 메인 레이아웃 (Compose Row + weight)

### Architecture Decisions

| 결정 사항 | 선택 | 이유 |
|-----------|------|------|
| **UI Engine** | Compose Multiplatform | Desktop 전용, JVM 네이티브 창 제어 가능 |
| **Architecture** | Clean Architecture + MVI | 단방향 데이터 흐름, 테스트 용이 |
| **DI** | Koin | Kotlin 친화적, 경량 |
| **테마** | Material3 + Custom | 모던 디자인, 다크 테마 기본 |

---

## 🚀 Phase Breakdown

---

### Phase 1.1: Gradle 프로젝트 검증 및 설정 (30분)

**Goal**: 기존 프로젝트 구조를 확인하고 필요한 의존성을 추가한다.

#### 🔴 RED Tasks
- [ ] `src/commonTest/kotlin/SetupTest.kt` 생성
- [ ] 테스트 케이스:
  - [ ] Koin 모듈 로딩 확인
  - [ ] Compose 앱 초기화 확인

#### 🟢 GREEN Tasks
- [ ] `build.gradle.kts` 의존성 확인/추가:
  ```kotlin
  dependencies {
      // Compose
      implementation(compose.desktop.currentOs)
      implementation(compose.material3)
      
      // Koin
      implementation("io.insert-koin:koin-core:3.5.0")
      implementation("io.insert-koin:koin-compose:1.1.0")
      
      // Coroutines
      implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:1.7.3")
      
      // Serialization (JSON)
      implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.6.0")
      
      // Testing
      testImplementation(kotlin("test"))
      testImplementation("io.insert-koin:koin-test:3.5.0")
  }
  ```
- [ ] `./gradlew build` 성공 확인

#### Quality Gate
- [ ] `./gradlew test` 통과
- [ ] `./gradlew run` 빈 창 표시

---

### Phase 1.2: Clean Architecture 디렉토리 구조 (20분)

**Goal**: 레이어별 패키지 구조를 생성한다.

#### Tasks
- [ ] 디렉토리 구조 생성:
  ```
  commonMain/kotlin/
  ├── core/
  │   ├── database/
  │   ├── datastore/
  │   └── display/
  ├── data/
  │   ├── local/
  │   │   ├── entity/
  │   │   └── dao/
  │   └── repository/
  ├── domain/
  │   ├── model/
  │   ├── repository/
  │   └── usecase/
  ├── feature/
  │   ├── main/
  │   ├── search/
  │   ├── navigation/
  │   └── presentation/
  ├── di/
  └── theme/
  ```

#### Quality Gate
- [ ] 모든 디렉토리 생성 완료
- [ ] 빌드 오류 없음

---

### Phase 1.3: Koin DI 모듈 설정 (30분)

**Goal**: 의존성 주입 모듈을 정의한다.

#### 🔴 RED Tasks
- [ ] `di/AppModuleTest.kt` 생성
- [ ] 테스트: Koin 모듈 검증

#### 🟢 GREEN Tasks
- [ ] `di/AppModule.kt` 생성:
  ```kotlin
  val appModule = module {
      // ViewModels
      viewModel { MainViewModel(get()) }
      
      // UseCases
      factory { GetBibleBooksUseCase(get()) }
      
      // Repositories
      single<BibleRepository> { BibleRepositoryImpl(get()) }
      
      // Database
      single { BibleDatabase.create() }
  }
  ```
- [ ] `Main.kt`에서 Koin 초기화:
  ```kotlin
  fun main() = application {
      startKoin {
          modules(appModule)
      }
      Window(onCloseRequest = ::exitApplication) {
          App()
      }
  }
  ```

#### Quality Gate
- [ ] Koin 모듈 로딩 성공
- [ ] `./gradlew test` 통과

---

### Phase 1.4: Material3 테마 및 디자인 시스템 (40분)

**Goal**: 다크 테마 기반 Glassmorphism 스타일 디자인 시스템을 구현한다.

#### 🟢 GREEN Tasks
- [ ] `theme/Color.kt`:
  ```kotlin
  object AppColors {
      val Background = Color(0xFF05070A)
      val CardBackground = Color(0xFF141923).copy(alpha = 0.85f)
      val BorderColor = Color.White.copy(alpha = 0.15f)
      val Accent = Color(0xFF4F46E5)
      val AccentHover = Color(0xFF6366F1)
      val TextPrimary = Color(0xFFF8FAFC)
      val TextSecondary = Color(0xFF94A3B8)
  }
  ```
- [ ] `theme/Theme.kt`:
  ```kotlin
  @Composable
  fun LovelyBibleTheme(content: @Composable () -> Unit) {
      MaterialTheme(
          colorScheme = darkColorScheme(
              primary = AppColors.Accent,
              background = AppColors.Background,
              surface = AppColors.CardBackground
          ),
          typography = Typography,
          content = content
      )
  }
  ```
- [ ] Pretendard 폰트 설정 (resources/font/)

#### Quality Gate
- [ ] 테마 적용된 앱 실행
- [ ] 다크 모드 배경 확인

---

### Phase 1.5: 메인 레이아웃 구현 (40분)

**Goal**: 40:60 비율의 Control/Display 2단 레이아웃을 구현한다.

#### 🔴 RED Tasks
- [ ] UI 테스트: 레이아웃 비율 검증

#### 🟢 GREEN Tasks
- [ ] `feature/main/MainScreen.kt`:
  ```kotlin
  @Composable
  fun MainScreen() {
      Row(
          modifier = Modifier
              .fillMaxSize()
              .background(AppColors.Background)
              .padding(20.dp)
      ) {
          // 왼쪽: 선택 패널 (40%)
          SelectionPanel(
              modifier = Modifier
                  .weight(0.4f)
                  .fillMaxHeight()
          )
          
          Spacer(modifier = Modifier.width(20.dp))
          
          // 오른쪽: 디스플레이 패널 (60%)
          DisplayPanel(
              modifier = Modifier
                  .weight(0.6f)
                  .fillMaxHeight()
          )
      }
  }
  ```
- [ ] `ui/components/Card.kt` (Glassmorphism 카드):
  ```kotlin
  @Composable
  fun GlassCard(
      modifier: Modifier = Modifier,
      content: @Composable () -> Unit
  ) {
      Surface(
          modifier = modifier,
          shape = RoundedCornerShape(16.dp),
          color = AppColors.CardBackground,
          border = BorderStroke(1.dp, AppColors.BorderColor)
      ) {
          content()
      }
  }
  ```

#### Quality Gate
- [ ] `./gradlew run` 레이아웃 확인
- [ ] 40:60 비율 정확히 표시
- [ ] Glassmorphism 카드 효과 확인

---

## 📁 File Changes Summary

| 파일 경로 | 변경 유형 | 설명 |
|-----------|-----------|------|
| `build.gradle.kts` | Modified | 의존성 추가 |
| `di/AppModule.kt` | Created | Koin DI 모듈 |
| `theme/Color.kt` | Created | 색상 정의 |
| `theme/Theme.kt` | Created | Material3 테마 |
| `feature/main/MainScreen.kt` | Created | 메인 레이아웃 |
| `ui/components/Card.kt` | Created | 공통 컴포넌트 |

---

## ✅ Validation Commands

```bash
# 테스트 실행
./gradlew test

# 앱 실행
./gradlew run

# 빌드 검증
./gradlew build
```

---

**Next Step**: Phase 1.1 시작 - Gradle 프로젝트 검증

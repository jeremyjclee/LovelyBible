# 📦 MASTER PLAN: Phase 6 - Build & Distribution

**Version**: v2.0.0  
**Feature**: Compose Desktop 빌드 및 배포  
**Created**: 2026-01-19  
**Status**: ⚪ Not Started  
**Total Estimated Time**: 1.5-2 hours

---

## 📋 Overview

### Objective
Compose Desktop 앱을 Windows용 실행 파일(.exe, .msi)로 패키징하여 배포 가능한 상태로 만든다.

### Key Deliverables
- [ ] Compose Desktop Gradle 설정
- [ ] 앱 아이콘 및 메타데이터
- [ ] Windows Installer (MSI) 생성
- [ ] 빌드 검증 및 테스트

### Architecture Decisions

| 결정 사항 | 선택 | 이유 |
|-----------|------|------|
| **빌드 도구** | Compose Gradle Plugin | 공식 지원, 통합 빌드 |
| **패키징 형식** | MSI + EXE | Windows 표준 설치 |
| **번들링** | JVM Runtime 포함 | 사용자 환경 독립적 |

---

## 🚀 Phase Breakdown

---

### Phase 6.1: Compose Desktop 빌드 설정 (30분)

**Goal**: `build.gradle.kts`에 Desktop 빌드 설정을 추가한다.

#### 🟢 GREEN Tasks
- [ ] `composeApp/build.gradle.kts` 설정:
  ```kotlin
  compose.desktop {
      application {
          mainClass = "MainKt"
          
          nativeDistributions {
              targetFormats(TargetFormat.Msi, TargetFormat.Exe)
              
              packageName = "LovelyBible"
              packageVersion = "1.0.0"
              description = "성경 프레젠테이션 앱"
              copyright = "© 2026 LovelyBible"
              vendor = "LovelyBible Team"
              
              windows {
                  menuGroup = "LovelyBible"
                  shortcut = true
                  dirChooser = true
                  perUserInstall = true
                  iconFile.set(project.file("src/jvmMain/resources/icon.ico"))
              }
          }
      }
  }
  ```
- [ ] JVM 설정 추가:
  ```kotlin
  jvmArgs += listOf(
      "-Dfile.encoding=UTF-8",
      "-Xmx512m"
  )
  ```

#### Quality Gate
- [ ] `./gradlew packageMsi` 명령어 실행 가능

---

### Phase 6.2: 앱 아이콘 및 리소스 (30분)

**Goal**: Windows 앱 아이콘(.ico)을 생성하고 리소스를 준비한다.

#### 🟢 GREEN Tasks
- [ ] 아이콘 파일 준비:
  - `src/jvmMain/resources/icon.ico` (256x256)
  - `src/jvmMain/resources/icon.png` (backup)
- [ ] 수동 생성 또는 온라인 변환 도구 사용
  - RealFaviconGenerator
  - ConvertICO.com
- [ ] 아이콘 파일 배치:
  ```
  src/jvmMain/resources/
  ├── icon.ico
  ├── icon.png
  └── bible/
      └── bible.json
  ```

#### Quality Gate
- [ ] 아이콘 파일 존재 확인
- [ ] `.ico` 형식 유효성

---

### Phase 6.3: 빌드 실행 및 검증 (45분)

**Goal**: 실제 설치 파일을 빌드하고 테스트한다.

#### 🟢 GREEN Tasks
- [ ] 빌드 실행:
  ```bash
  # MSI 설치 파일 생성
  ./gradlew packageMsi
  
  # 또는 EXE 설치 파일
  ./gradlew packageExe
  
  # 디버그용 (설치 없이 실행 가능)
  ./gradlew createDistributable
  ```
- [ ] 빌드 결과물 확인:
  ```
  build/compose/binaries/main/
  ├── msi/
  │   └── LovelyBible-1.0.0.msi
  ├── exe/
  │   └── LovelyBible-1.0.0.exe
  └── app/
      └── LovelyBible/
          └── LovelyBible.exe (실행 파일)
  ```

#### 수동 테스트 체크리스트
- [ ] `.msi` 또는 `.exe` 실행하여 설치
- [ ] 설치 경로 선택 가능 확인
- [ ] 설치 후 앱 실행
- [ ] 듀얼 모니터 PPT 모드 동작 확인
- [ ] 성경 검색 기능 동작 확인
- [ ] 앱 종료 및 재실행 정상

#### Quality Gate
- [ ] 빌드 오류 없음
- [ ] 설치 파일 생성됨
- [ ] 설치 및 실행 성공

---

### Phase 6.4: 문서화 및 릴리즈 준비 (15분)

**Goal**: README 및 릴리즈 노트를 업데이트한다.

#### Tasks
- [ ] `README.md` 업데이트:
  ```markdown
  ## 설치 방법
  
  ### Windows
  1. [Releases](releases) 페이지에서 `LovelyBible-x.x.x.msi` 다운로드
  2. 설치 파일 실행
  3. 설치 완료 후 시작 메뉴에서 실행
  
  ## 빌드 방법
  
  ```bash
  # 개발 모드 실행
  ./gradlew run
  
  # 배포용 빌드
  ./gradlew packageMsi
  ```
  
  ## 시스템 요구사항
  - Windows 10/11
  - 메모리: 512MB 이상
  - 듀얼 모니터 권장 (PPT 모드 사용 시)
  ```
- [ ] 릴리즈 노트 작성 (`docs/r-notes/v1.0.0.md`)
- [ ] `.gitignore` 업데이트:
  ```
  build/
  *.msi
  *.exe
  ```

#### Quality Gate
- [ ] 문서 업데이트 완료
- [ ] Git 커밋 준비 상태

---

## 📁 File Changes Summary

| 파일 경로 | 변경 유형 | 설명 |
|-----------|-----------|------|
| `build.gradle.kts` | Modified | Desktop 빌드 설정 |
| `src/jvmMain/resources/icon.ico` | Created | 앱 아이콘 |
| `README.md` | Modified | 설치/빌드 방법 |
| `.gitignore` | Modified | 빌드 결과물 제외 |
| `docs/r-notes/v1.0.0.md` | Created | 릴리즈 노트 |

---

## ✅ Validation Commands

```bash
# 개발 모드 실행
./gradlew run

# 테스트 실행
./gradlew test

# MSI 빌드
./gradlew packageMsi

# EXE 빌드  
./gradlew packageExe

# 빌드 결과물 경로 (Windows)
dir build\compose\binaries\main\msi
```

---

## 🎯 Architect's Tip

### Windows Defender 경고 대응

번들된 앱은 코드 서명이 없으면 Windows SmartScreen 경고가 표시됩니다.

**해결 방법**:
1. **개발/테스트 단계**: "추가 정보" → "실행" 클릭
2. **배포 단계**: 코드 서명 인증서 구매 및 적용

```kotlin
windows {
    // 코드 서명 (선택, 유료 인증서 필요)
    signPath.set(project.file("path/to/certificate.pfx"))
    signPassword.set(System.getenv("SIGN_PASSWORD"))
}
```

### 빌드 최적화

```kotlin
// ProGuard 설정 (선택)
buildTypes.release {
    proguard {
        configurationFiles.from(project.file("proguard-rules.pro"))
    }
}
```

---

**🎉 모든 Master Plan 완료!**

Phase 1 → Phase 2 → Phase 3 → Phase 4 → Phase 5 → Phase 6 순서로 진행하시면 됩니다.

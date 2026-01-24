# 📦 PLAN: Phase 6 - Electron Build & Distribution

**Version**: v1.0.0  
**Feature**: Electron 빌드 및 배포  
**Created**: 2026-01-19  
**Last Updated**: 2026-01-19  
**Status**: ⚪ Not Started  
**Total Estimated Time**: 1.5-2 hours (Small Scope)

---

**CRITICAL INSTRUCTIONS**: After completing each phase:
1. ✅ Check off completed task checkboxes
2. 🧪 Run all quality gate validation commands
3. ⚠️ Verify ALL quality gate items pass
4. 📅 Update "Last Updated" date
5. 📝 Document learnings in Notes section
6. ➡️ Only then proceed to next phase

⛔ DO NOT skip quality gates or proceed with failing checks

---

## 📋 Overview

### Objective
Electron 앱을 Windows 배포용 .exe 설치 파일로 패키징하여 사용자가 쉽게 설치하고 실행할 수 있도록 한다.

### Key Deliverables
- [ ] electron-builder 설정
- [ ] 앱 아이콘 및 메타데이터
- [ ] NSIS 설치 프로그램 생성
- [ ] 빌드 자동화 스크립트
- [ ] 배포 테스트

### Architecture Decisions

| 결정 사항 | 선택 | 이유 |
|-----------|------|------|
| **빌드 도구** | electron-builder | 가장 널리 사용, 문서화 우수 |
| **패키징 형식** | NSIS Installer | Windows 표준 설치 경험 |
| **아이콘 형식** | ICO (256x256) | Windows 표준 형식 |
| **출력 디렉토리** | dist/ | 빌드 결과물 분리 |

---

## 🚀 Phase Breakdown

---

### Phase 6.1: 빌드 도구 설정 (30분)

**Goal**: electron-builder를 설치하고 기본 빌드 설정을 구성한다.

**Dependencies**: Phase 1-5 완료

#### Test Strategy
- **Test Type**: Configuration validation
- **Test File Location**: `__tests__/build-config.test.js`
- **Coverage Target**: 빌드 설정 검증 100%

#### 🔴 RED Tasks
- [ ] `__tests__/build-config.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] `package.json`에 `build` 필드 존재
  - [ ] `build.appId` 형식 유효성 (`com.xxx.xxx`)
  - [ ] `build.productName` 존재
  - [ ] `build.win.target` 설정 존재

#### 🟢 GREEN Tasks
- [ ] electron-builder 설치:
  ```bash
  npm install electron-builder --save-dev
  ```
- [ ] `package.json`에 빌드 설정 추가:
  ```json
  {
    "build": {
      "appId": "com.lovelybible.app",
      "productName": "Lovely Bible",
      "copyright": "Copyright © 2026 LovelyBible Team",
      "directories": {
        "output": "dist",
        "buildResources": "build"
      },
      "files": [
        "**/*",
        "!docs/**",
        "!__tests__/**",
        "!*.zip"
      ],
      "win": {
        "target": "nsis",
        "icon": "build/icon.ico"
      }
    }
  }
  ```
- [ ] 빌드 스크립트 추가:
  ```json
  {
    "scripts": {
      "build": "electron-builder --win",
      "build:dir": "electron-builder --win --dir"
    }
  }
  ```

#### 🔵 REFACTOR Tasks
- [ ] 불필요한 파일 제외 패턴 최적화
- [ ] 빌드 설정 주석 추가

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] `package.json` 문법 오류 없음
- [ ] electron-builder 설정 유효성 확인

---

### Phase 6.2: 앱 아이콘 및 리소스 준비 (30분)

**Goal**: Windows 앱 아이콘(.ico)을 생성하고 빌드 리소스를 준비한다.

**Dependencies**: Phase 6.1 완료

#### Test Strategy
- **Test Type**: Resource validation
- **Test File Location**: `__tests__/resources.test.js`
- **Coverage Target**: 리소스 파일 존재 확인 100%

#### 🔴 RED Tasks
- [ ] `__tests__/resources.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] `build/` 디렉토리 존재
  - [ ] `build/icon.ico` 파일 존재
  - [ ] 아이콘 파일 크기 > 0
  - [ ] ICO 형식 매직 넘버 검증

#### 🟢 GREEN Tasks
- [ ] `build/` 디렉토리 생성
- [ ] 아이콘 변환 스크립트 (`convert-icon.js`):
  ```javascript
  const sharp = require('sharp');
  const toIco = require('to-ico');
  
  // PNG → ICO 변환
  async function convertToIco() {
      const sizes = [16, 32, 48, 256];
      const buffers = await Promise.all(
          sizes.map(size => 
              sharp('bible-bg.png')
                  .resize(size, size)
                  .png()
                  .toBuffer()
          )
      );
      const ico = await toIco(buffers);
      fs.writeFileSync('build/icon.ico', ico);
  }
  ```
- [ ] 또는 수동으로 ICO 파일 생성
- [ ] `build/icon.png` 백업 이미지 추가

#### 🔵 REFACTOR Tasks
- [ ] 여러 해상도 아이콘 최적화
- [ ] 스플래시 스크린 이미지 (선택)

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] `build/icon.ico` 파일 존재 및 유효
- [ ] Windows 탐색기에서 아이콘 미리보기 확인

---

### Phase 6.3: 빌드 실행 및 검증 (45분)

**Goal**: 실제 .exe 파일을 빌드하고 설치/실행 테스트를 완료한다.

**Dependencies**: Phase 6.2 완료

#### Test Strategy
- **Test Type**: E2E Tests + Manual Tests
- **Test File Location**: `__tests__/build-output.test.js`
- **Coverage Target**: 빌드 결과물 검증 100%

#### 🔴 RED Tasks
- [ ] `__tests__/build-output.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] `dist/` 디렉토리 존재
  - [ ] `.exe` 설치 파일 존재
  - [ ] 설치 파일 크기 > 50MB
  - [ ] `win-unpacked/` 폴더 존재

#### 🟢 GREEN Tasks
- [ ] 빌드 실행:
  ```bash
  npm run build
  ```
- [ ] 빌드 로그 확인 및 오류 해결
- [ ] 생성된 파일 확인:
  - `dist/Lovely Bible Setup X.X.X.exe`
  - `dist/win-unpacked/Lovely Bible.exe`

#### 수동 테스트 체크리스트
- [ ] `.exe` 파일 실행하여 설치 진행
- [ ] 설치 경로 선택 가능 확인
- [ ] 설치 후 앱 실행
- [ ] 듀얼 모니터 PPT 모드 작동 확인
- [ ] 성경 검색 기능 작동 확인
- [ ] 앱 종료 및 재실행 정상 동작

#### 🔵 REFACTOR Tasks
- [ ] `.gitignore`에 `dist/` 추가
- [ ] 빌드 시간 최적화

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] `npm run build` 오류 없이 완료
- [ ] `.exe` 파일 생성 확인
- [ ] 설치 및 실행 테스트 성공

---

### Phase 6.4: 문서화 및 최종 검증 (15분)

**Goal**: README 업데이트 및 최종 검증을 완료한다.

**Dependencies**: Phase 6.3 완료

#### Tasks
- [ ] `README.md` 업데이트:
  - [ ] 설치 방법 섹션
  - [ ] 빌드 방법 섹션
  - [ ] 시스템 요구사항
- [ ] 릴리즈 노트 작성 (`docs/r-notes/v1.0.0.md`)
- [ ] 최종 전체 테스트: `npm test`

#### Quality Gate Checklist
- [ ] 모든 문서 업데이트 완료
- [ ] Git 커밋 준비 상태

---

## 📊 Risk Assessment

| 위험 요소 | 확률 | 영향도 | 완화 전략 |
|-----------|------|--------|-----------|
| electron-builder 설치 오류 | Low | Medium | npm cache clean 후 재시도 |
| 아이콘 형식 호환성 | Low | Low | 여러 형식 준비 |
| 빌드 시간 지연 (>10분) | Medium | Low | 첫 빌드만 오래 걸림 |
| Windows Defender 경고 | Medium | Low | 코드 서명 추후 적용 |

---

## 📁 File Changes Summary

| 파일 경로 | 변경 유형 | 설명 |
|-----------|-----------|------|
| `package.json` | Modified | build 설정, 스크립트 추가 |
| `build/icon.ico` | Created | Windows 앱 아이콘 |
| `build/icon.png` | Created | PNG 백업 아이콘 |
| `__tests__/build-config.test.js` | Created | 빌드 설정 테스트 |
| `__tests__/resources.test.js` | Created | 리소스 파일 테스트 |
| `__tests__/build-output.test.js` | Created | 빌드 결과물 테스트 |
| `dist/*.exe` | Created | 최종 설치 파일 |
| `README.md` | Modified | 설치/빌드 방법 추가 |
| `.gitignore` | Modified | dist/ 제외 추가 |

---

## ✅ Validation Commands

```bash
# 테스트 실행
npm test

# 개발 모드 실행
npm run start

# 프로덕션 빌드
npm run build

# 빌드 결과물 확인 (Windows)
dir dist\
```

---

## 📝 Notes & Learnings

(각 Phase 진행 후 기록)

---

## 📅 Progress Tracking

| Phase | Status | 시작 시간 | 완료 시간 | 소요 시간 |
|-------|--------|-----------|-----------|-----------|
| Phase 6.1 | ⚪ Not Started | - | - | - |
| Phase 6.2 | ⚪ Not Started | - | - | - |
| Phase 6.3 | ⚪ Not Started | - | - | - |
| Phase 6.4 | ⚪ Not Started | - | - | - |

---

**Next Step**: Phase 6.1 시작 - 빌드 도구 설정

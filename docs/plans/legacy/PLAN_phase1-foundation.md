# 📦 PLAN: Phase 1 - Project Foundation & Basic UI

**Version**: v1.0.0  
**Feature**: 프로젝트 초기 설정 및 기본 UI 구조  
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
Electron 기반 성경 프레젠테이션 앱의 기본 프로젝트 구조와 UI 레이아웃을 구축한다.

### Key Deliverables
- [ ] Node.js/Electron 프로젝트 초기화
- [ ] 기본 HTML/CSS 구조 생성
- [ ] Glassmorphism 디자인 시스템 구현
- [ ] 40:60 비율의 메인 레이아웃 완성

### Architecture Decisions

| 결정 사항 | 선택 | 이유 |
|-----------|------|------|
| **프레임워크** | Vanilla JS + Electron | 간단하고 빠른 개발, 외부 의존성 최소화 |
| **스타일링** | CSS Variables + Glassmorphism | 모던하고 세련된 UI, 다크 테마 지원 |
| **폰트** | Pretendard | 한글 가독성 우수, 웹폰트 지원 |
| **레이아웃** | CSS Grid (40:60) | 왼쪽 선택 패널, 오른쪽 디스플레이 패널 |

---

## 🚀 Phase Breakdown

---

### Phase 1.1: 프로젝트 초기화 (20분)

**Goal**: Node.js 프로젝트를 초기화하고 Electron 개발 환경을 구성한다.

**Dependencies**: 없음 (첫 번째 Phase)

#### Test Strategy
- **Test Type**: Configuration validation
- **Test File Location**: `__tests__/setup.test.js`
- **Coverage Target**: 프로젝트 설정 파일 검증 100%

#### 🔴 RED Tasks - 실패하는 테스트 먼저 작성
- [ ] `__tests__/setup.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] `package.json` 존재 확인
  - [ ] `main.js` (Electron main process) 존재 확인
  - [ ] `index.html` 존재 확인
  - [ ] `package.json`에 `main` 필드가 `main.js` 인지 확인
- [ ] 테스트 실행하여 실패 확인

#### 🟢 GREEN Tasks - 테스트 통과를 위한 최소 구현
- [ ] 프로젝트 디렉토리 생성
- [ ] `npm init -y` 실행
- [ ] `npm install electron --save-dev` 실행
- [ ] `npm install jest --save-dev` 실행
- [ ] `package.json` 수정:
  ```json
  {
    "name": "lovely-bible",
    "version": "1.0.0",
    "main": "main.js",
    "scripts": {
      "start": "electron .",
      "test": "jest"
    }
  }
  ```
- [ ] 빈 `main.js` 파일 생성
- [ ] 빈 `index.html` 파일 생성
- [ ] 테스트 재실행하여 통과 확인

#### 🔵 REFACTOR Tasks
- [ ] `.gitignore` 파일 생성 (node_modules, dist 등)
- [ ] README.md 기본 구조 작성

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] `npm run start` Electron 창 열림 (빈 화면이라도)
- [ ] package.json 문법 오류 없음

---

### Phase 1.2: CSS 디자인 시스템 구축 (30분)

**Goal**: Glassmorphism 기반 디자인 시스템과 CSS 변수를 정의한다.

**Dependencies**: Phase 1.1 완료

#### Test Strategy
- **Test Type**: Visual validation (수동 테스트)
- **Coverage Target**: CSS 파일 존재 및 변수 정의 확인

#### 🔴 RED Tasks
- [ ] `__tests__/styles.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] `style.css` 파일 존재 확인
  - [ ] CSS 파일 내 `--bg-color` 변수 정의 확인
  - [ ] CSS 파일 내 `--accent-color` 변수 정의 확인

#### 🟢 GREEN Tasks
- [ ] `style.css` 파일 생성
- [ ] CSS 변수 정의:
  ```css
  :root {
      --bg-color: #05070a;
      --card-bg: rgba(20, 25, 35, 0.85);
      --border-color: rgba(255, 255, 255, 0.15);
      --accent-color: #4f46e5;
      --accent-hover: #6366f1;
      --text-primary: #f8fafc;
      --text-secondary: #94a3b8;
      --glass-blur: blur(12px);
  }
  ```
- [ ] 기본 리셋 스타일 추가
- [ ] body 스타일 적용 (배경 그라디언트, 폰트)
- [ ] `.card` 클래스 스타일 (glassmorphism)

#### 🔵 REFACTOR Tasks
- [ ] 반복되는 스타일 변수화
- [ ] 주석으로 섹션 구분

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] 브라우저에서 index.html 열어 배경 그라디언트 확인
- [ ] Chrome DevTools에서 CSS 변수 적용 확인

---

### Phase 1.3: 메인 레이아웃 구현 (40분)

**Goal**: 40:60 비율의 2단 레이아웃과 기본 UI 컴포넌트를 구현한다.

**Dependencies**: Phase 1.2 완료

#### Test Strategy
- **Test Type**: DOM structure validation
- **Test File Location**: `__tests__/layout.test.js`
- **Coverage Target**: HTML 요소 존재 확인

#### 🔴 RED Tasks
- [ ] `__tests__/layout.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] `.app-container` 요소 존재
  - [ ] `.main-grid` 요소 존재
  - [ ] `.selection-panel` (왼쪽) 요소 존재
  - [ ] `.display-panel` (오른쪽) 요소 존재
  - [ ] `#bookGrid` 요소 존재
  - [ ] `#bibleScreen` 요소 존재

#### 🟢 GREEN Tasks
- [ ] `index.html` 기본 구조 작성:
  ```html
  <!DOCTYPE html>
  <html lang="ko">
  <head>
      <meta charset="UTF-8">
      <title>Lovely Bible</title>
      <link rel="stylesheet" href="style.css">
  </head>
  <body>
      <div class="app-container">
          <main class="main-grid">
              <section class="selection-panel">
                  <div class="card book-selector">
                      <div id="bookGrid" class="book-grid"></div>
                  </div>
              </section>
              <section class="display-panel">
                  <div class="card screen-container">
                      <div id="bibleScreen" class="bible-screen"></div>
                  </div>
              </section>
          </main>
      </div>
  </body>
  </html>
  ```
- [ ] CSS Grid 레이아웃 추가:
  ```css
  .main-grid {
      display: grid;
      grid-template-columns: 4fr 6fr;
      gap: 20px;
  }
  ```
- [ ] 왼쪽/오른쪽 패널 스타일 추가

#### 🔵 REFACTOR Tasks
- [ ] Pretendard 웹폰트 CDN 연결
- [ ] 반응형 고려사항 주석 추가

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] `npm run start` 40:60 레이아웃 확인
- [ ] 카드 컴포넌트에 glassmorphism 효과 적용 확인

---

## 📊 Risk Assessment

| 위험 요소 | 확률 | 영향도 | 완화 전략 |
|-----------|------|--------|-----------|
| Electron 설치 오류 | Low | Medium | npm cache clean 후 재시도 |
| CSS 변수 브라우저 호환성 | Low | Low | 모던 브라우저만 대상 |
| 웹폰트 로딩 지연 | Low | Low | fallback 폰트 지정 |

---

## 📁 File Changes Summary

| 파일 경로 | 변경 유형 | 설명 |
|-----------|-----------|------|
| `package.json` | Created | 프로젝트 설정 |
| `main.js` | Created | Electron 메인 프로세스 |
| `index.html` | Created | 메인 HTML 구조 |
| `style.css` | Created | CSS 디자인 시스템 |
| `__tests__/setup.test.js` | Created | 설정 테스트 |
| `__tests__/styles.test.js` | Created | 스타일 테스트 |
| `__tests__/layout.test.js` | Created | 레이아웃 테스트 |

---

## ✅ Validation Commands

```bash
# 테스트 실행
npm test

# Electron 앱 실행
npm run start
```

---

## 📝 Notes & Learnings

### Phase 1.1 Notes
- (진행 후 기록)

### Phase 1.2 Notes
- (진행 후 기록)

### Phase 1.3 Notes
- (진행 후 기록)

---

## 📅 Progress Tracking

| Phase | Status | 시작 시간 | 완료 시간 | 소요 시간 |
|-------|--------|-----------|-----------|-----------|
| Phase 1.1 | ⚪ Not Started | - | - | - |
| Phase 1.2 | ⚪ Not Started | - | - | - |
| Phase 1.3 | ⚪ Not Started | - | - | - |

---

**Next Step**: Phase 1.1 시작 - 프로젝트 초기화

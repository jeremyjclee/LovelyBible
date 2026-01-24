# 📦 PLAN: Phase 2 - Bible Data Loading & Parsing

**Version**: v1.0.0  
**Feature**: 성경 데이터 로딩 및 파싱 시스템  
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
성경 텍스트 데이터를 로딩하고 파싱하여 JavaScript 객체로 변환하는 시스템을 구축한다.

### Key Deliverables
- [ ] 성경 텍스트 데이터 파일 준비
- [ ] 텍스트 파싱 함수 구현
- [ ] 책/장/절 구조화된 데이터 생성
- [ ] 구약/신약 분리 및 책 목록 관리

### Architecture Decisions

| 결정 사항 | 선택 | 이유 |
|-----------|------|------|
| **데이터 형식** | JavaScript 객체 내장 | 별도 파일 로딩 불필요, 번들 크기 최적화 |
| **파싱 방식** | 정규식 기반 텍스트 파싱 | 구조화된 텍스트에서 책/장/절 추출 |
| **데이터 구조** | `{ 책명: { 장: { 절: 텍스트 } } }` | 빠른 접근, 직관적 구조 |

---

## 🚀 Phase Breakdown

---

### Phase 2.1: 성경 데이터 구조 정의 (30분)

**Goal**: 성경 데이터를 저장할 자료구조와 책 목록을 정의한다.

**Dependencies**: Phase 1 완료

#### Test Strategy
- **Test Type**: Unit Tests
- **Test File Location**: `__tests__/bible-data.test.js`
- **Coverage Target**: 데이터 구조 검증 100%

#### 🔴 RED Tasks
- [ ] `__tests__/bible-data.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] `bibleBooks` 배열이 66권의 책 이름 포함 확인
  - [ ] 구약 39권, 신약 27권 분리 확인
  - [ ] `OT_BOOKS`, `NT_BOOKS` 배열 존재 확인
  - [ ] 각 책 이름이 한글인지 확인

#### 🟢 GREEN Tasks
- [ ] `bible-data.js` 파일 생성
- [ ] 구약 책 목록 정의 (39권):
  ```javascript
  const OT_BOOKS = [
      '창세기', '출애굽기', '레위기', '민수기', '신명기',
      // ... 39권
  ];
  ```
- [ ] 신약 책 목록 정의 (27권):
  ```javascript
  const NT_BOOKS = [
      '마태복음', '마가복음', '누가복음', '요한복음',
      // ... 27권
  ];
  ```
- [ ] 전체 책 목록 배열 생성
- [ ] 테스트 통과 확인

#### 🔵 REFACTOR Tasks
- [ ] 특수 항목 추가 (사도신경 등)
- [ ] 주석으로 책 그룹 구분

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] 구약 39권, 신약 27권 정확히 포함

---

### Phase 2.2: 성경 텍스트 파싱 함수 (40분)

**Goal**: 성경 텍스트를 파싱하여 구조화된 객체로 변환하는 함수를 구현한다.

**Dependencies**: Phase 2.1 완료

#### Test Strategy
- **Test Type**: Unit Tests
- **Test File Location**: `__tests__/bible-parser.test.js`
- **Coverage Target**: 파싱 로직 90%

#### 🔴 RED Tasks
- [ ] `__tests__/bible-parser.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] `parseBible()` 함수 존재 확인
  - [ ] 샘플 텍스트 파싱 시 올바른 구조 반환 확인
  - [ ] `창세기 1:1` 형식 파싱 확인
  - [ ] 빈 텍스트 처리 확인
  - [ ] 잘못된 형식 처리 확인

#### 🟢 GREEN Tasks
- [ ] `parseBible(text)` 함수 구현:
  ```javascript
  function parseBible(text) {
      const result = {};
      const lines = text.split('\n');
      lines.forEach(line => {
          // 정규식으로 "책명 장:절 텍스트" 형식 파싱
          const match = line.match(/^(.+?)\s+(\d+):(\d+)\s+(.+)$/);
          if (match) {
              const [, book, chapter, verse, content] = match;
              // 구조화된 객체에 저장
          }
      });
      return result;
  }
  ```
- [ ] 책/장/절 중첩 객체 생성 로직
- [ ] 테스트 통과 확인

#### 🔵 REFACTOR Tasks
- [ ] 에러 핸들링 추가
- [ ] 로딩 상태 표시 기능

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] 다양한 입력에 대해 올바른 파싱 동작

---

### Phase 2.3: 책 그리드 렌더링 (40분)

**Goal**: 구약/신약 토글과 책 버튼 그리드를 렌더링한다.

**Dependencies**: Phase 2.2 완료

#### Test Strategy
- **Test Type**: Integration Tests (DOM)
- **Test File Location**: `__tests__/book-grid.test.js`
- **Coverage Target**: 렌더링 로직 85%

#### 🔴 RED Tasks
- [ ] `__tests__/book-grid.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] `renderBookGrid()` 함수 존재 확인
  - [ ] 구약 선택 시 39개 버튼 렌더링 확인
  - [ ] 신약 선택 시 27개 버튼 렌더링 확인
  - [ ] 책 버튼 클릭 시 `selectBook()` 호출 확인

#### 🟢 GREEN Tasks
- [ ] `app.js` 파일 생성
- [ ] 구약/신약 토글 버튼 이벤트 핸들러:
  ```javascript
  let currentTestament = 'ot';
  
  otBtn.addEventListener('click', () => {
      currentTestament = 'ot';
      renderBookGrid('ot');
  });
  ```
- [ ] `renderBookGrid(testament)` 함수 구현:
  ```javascript
  function renderBookGrid(testament) {
      const books = testament === 'ot' ? OT_BOOKS : NT_BOOKS;
      bookGrid.innerHTML = books.map(book => 
          `<button class="book-btn">${book}</button>`
      ).join('');
  }
  ```
- [ ] 책 버튼 클릭 이벤트 처리
- [ ] CSS 스타일 적용

#### 🔵 REFACTOR Tasks
- [ ] 활성 책 하이라이트 기능
- [ ] 5열 그리드 레이아웃 최적화

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] `npm run start` 구약/신약 토글 동작 확인
- [ ] 책 버튼 클릭 시 선택 상태 표시 확인

---

## 📊 Risk Assessment

| 위험 요소 | 확률 | 영향도 | 완화 전략 |
|-----------|------|--------|-----------|
| 성경 데이터 인코딩 문제 | Medium | Medium | UTF-8 인코딩 확인 |
| 파싱 정규식 오류 | Medium | High | 다양한 테스트 케이스 |
| 대용량 데이터 렌더링 지연 | Low | Low | 지연 로딩 고려 |

---

## 📁 File Changes Summary

| 파일 경로 | 변경 유형 | 설명 |
|-----------|-----------|------|
| `bible-data.js` | Created | 성경 데이터 및 책 목록 |
| `app.js` | Created | 메인 애플리케이션 로직 |
| `index.html` | Modified | script 태그 추가 |
| `__tests__/bible-data.test.js` | Created | 데이터 구조 테스트 |
| `__tests__/bible-parser.test.js` | Created | 파싱 로직 테스트 |
| `__tests__/book-grid.test.js` | Created | 렌더링 테스트 |

---

## ✅ Validation Commands

```bash
# 테스트 실행
npm test

# Electron 앱 실행
npm run start

# 특정 테스트 파일 실행
npm test -- bible-data.test.js
```

---

## 📝 Notes & Learnings

### Phase 2.1 Notes
- (진행 후 기록)

### Phase 2.2 Notes
- (진행 후 기록)

### Phase 2.3 Notes
- (진행 후 기록)

---

## 📅 Progress Tracking

| Phase | Status | 시작 시간 | 완료 시간 | 소요 시간 |
|-------|--------|-----------|-----------|-----------|
| Phase 2.1 | ⚪ Not Started | - | - | - |
| Phase 2.2 | ⚪ Not Started | - | - | - |
| Phase 2.3 | ⚪ Not Started | - | - | - |

---

**Next Step**: Phase 2.1 시작 - 성경 데이터 구조 정의

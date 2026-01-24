# 📦 PLAN: Phase 4 - Verse Navigation & Pagination

**Version**: v1.0.0  
**Feature**: 성경 구절 내비게이션 및 페이지네이션  
**Created**: 2026-01-19  
**Last Updated**: 2026-01-19  
**Status**: ⚪ Not Started  
**Total Estimated Time**: 2-2.5 hours (Medium Scope)

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
"이전/다음" 버튼으로 성경 전체를 연속적으로 탐색할 수 있는 글로벌 내비게이션 시스템을 구현한다.

### Key Deliverables
- [ ] 현재 위치 추적 (커서) 시스템
- [ ] 이전/다음 내비게이션 버튼
- [ ] 페이지당 절 개수 설정 (1-10절)
- [ ] 장/책 경계 자동 전환
- [ ] 페이지네이션 정보 표시

### Architecture Decisions

| 결정 사항 | 선택 | 이유 |
|-----------|------|------|
| **커서 구조** | `{ book, chapter, verse }` | 현재 위치를 명확하게 표현 |
| **내비게이션** | 글로벌 (전체 성경) | 장/책 경계 자동 전환 |
| **페이지 크기** | 사용자 설정 (1-10) | 프레젠테이션 용도에 맞게 조절 |

---

## 🚀 Phase Breakdown

---

### Phase 4.1: 커서 및 상태 관리 (30분)

**Goal**: 현재 위치(커서)를 추적하고 관리하는 시스템을 구현한다.

**Dependencies**: Phase 2, 3 완료

#### Test Strategy
- **Test Type**: Unit Tests
- **Test File Location**: `__tests__/navigation-state.test.js`
- **Coverage Target**: 상태 관리 로직 95%

#### 🔴 RED Tasks
- [ ] `__tests__/navigation-state.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] `currentCursor` 객체 존재 확인
  - [ ] 유효한 커서 초기화 확인
  - [ ] `setCursor(book, chapter, verse)` 함수 동작
  - [ ] 유효하지 않은 위치에 대한 에러 처리

#### 🟢 GREEN Tasks
- [ ] 커서 상태 변수 정의:
  ```javascript
  let currentCursor = {
      book: '창세기',
      chapter: 1,
      verse: 1
  };
  let linesPerPage = 1; // 페이지당 표시할 절 개수
  ```
- [ ] 커서 설정 함수:
  ```javascript
  function setCursor(book, chapter, verse) {
      if (!isValidPosition(book, chapter, verse)) {
          throw new Error('Invalid position');
      }
      currentCursor = { book, chapter, verse };
  }
  ```
- [ ] 위치 유효성 검사 함수

#### 🔵 REFACTOR Tasks
- [ ] 커서 변경 이벤트 발생 (observer 패턴)
- [ ] 상태 저장/복원 기능

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] 커서 상태 변경 로깅 확인

---

### Phase 4.2: 구절 표시 및 렌더링 (40분)

**Goal**: 현재 커서 위치에서 N개의 구절을 화면에 렌더링한다.

**Dependencies**: Phase 4.1 완료

#### Test Strategy
- **Test Type**: Integration Tests
- **Test File Location**: `__tests__/verse-display.test.js`
- **Coverage Target**: 렌더링 로직 90%

#### 🔴 RED Tasks
- [ ] `__tests__/verse-display.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] `displayCurrentPage()` 함수 존재
  - [ ] 1절 설정 시 1개 구절 표시
  - [ ] 5절 설정 시 5개 구절 표시 (충분한 구절이 있을 때)
  - [ ] 장 끝에서 다음 장으로 연결
  - [ ] 제목 표시 ("창세기 1장")

#### 🟢 GREEN Tasks
- [ ] 구절 수집 함수 (글로벌 네비게이션):
  ```javascript
  function getVersesFromCursor(count) {
      const verses = [];
      let pos = { ...currentCursor };
      
      while (verses.length < count) {
          const verse = getVerse(pos.book, pos.chapter, pos.verse);
          if (!verse) break;
          verses.push({ ...pos, text: verse });
          pos = getNextPosition(pos);
          if (!pos) break;
      }
      return verses;
  }
  ```
- [ ] 화면 렌더링 함수:
  ```javascript
  function renderScreen(verses) {
      bibleScreen.innerHTML = verses.map(v => `
          <div class="verse-row">
              <span class="verse-id">${v.verse}</span>
              <span class="verse-text">${v.text}</span>
          </div>
      `).join('');
  }
  ```
- [ ] 제목 업데이트 로직
- [ ] 페이지 정보 표시 ("1/50 페이지")

#### 🔵 REFACTOR Tasks
- [ ] 구절 스타일 개선
- [ ] 로딩 애니메이션

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] 화면에 구절 올바르게 표시
- [ ] 장 경계 넘어가는 경우 테스트

---

### Phase 4.3: 이전/다음 내비게이션 (45분)

**Goal**: 이전/다음 버튼으로 전체 성경을 탐색할 수 있도록 구현한다.

**Dependencies**: Phase 4.2 완료

#### Test Strategy
- **Test Type**: Integration Tests
- **Test File Location**: `__tests__/navigation.test.js`
- **Coverage Target**: 내비게이션 로직 95%

#### 🔴 RED Tasks
- [ ] `__tests__/navigation.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] `goNext()` 다음 구절로 이동
  - [ ] `goPrev()` 이전 구절로 이동
  - [ ] 장 끝에서 다음 장 첫 절로 이동
  - [ ] 책 끝에서 다음 책 첫 절로 이동
  - [ ] 창세기 1:1에서 이전 버튼 비활성화
  - [ ] 마지막 책 마지막 절에서 다음 버튼 비활성화

#### 🟢 GREEN Tasks
- [ ] 다음 위치 계산 함수:
  ```javascript
  function getNextPosition(pos) {
      // 현재 장의 마지막 절인지 확인
      const maxVerse = getMaxVerse(pos.book, pos.chapter);
      if (pos.verse < maxVerse) {
          return { ...pos, verse: pos.verse + 1 };
      }
      // 현재 책의 마지막 장인지 확인
      const maxChapter = getMaxChapter(pos.book);
      if (pos.chapter < maxChapter) {
          return { book: pos.book, chapter: pos.chapter + 1, verse: 1 };
      }
      // 다음 책의 첫 절
      const nextBook = getNextBook(pos.book);
      if (nextBook) {
          return { book: nextBook, chapter: 1, verse: 1 };
      }
      return null; // 끝
  }
  ```
- [ ] `goNext()` 함수:
  ```javascript
  function goNext() {
      // 마지막 표시된 구절의 다음 위치로 커서 설정
      const verses = getVersesFromCursor(linesPerPage);
      const lastVerse = verses[verses.length - 1];
      const nextPos = getNextPosition(lastVerse);
      if (nextPos) {
          setCursor(nextPos.book, nextPos.chapter, nextPos.verse);
          displayCurrentPage();
      }
  }
  ```
- [ ] `goPrev()` 함수 (역방향 로직)
- [ ] 버튼 활성화/비활성화 상태 관리
- [ ] 이벤트 리스너 연결

#### 🔵 REFACTOR Tasks
- [ ] 키보드 단축키 (← →)
- [ ] 부드러운 전환 애니메이션

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] 창세기 1:1부터 요한계시록 끝까지 내비게이션 가능
- [ ] 이전/다음 버튼 상태 올바르게 변경

---

### Phase 4.4: 페이지 크기 설정 (30분)

**Goal**: 사용자가 페이지당 표시할 절 개수를 설정할 수 있도록 한다.

**Dependencies**: Phase 4.3 완료

#### Test Strategy
- **Test Type**: Integration Tests
- **Test File Location**: `__tests__/page-size.test.js`
- **Coverage Target**: 설정 로직 90%

#### 🔴 RED Tasks
- [ ] `__tests__/page-size.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] 절 개수 버튼 (1-10) 존재
  - [ ] 버튼 클릭 시 `linesPerPage` 변경
  - [ ] 변경 후 화면 즉시 업데이트
  - [ ] 활성 버튼 스타일 변경

#### 🟢 GREEN Tasks
- [ ] 설정 UI 추가:
  ```html
  <div class="card settings-row">
      <div class="line-setting">
          <span class="label">절 개수:</span>
          <div class="line-btn-group">
              <button class="line-btn active" data-val="1">1</button>
              <button class="line-btn" data-val="2">2</button>
              <!-- ... 10까지 -->
          </div>
      </div>
  </div>
  ```
- [ ] 버튼 클릭 이벤트:
  ```javascript
  lineButtons.forEach(btn => {
      btn.addEventListener('click', () => {
          linesPerPage = parseInt(btn.dataset.val);
          updateActiveButton(btn);
          displayCurrentPage();
      });
  });
  ```
- [ ] CSS 스타일 적용

#### 🔵 REFACTOR Tasks
- [ ] 설정 저장 (LocalStorage)
- [ ] 키보드 단축키 (1-0 숫자키)

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] 절 개수 변경 시 화면 즉시 업데이트
- [ ] 활성 버튼 스타일 변경 확인

---

## 📊 Risk Assessment

| 위험 요소 | 확률 | 영향도 | 완화 전략 |
|-----------|------|--------|-----------|
| 경계 조건 오류 | Medium | High | 철저한 경계 테스트 |
| 대량 구절 렌더링 성능 | Low | Medium | 가상 스크롤 고려 |
| 커서 상태 불일치 | Low | Medium | 상태 검증 로직 추가 |

---

## 📁 File Changes Summary

| 파일 경로 | 변경 유형 | 설명 |
|-----------|-----------|------|
| `index.html` | Modified | 내비게이션 UI, 설정 UI 추가 |
| `style.css` | Modified | 버튼 스타일 추가 |
| `app.js` | Modified | 내비게이션 로직 추가 |
| `__tests__/navigation-state.test.js` | Created | 상태 테스트 |
| `__tests__/verse-display.test.js` | Created | 표시 테스트 |
| `__tests__/navigation.test.js` | Created | 내비게이션 테스트 |
| `__tests__/page-size.test.js` | Created | 페이지 크기 테스트 |

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

(각 Phase 진행 후 기록)

---

## 📅 Progress Tracking

| Phase | Status | 시작 시간 | 완료 시간 | 소요 시간 |
|-------|--------|-----------|-----------|-----------|
| Phase 4.1 | ⚪ Not Started | - | - | - |
| Phase 4.2 | ⚪ Not Started | - | - | - |
| Phase 4.3 | ⚪ Not Started | - | - | - |
| Phase 4.4 | ⚪ Not Started | - | - | - |

---

**Next Step**: Phase 4.1 시작 - 커서 및 상태 관리

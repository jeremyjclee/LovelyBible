# 📦 PLAN: Phase 3 - Search Functionality

**Version**: v1.0.0  
**Feature**: 성경 검색 기능 구현  
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
책/장/절 검색 UI와 자동완성 기능을 구현하여 사용자가 원하는 구절을 빠르게 찾을 수 있도록 한다.

### Key Deliverables
- [ ] 3-박스 검색 UI (책/장/절)
- [ ] 책 이름 자동완성 (Autocomplete)
- [ ] Enter/Tab 키 체인 검색
- [ ] 최근 검색 기록 기능

### Architecture Decisions

| 결정 사항 | 선택 | 이유 |
|-----------|------|------|
| **자동완성** | 로컬 필터링 | 빠른 응답, 서버 불필요 |
| **검색 흐름** | Tab/Enter 체인 | 책 → 장 → 절 순차 입력 |
| **최근 검색** | LocalStorage | 브라우저 재시작 시에도 유지 |

---

## 🚀 Phase Breakdown

---

### Phase 3.1: 검색 입력 UI 구현 (30분)

**Goal**: 책/장/절 3개 입력 필드와 기본 스타일을 구현한다.

**Dependencies**: Phase 2 완료

#### Test Strategy
- **Test Type**: DOM validation
- **Test File Location**: `__tests__/search-ui.test.js`
- **Coverage Target**: UI 요소 존재 확인 100%

#### 🔴 RED Tasks
- [ ] `__tests__/search-ui.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] `#searchBook` 입력 필드 존재
  - [ ] `#searchChapter` 입력 필드 존재
  - [ ] `#searchVerse` 입력 필드 존재
  - [ ] `.search-panel` 컨테이너 존재

#### 🟢 GREEN Tasks
- [ ] `index.html`에 검색 패널 추가:
  ```html
  <div class="card search-panel">
      <h3 class="panel-title">검색</h3>
      <div class="search-inputs-row">
          <input type="text" id="searchBook" placeholder="책 (예: 창세기)">
          <input type="number" id="searchChapter" placeholder="장">
          <input type="number" id="searchVerse" placeholder="절">
      </div>
  </div>
  ```
- [ ] `style.css`에 검색 입력 스타일 추가
- [ ] 숫자 입력 필드 스피너 제거 CSS

#### 🔵 REFACTOR Tasks
- [ ] 입력 필드 포커스 스타일 개선
- [ ] 플레이스홀더 스타일 조정

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] `npm run start` 검색 UI 표시 확인
- [ ] 입력 필드에 텍스트 입력 가능 확인

---

### Phase 3.2: 책 이름 자동완성 (45분)

**Goal**: 책 이름 입력 시 일치하는 책 목록을 드롭다운으로 표시한다.

**Dependencies**: Phase 3.1 완료

#### Test Strategy
- **Test Type**: Unit Tests + Integration Tests
- **Test File Location**: `__tests__/autocomplete.test.js`
- **Coverage Target**: 자동완성 로직 85%

#### 🔴 RED Tasks
- [ ] `__tests__/autocomplete.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] "창" 입력 시 "창세기" 제안 표시
  - [ ] "요한" 입력 시 여러 "요한*" 책 제안
  - [ ] 빈 입력 시 제안 숨김
  - [ ] 제안 클릭 시 입력 필드 값 설정
  - [ ] 가장 유사한 항목 자동 선택

#### 🟢 GREEN Tasks
- [ ] 제안 목록 컨테이너 HTML 추가:
  ```html
  <div id="bookSuggestions" class="suggestions"></div>
  ```
- [ ] 자동완성 로직 구현:
  ```javascript
  searchBook.addEventListener('input', (e) => {
      const query = e.target.value.toLowerCase();
      const matches = bibleBooks.filter(b => 
          b.toLowerCase().includes(query)
      );
      renderSuggestions(matches);
  });
  ```
- [ ] 제안 목록 렌더링 함수
- [ ] 제안 클릭 시 선택 처리
- [ ] 외부 클릭 시 제안 닫기

#### 🔵 REFACTOR Tasks
- [ ] 키보드 위/아래 화살표 네비게이션
- [ ] 최대 표시 개수 제한

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] 책 이름 입력 시 제안 목록 표시
- [ ] 제안 클릭 시 입력 필드 업데이트

---

### Phase 3.3: 검색 체인 & 실행 (45분)

**Goal**: Enter/Tab 키로 다음 필드 이동 및 검색 실행을 구현한다.

**Dependencies**: Phase 3.2 완료

#### Test Strategy
- **Test Type**: Integration Tests
- **Test File Location**: `__tests__/search-chain.test.js`
- **Coverage Target**: 키보드 이벤트 처리 90%

#### 🔴 RED Tasks
- [ ] `__tests__/search-chain.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] 책 입력 후 Enter → 장 필드로 포커스 이동
  - [ ] 장 입력 후 Enter → 절 필드로 포커스 이동
  - [ ] 절 입력 후 Enter → 검색 실행
  - [ ] Tab 키도 동일하게 동작 확인

#### 🟢 GREEN Tasks
- [ ] 키보드 이벤트 핸들러 구현:
  ```javascript
  searchBook.addEventListener('keydown', (e) => {
      if (e.key === 'Enter' || e.key === 'Tab') {
          e.preventDefault();
          // 가장 유사한 제안 자동 선택
          if (suggestions.length > 0) {
              selectSuggestion(suggestions[0]);
          }
          searchChapter.focus();
      }
  });
  ```
- [ ] 검색 실행 함수:
  ```javascript
  function executeSearch() {
      const book = searchBook.value;
      const chapter = parseInt(searchChapter.value);
      const verse = parseInt(searchVerse.value);
      // 해당 구절로 이동
      goToVerse(book, chapter, verse);
  }
  ```
- [ ] 유효성 검사 (존재하는 책/장/절 확인)

#### 🔵 REFACTOR Tasks
- [ ] 잘못된 입력 시 피드백 표시
- [ ] 검색 애니메이션 효과

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] Enter/Tab 키 체인 완전히 동작
- [ ] 검색 후 해당 구절 표시 확인

---

### Phase 3.4: 최근 검색 기록 (30분)

**Goal**: 최근 검색한 구절 목록을 저장하고 표시한다.

**Dependencies**: Phase 3.3 완료

#### Test Strategy
- **Test Type**: Unit Tests + Integration Tests
- **Test File Location**: `__tests__/recent-search.test.js`
- **Coverage Target**: 저장/로딩 로직 90%

#### 🔴 RED Tasks
- [ ] `__tests__/recent-search.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] 검색 시 기록에 추가
  - [ ] 최대 15개 유지
  - [ ] 중복 제거 (최신이 위로)
  - [ ] 기록 클릭 시 해당 구절 검색
  - [ ] LocalStorage 저장/로딩

#### 🟢 GREEN Tasks
- [ ] 최근 검색 UI 추가:
  ```html
  <div class="recent-search-row">
      <span class="recent-search-title">최근 검색</span>
      <div id="recentSearchList" class="recent-search-list"></div>
  </div>
  ```
- [ ] 최근 검색 저장 함수:
  ```javascript
  function addRecentSearch(book, chapter, verse) {
      let recent = JSON.parse(localStorage.getItem('recentSearches') || '[]');
      const entry = { book, chapter, verse };
      // 중복 제거 후 맨 앞에 추가
      recent = recent.filter(r => !(r.book === book && r.chapter === chapter && r.verse === verse));
      recent.unshift(entry);
      recent = recent.slice(0, 15);
      localStorage.setItem('recentSearches', JSON.stringify(recent));
      renderRecentSearches();
  }
  ```
- [ ] 최근 검색 렌더링 함수
- [ ] 앱 시작 시 최근 검색 로딩

#### 🔵 REFACTOR Tasks
- [ ] "창세기 1장 1절" 형식으로 표시
- [ ] 2줄 제한 레이아웃

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] 검색 시 기록 추가 확인
- [ ] 앱 재시작 후 기록 유지 확인

---

## 📊 Risk Assessment

| 위험 요소 | 확률 | 영향도 | 완화 전략 |
|-----------|------|--------|-----------|
| 자동완성 성능 저하 | Low | Low | 디바운스 적용 |
| LocalStorage 용량 초과 | Low | Low | 항목 수 제한 |
| 키보드 이벤트 충돌 | Medium | Medium | preventDefault 신중하게 사용 |

---

## 📁 File Changes Summary

| 파일 경로 | 변경 유형 | 설명 |
|-----------|-----------|------|
| `index.html` | Modified | 검색 UI 추가 |
| `style.css` | Modified | 검색 스타일 추가 |
| `app.js` | Modified | 검색 로직 추가 |
| `__tests__/search-ui.test.js` | Created | UI 테스트 |
| `__tests__/autocomplete.test.js` | Created | 자동완성 테스트 |
| `__tests__/search-chain.test.js` | Created | 검색 체인 테스트 |
| `__tests__/recent-search.test.js` | Created | 최근 검색 테스트 |

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
| Phase 3.1 | ⚪ Not Started | - | - | - |
| Phase 3.2 | ⚪ Not Started | - | - | - |
| Phase 3.3 | ⚪ Not Started | - | - | - |
| Phase 3.4 | ⚪ Not Started | - | - | - |

---

**Next Step**: Phase 3.1 시작 - 검색 입력 UI 구현

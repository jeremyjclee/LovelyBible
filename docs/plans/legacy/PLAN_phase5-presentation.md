# 📦 PLAN: Phase 5 - Dual Monitor Presentation Mode

**Version**: v1.0.0  
**Feature**: 듀얼 모니터 프레젠테이션 모드  
**Created**: 2026-01-19  
**Last Updated**: 2026-01-19  
**Status**: ⚪ Not Started  
**Total Estimated Time**: 2.5-3 hours (Medium-Large Scope)

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
외부 모니터에 전체화면 프레젠테이션을 표시하고, 메인 화면에서 컨트롤할 수 있는 듀얼 모니터 PPT 모드를 구현한다.

### Key Deliverables
- [ ] Electron IPC 기반 창간 통신
- [ ] 외부 모니터 자동 감지
- [ ] 전체화면 프레젠테이션 창
- [ ] 실시간 구절 동기화
- [ ] 웹 브라우저 폴백 모드

### Architecture Decisions

| 결정 사항 | 선택 | 이유 |
|-----------|------|------|
| **창 관리** | Electron BrowserWindow | 외부 모니터 정밀 제어 가능 |
| **통신 방식** | IPC (ipcMain/ipcRenderer) | 안전하고 비동기 통신 |
| **폴백** | 로컬 전체화면 오버레이 | 단일 모니터 환경 지원 |
| **프레젠테이션 스타일** | 별도 presentation.html | 독립적인 디자인 커스터마이징 |

---

## 🚀 Phase Breakdown

---

### Phase 5.1: Electron IPC 설정 (40분)

**Goal**: Electron 메인-렌더러 프로세스 간 IPC 통신을 설정한다.

**Dependencies**: Phase 1-4 완료

#### Test Strategy
- **Test Type**: Integration Tests
- **Test File Location**: `__tests__/electron-ipc.test.js`
- **Coverage Target**: IPC 채널 정의 100%

#### 🔴 RED Tasks
- [ ] `__tests__/electron-ipc.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] `preload.js` 파일 존재 확인
  - [ ] `window.electronAPI` 객체 노출 확인
  - [ ] `toggle-presentation` 채널 정의 확인
  - [ ] `update-presentation` 채널 정의 확인

#### 🟢 GREEN Tasks
- [ ] `preload.js` 파일 생성:
  ```javascript
  const { contextBridge, ipcRenderer } = require('electron');
  
  contextBridge.exposeInMainWorld('electronAPI', {
      isElectron: true,
      togglePresentation: () => ipcRenderer.send('toggle-presentation'),
      updatePresentation: (data) => ipcRenderer.send('update-presentation', data),
      onPresentationStatus: (callback) => 
          ipcRenderer.on('presentation-status', (_, data) => callback(data)),
      onPresentationClosed: (callback) =>
          ipcRenderer.on('presentation-closed', callback),
      getDisplayInfo: () => ipcRenderer.invoke('get-display-info')
  });
  ```
- [ ] `main.js`에 IPC 핸들러 추가:
  ```javascript
  ipcMain.on('toggle-presentation', (event) => {
      // 발표 창 토글 로직
  });
  
  ipcMain.on('update-presentation', (event, verseData) => {
      // 발표 창에 데이터 전송
  });
  
  ipcMain.handle('get-display-info', () => {
      // 디스플레이 정보 반환
  });
  ```
- [ ] BrowserWindow 설정에 preload 경로 추가

#### 🔵 REFACTOR Tasks
- [ ] 에러 핸들링 추가
- [ ] 연결 상태 로깅

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] `npm run start` Electron 창 정상 실행
- [ ] DevTools에서 `window.electronAPI` 객체 확인

---

### Phase 5.2: 외부 모니터 감지 (30분)

**Goal**: 연결된 외부 모니터를 자동으로 감지하고 정보를 제공한다.

**Dependencies**: Phase 5.1 완료

#### Test Strategy
- **Test Type**: Integration Tests
- **Test File Location**: `__tests__/display-detection.test.js`
- **Coverage Target**: 감지 로직 90%

#### 🔴 RED Tasks
- [ ] `__tests__/display-detection.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] 연결된 디스플레이 수 반환
  - [ ] 외부 디스플레이 존재 여부 확인
  - [ ] 외부 디스플레이 좌표(bounds) 정보

#### 🟢 GREEN Tasks
- [ ] 디스플레이 감지 로직:
  ```javascript
  function getExternalDisplay() {
      const displays = screen.getAllDisplays();
      return displays.find((display) => {
          return display.bounds.x !== 0 || display.bounds.y !== 0;
      });
  }
  ```
- [ ] 디스플레이 정보 API:
  ```javascript
  ipcMain.handle('get-display-info', () => {
      const allDisplays = screen.getAllDisplays();
      const hasExternal = allDisplays.some(d => 
          d.bounds.x !== 0 || d.bounds.y !== 0
      );
      return {
          displayCount: allDisplays.length,
          hasExternalMonitor: hasExternal,
          isPresentationOpen: secondaryWindow && !secondaryWindow.isDestroyed()
      };
  });
  ```
- [ ] 앱 시작 시 자동 감지

#### 🔵 REFACTOR Tasks
- [ ] 디스플레이 변경 감지 이벤트
- [ ] UI에 모니터 상태 표시

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] 외부 모니터 연결/해제 시 정확한 감지

---

### Phase 5.3: 프레젠테이션 창 구현 (50분)

**Goal**: 외부 모니터에 전체화면 프레젠테이션 창을 생성하고 표시한다.

**Dependencies**: Phase 5.2 완료

#### Test Strategy
- **Test Type**: Integration Tests + Manual Tests
- **Test File Location**: `__tests__/presentation-window.test.js`
- **Coverage Target**: 창 생성 로직 85%

#### 🔴 RED Tasks
- [ ] `__tests__/presentation-window.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] `presentation.html` 파일 존재
  - [ ] 프레젠테이션 창 생성 함수 존재
  - [ ] 창이 외부 모니터 좌표에 생성
  - [ ] 전체화면 모드 활성화

#### 🟢 GREEN Tasks
- [ ] `presentation.html` 생성:
  ```html
  <!DOCTYPE html>
  <html lang="ko">
  <head>
      <title>Lovely Bible - Presentation</title>
      <style>
          /* 프레젠테이션 전용 스타일 */
          body {
              background: linear-gradient(...);
              display: flex;
              align-items: center;
              justify-content: center;
          }
          .verse-container { ... }
      </style>
  </head>
  <body>
      <div id="presentationContent"></div>
      <script src="preload.js"></script>
      <script>
          window.electronAPI.onDisplayVerses((data) => {
              renderVerses(data);
          });
      </script>
  </body>
  </html>
  ```
- [ ] 프레젠테이션 창 생성 함수:
  ```javascript
  function createSecondaryWindow(externalDisplay) {
      secondaryWindow = new BrowserWindow({
          x: externalDisplay.bounds.x,
          y: externalDisplay.bounds.y,
          width: externalDisplay.bounds.width,
          height: externalDisplay.bounds.height,
          fullscreen: true,
          frame: false,
          webPreferences: {
              preload: path.join(__dirname, 'preload.js')
          }
      });
      secondaryWindow.loadFile('presentation.html');
  }
  ```
- [ ] ESC 키로 창 닫기

#### 🔵 REFACTOR Tasks
- [ ] 부드러운 페이드 애니메이션
- [ ] 배경 이미지 설정

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] 외부 모니터에 전체화면 창 표시
- [ ] ESC 키로 창 닫기 동작

---

### Phase 5.4: 실시간 구절 동기화 (40분)

**Goal**: 메인 창에서 구절 변경 시 프레젠테이션 창에 실시간 동기화한다.

**Dependencies**: Phase 5.3 완료

#### Test Strategy
- **Test Type**: Integration Tests
- **Test File Location**: `__tests__/verse-sync.test.js`
- **Coverage Target**: 동기화 로직 90%

#### 🔴 RED Tasks
- [ ] `__tests__/verse-sync.test.js` 생성
- [ ] 테스트 케이스 작성:
  - [ ] 구절 변경 시 IPC 메시지 전송
  - [ ] 프레젠테이션 창에서 메시지 수신
  - [ ] 화면 업데이트 확인

#### 🟢 GREEN Tasks
- [ ] 메인 창에서 업데이트 전송:
  ```javascript
  function updatePresentationWindow() {
      if (isElectron && isElectronPresentationOpen) {
          const verses = getVersesFromCursor(linesPerPage);
          const title = getCurrentTitle();
          window.electronAPI.updatePresentation({
              verses,
              title,
              paginationInfo: getPaginationInfo()
          });
      }
  }
  ```
- [ ] `displayCurrentPage()` 호출 시 자동 동기화
- [ ] 프레젠테이션 창 렌더링 로직
- [ ] 제목 및 구절 스타일링

#### 🔵 REFACTOR Tasks
- [ ] 전환 애니메이션
- [ ] 폰트 크기 자동 조절

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] 이전/다음 버튼 클릭 시 프레젠테이션 동기화
- [ ] 검색 시 프레젠테이션 동기화

---

### Phase 5.5: 웹 브라우저 폴백 모드 (30분)

**Goal**: 단일 모니터 또는 웹 브라우저 환경을 위한 로컬 전체화면 오버레이를 구현한다.

**Dependencies**: Phase 5.4 완료

#### Test Strategy
- **Test Type**: Integration Tests + Manual Tests
- **Coverage Target**: 폴백 로직 85%

#### 🔴 RED Tasks
- [ ] 테스트 케이스 작성:
  - [ ] Electron이 아닌 환경에서 폴백 모드 동작
  - [ ] 로컬 오버레이 전체화면 동작
  - [ ] 키보드 내비게이션 동작

#### 🟢 GREEN Tasks
- [ ] 로컬 전체화면 오버레이:
  ```javascript
  function createLocalFullscreenOverlay() {
      presentationOverlay = document.createElement('div');
      presentationOverlay.id = 'presentationOverlay';
      presentationOverlay.innerHTML = `
          <div class="ppt-content">
              <div class="ppt-title"></div>
              <div class="ppt-verses"></div>
          </div>
      `;
      document.body.appendChild(presentationOverlay);
      presentationOverlay.requestFullscreen();
  }
  ```
- [ ] 전체화면 해제 감지
- [ ] 키보드 이벤트 (← → ESC)
- [ ] 터치/마우스 제스처

#### 🔵 REFACTOR Tasks
- [ ] Window Management API 지원 (브라우저)
- [ ] 두 번째 모니터 팝업 시도

#### Quality Gate Checklist
- [ ] `npm test` 모든 테스트 통과
- [ ] 웹 브라우저에서 전체화면 모드 동작
- [ ] 키보드로 구절 이동 가능

---

## 📊 Risk Assessment

| 위험 요소 | 확률 | 영향도 | 완화 전략 |
|-----------|------|--------|-----------|
| IPC 통신 지연 | Low | Medium | 비동기 처리, 디바운스 |
| 외부 모니터 미감지 | Medium | Medium | 수동 새로고침 옵션 |
| 전체화면 권한 문제 | Low | Low | 사용자 인터랙션 필수 |
| 브라우저 호환성 | Medium | Medium | 폴백 모드 제공 |

---

## 📁 File Changes Summary

| 파일 경로 | 변경 유형 | 설명 |
|-----------|-----------|------|
| `main.js` | Modified | IPC 핸들러, 창 관리 로직 |
| `preload.js` | Created | contextBridge API 노출 |
| `presentation.html` | Created | 프레젠테이션 전용 페이지 |
| `app.js` | Modified | 프레젠테이션 동기화 로직 |
| `style.css` | Modified | 오버레이 스타일 |
| `__tests__/electron-ipc.test.js` | Created | IPC 테스트 |
| `__tests__/display-detection.test.js` | Created | 디스플레이 감지 테스트 |
| `__tests__/presentation-window.test.js` | Created | 창 생성 테스트 |
| `__tests__/verse-sync.test.js` | Created | 동기화 테스트 |

---

## ✅ Validation Commands

```bash
# 테스트 실행
npm test

# Electron 앱 실행 (듀얼 모니터 테스트)
npm run start
```

---

## 📝 Notes & Learnings

(각 Phase 진행 후 기록)

---

## 📅 Progress Tracking

| Phase | Status | 시작 시간 | 완료 시간 | 소요 시간 |
|-------|--------|-----------|-----------|-----------|
| Phase 5.1 | ⚪ Not Started | - | - | - |
| Phase 5.2 | ⚪ Not Started | - | - | - |
| Phase 5.3 | ⚪ Not Started | - | - | - |
| Phase 5.4 | ⚪ Not Started | - | - | - |
| Phase 5.5 | ⚪ Not Started | - | - | - |

---

**Next Step**: Phase 5.1 시작 - Electron IPC 설정

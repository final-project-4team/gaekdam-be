#  객담 – 호텔 고객관리 crm

<br>

<div align="center">
<!-- <img width="600" height="500" alt="jimwhere_" src="https://github.com/user-attachments/assets/47da31df-2438-41a1-af7a-ead4676c30bb" /> -->
</div>


---
## 🤣  목차 (Table of Contents)

### [👩‍👧‍👦 멤버 소개](#1-멤버-소개)  
### [🖼️ 프로젝트 개요](#2-프로젝트-개요)
### [🚀 주요 기능 요약](#3-주요기능-요약)
### [🛠️ Tech Stack](#4-tech-stack)  
### [🗂️ 프로젝트 산출물](#5-프로젝트-산출물)
### [🤝 팀원 회고](#6-팀원-회고)
### [⚠️ Trouble Shooting](#7-trouble-shooting)
---



## 👩‍👧‍👦 1. 멤버 소개

<div align="center">

| 김성태 | 김상재 | 김성현 | 박인수 |
|--------|--------|--------|--------|
|<img width="150" height="150" src = "https://github.com/user-attachments/assets/699e02b7-546d-4181-a31b-71ae0502e458">|<img width="150" height="150" src = "https://github.com/user-attachments/assets/b9e64560-6418-426c-b4f3-0c4d5fa829f1">|<img width="150" height="150" src = "https://github.com/user-attachments/assets/2a199074-db5a-4ee7-a638-f322fd9843ca">|<img width="150" height="150" src = "https://github.com/user-attachments/assets/a7c3529e-983a-4233-8f26-bebd8e4784d3">|

</div>

## 🖼️ 2. **프로젝트 개요**
#### 본 프로젝트는 호텔 산업에 특화된 CRM입니다. 범용 CRM의 불필요한 기능은 과감히 배제하고, 리포트 기능을 통해 핵심 KPI와 주요 지표를 한눈에 파악할 수 있는 직관적인 대시보드를 구현했습니다. 또한 고객 여정 기반 메시징 등 필수 기능에 집중했으며, 특히 AI 에이전트를 도입하여 직원의 업무 숙련도의 편차를 보완하고 고객 응대 품질을 획기적으로 높였습니다

<br>

## 🚀 3. **주요 기능 요약**

🧭 리포트

- 레이아웃과 템플릿을 조합해 맞춤형 대시보드를 구성하고 관리<br>
- 월간 연간 단위기간 기준으로 KPI를 요약하고 목표 대비 증감을 함께 제공<br>
- 대시보드 데이터를 CSV로 추출하고 레이아웃을 PDF로 내보내기 지원<br>

👤 고객관리

- 고객 목록과 상세에서 이름 연락처 검색, 상태 필터로 필요한 고객을 빠르게 조회<br>
- LTV, 투숙일, 전년 대비 트렌드, 시즌 선호, 선호 객실 부대시설, 지출 카테고리 등 인사이트로 고객을 정밀 분석<br>
- 연락처 대표 지정, 마케팅 동의 조회, 고객 카드와 화면 구성을 커스텀 설정해 업무 편의성을 강화<br>

📌 고객 활동

- 예약을 상태별로 통합 조회하고 지점 상태 필터와 검색을 지원<br>
- 오늘 체크인 체크아웃 투숙 현황을 제공하고 체크인 체크아웃 처리를 지원<br>
- 부대시설 이용과 고객 이벤트를 타임라인으로 묶어 통합 조회<br>

🗣️ 고객의 소리

- 외부 VOC 문의를 목록 상세로 조회하고 상태 유형 필터를 제공<br>
- 문의 상태 유형 변경을 외부 시스템과 동기화해 일관되게 반영<br>
- 사건사고 보고서를 작성하고 조치 내역 누적과 책임자 지정을 지원<br>

✉️ 메세지

- 예약 체크인 체크아웃 전후 흐름으로 고객 여정 단계를 자동 구분<br>
- 여정 단계에 맞는 메시지를 발송하고 중복 발송을 방지<br>
- 템플릿과 발송 규칙을 관리해 호텔별 메시지 운영을 표준화<br>

⚙️ 세팅

- 권한 역할을 기능 단위로 구성하고 변경 이력을 기록<br>
- 부서 직책 사용자 계정을 운영 구조에 맞게 관리<br>
- 목표 항목과 값을 설정하고 현재 데이터 기반 달성 여부를 계산<br>

🔒 보안 정책

- 로그인 실패 제한과 휴면 전환으로 계정 보안을 강화<br>
- 개인정보 마스킹 기준과 데이터 보존기간을 설정<br>
- 접근 권한 검증과 로그로 운영 추적성을 확보<br>

🤖 AI

- 고객 상세 컨텍스트 기반으로 응대 가이드를 제공<br>
- 민감 개인정보 응답을 차단해 정보 유출을 예방<br>
- 매뉴얼 기반 응대 문구를 생성하고 유사 사례로 보완<br>

<br>

## 🛠️ 4. **Tech Stack**

#Database
<br>
![MariaDB](https://img.shields.io/badge/MariaDB-003545?style=for-the-badge&logo=mariadb&logoColor=white)
![Redis](https://img.shields.io/badge/Redis-DC382D?style=for-the-badge&logo=redis&logoColor=white)

#Backend
<br>
![Java 21](https://img.shields.io/badge/Java%2021-007396?style=for-the-badge&logo=openjdk&logoColor=white)
![Spring Boot Badge](https://img.shields.io/badge/Spring%20Boot-6DB33F?logo=springboot&logoColor=fff&style=for-the-badge)
![Spring Security Badge](https://img.shields.io/badge/Spring%20Security-6DB33F?logo=springsecurity&logoColor=fff&style=for-the-badge)
![Spring Data JPA](https://img.shields.io/badge/Spring%20Data%20JPA-59666C?style=for-the-badge&logo=hibernate&logoColor=white)
![JWT](https://img.shields.io/badge/JWT-black?style=for-the-badge&logo=jsonwebtokens&logoColor=white)
![JUnit5 Badge](https://img.shields.io/badge/JUnit5-25A162?logo=junit5&logoColor=fff&style=for-the-badge)
![Python](https://img.shields.io/badge/Python-3776AB?style=for-the-badge&logo=python&logoColor=white)
![FastAPI](https://img.shields.io/badge/FastAPI-009688?style=for-the-badge&logo=fastapi&logoColor=white)

#Frontend
<br>
![Vue.js Badge](https://img.shields.io/badge/Vue.js-4FC08D?logo=vuedotjs&logoColor=fff&style=for-the-badge)
![HTML5](https://img.shields.io/badge/HTML5-E34F26?style=for-the-badge&logo=html5&logoColor=white)
![CSS3](https://img.shields.io/badge/CSS3-1572B6?style=for-the-badge&logo=css3&logoColor=white)
![Pinia Badge](https://img.shields.io/badge/Pinia-FFD859?logo=pinia&logoColor=000&style=for-the-badge)
![Axios Badge](https://img.shields.io/badge/Axios-5A29E4?logo=axios&logoColor=fff&style=for-the-badge)
![Chart.js](https://img.shields.io/badge/Chart.js-FF6384?style=for-the-badge&logo=chartdotjs&logoColor=white)

#API Platform
<br>

![Postman](https://img.shields.io/badge/Postman-FF6C37?style=for-the-badge&logo=postman&logoColor=white)
![Swagger UI](https://img.shields.io/badge/Swagger-85EA2D?style=for-the-badge&logo=swagger&logoColor=black )


#Tools&External References
<br>
![GitHub](https://img.shields.io/badge/GitHub-181717?style=for-the-badge&logo=github&logoColor=white)
![Notion](https://img.shields.io/badge/Notion-000000?style=for-the-badge&logo=notion&logoColor=white)
![Figma](https://img.shields.io/badge/Figma-F24E1E?style=for-the-badge&logo=figma&logoColor=white)
<img src="https://img.shields.io/badge/Discord-5865F2?style=for-the-badge&logo=Discord&logoColor=white">
<a href="https://www.erdcloud.com/" target="_blank"> <img src="https://img.shields.io/badge/ERD%20Cloud-4285F4?style=for-the-badge&logo=googlecloud&logoColor=white"/> </a>

<br>

## 📂 5. **프로젝트 산출물**

<details>
  <summary><h3 style="display: inline-block;">프로젝트 기획서</h3></summary>
    
  <br>[프로젝트 기획서 바로가기](https://github.com/final-project-4team/gaekdam-be/wiki/프로젝트-기획서)

  </details> 

<details>
  <summary><h3 style="display: inline-block;">요구사항 명세서</h3></summary>
  <br>[바로가기](https://docs.google.com/spreadsheets/d/18o3ChA9HKGxNwZcl9T_01co17eDQA2Mkc36GwEzm_v4/edit?gid=2015108767#gid=2015108767)

  </details> 
  <br>
  
<details>
  <summary><h3 style="display: inline-block;">WBS</h3></summary>
  
  [WBS](https://github.com/final-project-4team/gaekdam-be/wiki/WBS)
</details>
  <br>
    
<details>
  <summary><h3 style="display: inline-block;">ERD</h3></summary>
    [바로가기](https://www.erdcloud.com/d/28ycP85iBgnF2iSQq)
    <img width="1312" height="882" alt="image" src="https://github.com/user-attachments/assets/4c1bc569-5719-4d19-97e0-dd39e64a1469" />

  </details> 
    <br>

<details>
  <summary><h3 style="display: inline-block;">화면 설계서</h3></summary>
  [바로가기] https://www.figma.com/design/ZFiVUIj6o5Xbcj5I0pOCky/%EC%B5%9C%EC%A2%85-%ED%94%84%EB%A1%9C%EC%A0%9D%ED%8A%B8?node-id=0-1&p=f&t=aJuRvgQ6vPVidrht-0
  </details>
  <br>

<details>
  <summary><h3 style="display: inline-block;">시스템 아키텍쳐 구조도</h3></summary>
  
  <img width="1823" height="1881" alt="최종프로젝트_스무고객_시스템아키텍쳐 drawio (3)" src="https://github.com/user-attachments/assets/9fa479fe-1ce8-4a75-aeef-9df74c6d2d46" />
  </details>
  <br>


<details>
  <summary><h3 style="display: inline-block;">프로그램 사양서(API명세서)</h3></summary>
  
  [프로그램 사양서(API명세서)](https://github.com/final-project-4team/gaekdam-be/wiki/API-%EB%AA%85%EC%84%B8%EC%84%9C)
</details>
  <br>

  <details>
  <summary><h3 style="display: inline-block;">단위 테스트 결과서</h3></summary>
    
  [단위 테스트 결과서](https://github.com/final-project-4team/gaekdam-be/wiki/%EB%8B%A8%EC%9C%84-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EA%B2%B0%EA%B3%BC%EC%84%9C)
  </details>
  <br>

<details>
<summary><h3 style="display: inline-block;">UI/UX 단위 테스트 결과서</h3></summary>
  
  [UI/UX 단위 테스트 결과서](https://github.com/final-project-4team/gaekdam-be/wiki/UI-UX-%EB%8B%A8%EC%9C%84-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EA%B2%B0%EA%B3%BC%EC%84%9C)
</details>
  <br>

<details>
<summary><h3 style="display: inline-block;">통합 테스트 결과서</h3></summary>
  
  [통합 테스트 결과서](https://github.com/final-project-4team/gaekdam-be/wiki/%ED%86%B5%ED%95%A9-%ED%85%8C%EC%8A%A4%ED%8A%B8-%EA%B2%B0%EA%B3%BC%EC%84%9C)
</details>
  <br>

<details>
<summary><h3 style="display: inline-block;">CI/CD 계획서</h3></summary>
    
[ 객담 CI/CD 계획서 상세보기](https://github.com/final-project-4team/gaekdam-be/blob/sub_develop/%EA%B0%9D%EB%8B%B4_CICD_%EA%B3%84%ED%9A%8D%EC%84%9C.pdf)
  </details>
  <br>


## 🤝 6. 팀원 회고

| 이름 | 회고 |
| --- | --- |
| <div align="center">김<br/>성<br/>태</div> | 이번 프로젝트를 하면서 “일단 구현”보다 먼저, 실제 운영을 전제로 흐름과 기준을 잡는 일이 결국 더 빠르고 안정적인 결과로 이어진다는 걸 확실히 느꼈습니다.<br/>초반에 요구사항을 정리하고 팀이 같은 기준으로 판단할 수 있도록 규칙과 형태를 맞췄고, 변경이 생겨도 혼선이 줄어들도록 작업 범위와 우선순위를 공유하며 조율했습니다.<br/>개발 과정에서는 Vitest 단위 테스트와 Testcontainers 기반 통합 테스트로 기능과 흐름을 검증했고, GitHub Actions로 PR 머지 전에 자동으로 검사되게 구성해 머지 전에 문제를 미리 걸러낼 수 있게 했습니다.<br/>또한 이슈가 생기면 감으로 밀기보다 로그와 근거를 바탕으로 상황을 정리해 공유하고, 원인을 좁힌 뒤 해결과 재발 방지까지 연결하는 방식으로 팀 내 커뮤니케이션을 맞춰갔습니다.<br/>결과적으로 단순히 기능을 많이 만드는 것보다, 함께 이해하고 같은 방향으로 움직일 수 있는 구조와 소통이 프로젝트 속도와 품질을 동시에 만든다는 걸 크게 배웠습니다. |
| <div align="center">김<br/>상<br/>재</div> | 내용 입력<br/> |
| <div align="center">김<br/>성<br/>현</div> | 내용 입력<br/> |
| <div align="center">박<br/>인<br/>수</div> | 내용 입력<br/> |

<br>

## ⚠️ 7. Trouble Shooting
  <details> <summary>1) GitHub Actions 오토머지가 “Waiting for status checks”에서 멈춤</summary>

### 증상
PR에 Auto-merge를 걸어도 머지가 진행되지 않고 Waiting for status checks 상태로 계속 대기

### 원인 분석

머지 조건으로 걸린 필수 체크(checks) 와 실제 워크플로우에서 실행되는 job 이름이 달라 “체크 통과”로 인식되지 않는 케이스
또는 develop 전용 auto-merge workflow가 main까지 영향을 주는 형태로 동작하여, 브랜치 정책이 의도와 다르게 묶이는 케이스

### 해결

브랜치별로 워크플로우 트리거를 명확히 분리
pull_request.branches: ["develop"] 처럼 대상 브랜치 제한
필수 체크로 등록된 job 이름과 실제 job 이름을 일치시키고, CI 흐름을 unit-test → integration-test로 고정

### 검증

PR 생성 → Actions에서 unit/integration 순차 실행 → 모두 성공 시 Auto-merge가 즉시 수행되는 것을 확인

### 재발 방지

브랜치별 워크플로우 파일 분리(또는 조건문 명확화) + 필수 체크 항목을 CI job 이름과 1:1로 관리
  </details> 

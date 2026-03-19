# daggu-frontend
project daggu's frontend repo
📝 jju-daggu 프로젝트 개요
**다꾸메이커(Diary Sticker Assistant)**는 사용자의 소중한 기록이 담긴 다이어리에서 텍스트를 추출하고, 감정을 분석하여 세상에 하나뿐인 커스텀 스티커를 생성해 주는 AI 서비스입니다.

📅 전체 개발 일정
기간: 2026.03.20 ~ 2026.05.15

주요 마일스톤:

3월 말: 요구사항 정의 및 UI/UX 설계 완료

4월 중순: OCR 및 이미지 생성 API 연동 완료

5월 초: 프론트/백엔드 통합 및 테스트

5월 중순: 최종 발표 및 배포

🚀 협업 및 커밋 가이드라인
우리 팀의 효율적인 개발을 위해 아래와 같은 규칙을 권장합니다.

1. 레포지토리 구성
daggu-frontend: React/Next.js 기반의 사용자 화면 구성

daggu-backend: Python/FastAPI 기반의 OCR 및 AI 모델 서빙

2. 커밋 메시지 규칙 (Commit Convention)
커밋 메시지 서두에 아래 태그를 붙여 어떤 작업인지 명확히 표시합니다.

feat: 새로운 기능 추가

fix: 버그 수정

docs: 리드미 등 문서 수정

design: UI 레이아웃 및 디자인 변경 (Figma 반영 등)

refactor: 코드 리팩토링 (기능 변경 없음)

예시: feat: Google Cloud Vision API를 활용한 OCR 기능 구현

3. 역할 분담 및 워크플로우
프론트엔드 (3인): UI 컴포넌트 개발, Figma 프로토타입 구현, API 데이터 바인딩

백엔드: Gemini API 연동(감정 분석), 이미지 생성 API(스티커 제작), 데이터베이스 설계

🛠 Tech Stack
Design: Figma

Frontend: React / TypeScript

Backend: Python / FastAPI / Docker

AI/ML: Google Cloud Vision (OCR), Gemini (LLM), Image Generation API

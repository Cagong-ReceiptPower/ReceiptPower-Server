### AI파트에서 필요한 파일은 이제 embedding_receipts.py, naver_ocr.py, receipt_embedding_model.pth 뿐입니다. 나머지는 이전 버전임.

## 설치해야하는 것
1. pip install python-multipart
이게 필요했습니다.

실행 시 오류가 일어날 수도 있는데, 나머지는 오류메세지 보고 설치하면 될 것 같네요.
<br />
<br />
<br />

## embedding_receipts.py 파일 설명
이 코드는 사전학습된 ResNet 기반 ReceiptEmbeddingModel을 이용하여, 업로드된 영수증 이미지를 카페 프로필 임베딩과 비교해 내 카페 영수증인지 판별하는 FastAPI 서버입니다.
또한, 카페 프로필 등록 시 3장 이상의 영수증 이미지와 augmentation을 적용해 평균 임베딩을 생성하고 JSON 파일로 저장할 수 있습니다.

### 주요 기능
1. **카페 프로필 등록(/register_profile)**
최소 3장 이상의 영수증 이미지 업로드 필요. 업로드된 각 이미지에 대해 데이터 증강까지 해서 이미지 임베딩 계산
2. **영수증 판별(/predict_receipt)**
단일 영수증 이미지 업로드. 등록된 카페 프로필(json)과 코사인 유사도 계산함. 임계값 이상이면 내 카페 영수증으로 판단함.

### 동작 방식
1. **클라이언트에서 프로필 등록**
  * Flutter 앱 등에서 POST /register_profile 요청 시:
    * cafe_name (Form)
    * 최소 3장 이상의 이미지 파일 리스트 (files)
  * 임베딩의 평균을 json 파일로 저장

2. 단일 영수증 판별
  * Flutter 앱 등에서 POST /predict_receipt 요청 시:
    * cafe_name (Form, 등록 시와 동일)
    * 단일 이미지 파일 (file)
  * 서버는 업로드된 이미지 임베딩 계산
  * json에 저장된 카페 프로필과 코사인 유사도 계산

### 주의사항
* /register_profile 호출 전에 카페별로 최소 3장 이상의 이미지 업로드 필요

* JSON 파일 경로는 {cafe_name}_profile.json으로 자동 저장

* 판별 시 /predict_receipt에서 동일한 cafe_name 사용 필요

* 모델 파일 receipt_embedding_model.pth가 같은 디렉토리에 있어야 함


<br />
<br />
<br />

## receipt_embedding_model.pth 파일 설명

* embedding_receipts.py에서 사용하는 사전학습된 ResNet 기반 모델 가중치 파일

* 모델 구조와 가중치가 모두 포함되어 있음

* 이 파일이 없으면 /register_profile이나 /predict_receipt 모두 실행 불가

<br />
<br />
<br />

## naver_ocr.py 파일 설명

이 코드는 **네이버 Clova OCR**을 이용하여 영수증 이미지에서 필요한 정보를 추출하고, JSON 형식으로 반환하는 FastAPI 서버입니다.

### 주요 기능

영수증 이미지로부터 다음 3가지 요소를 추출합니다:

1. **주문번호**
2. **메뉴목록 (메뉴명 + 가격)**
3. **총결제금액**

### 동작 방식

1. **클라이언트에서 이미지 업로드**

   * Flutter 앱 등에서 `POST /ocr` 요청 시, 영수증 이미지 파일을 전송합니다.
   * 서버는 이미지를 `temp/` 폴더에 임시 저장합니다.

2. **네이버 Clova OCR 호출**

   * 저장된 이미지를 네이버 Clova OCR API로 전송합니다.
   * 이때 `secret_key`는 **팀 노션에 기록된 값**을 코드에 반영해야 합니다.

     ```python
     secret_key = "---노션에 적어 놓음---"
     ```
   * `api_url`은 네이버 클라우드 콘솔에서 발급받은 OCR API의 실제 URL을 사용합니다.

3. **OCR 결과 파싱**

   * 응답으로 받은 텍스트 중에서 \*\*불필요한 단어(부가세, POS, BILL 등)\*\*를 제외합니다.
   * 주문번호, 메뉴명과 가격 쌍, 총 결제 금액만 남겨 JSON 형태로 반환합니다.

4. **JSON 응답 반환**

   * 최종적으로 FastAPI 서버는 다음과 같은 JSON을 반환합니다:

     ```json
     {
       "주문번호": "123",
       "메뉴목록": [
         {"이름": "아메리카노", "가격": "4,900"},
         {"이름": "카페라떼", "가격": "5,400"}
       ],
       "총결제금액": "10,300"
     }
     ```

### 실행 전 주의사항

* **Step 3**에서 `secret_key` 값을 반드시 팀 노션에 기록된 실제 값으로 교체해야 합니다.
* `api_url`은 네이버 클라우드 OCR API에서 발급받은 프로젝트 전용 URL을 입력해야 합니다.
* 로컬 실행 시 다음 명령어로 FastAPI 서버를 실행할 수 있습니다:

  ```bash
  uvicorn naver_ocr:app --reload
  ```
* 서버는 기본적으로 `http://127.0.0.1:8000`에서 실행되며, `/ocr` 엔드포인트를 통해 테스트할 수 있습니다.

실행 전에 코드의 step3 부분에 secret_key를 알맞게 써넣어야 하고, 그리고 image_file에 사진의 위치가 들어가야 합니다.

naver_ocr.py는 naver clova ocr을 이용해서 영수증 사진으로 부터 필요한 요소만 뽑아서 return해주는 코드입니다.
1. 주문번호
2. 메뉴목록
3. 총결제금액
이렇게 3가지 뽑아서 json형식으로 return해줍니다.

실행하고 127.0.0.1:8000 에 들어가면 화면에 뽑아주는 걸 확인할 수 있습니다. 
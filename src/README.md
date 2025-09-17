## 설치해야하는 것
1. pip install python-multipart
이게 필요했습니다.

실행 시 오류가 일어날 수도 있는데, 나머지는 오류메세지 보고 설치하면 될 것 같네요.

## naver_ocr.py 파일 설명
실행 전에 코드의 step3 부분에 secret_key를 알맞게 써넣어야 하고, 그리고 image_file에 사진의 위치가 들어가야 합니다.

naver_ocr.py는 naver clova ocr을 이용해서 영수증 사진으로 부터 필요한 요소만 뽑아서 return해주는 코드입니다.
1. 주문번호
2. 메뉴목록
3. 총결제금액
이렇게 3가지 뽑아서 json형식으로 return해줍니다.

실행하고 127.0.0.1:8000 에 들어가면 화면에 뽑아주는 걸 확인할 수 있습니다. 

## identify_receipts.py 파일 설명
이 파일이 실행되려면 my_cafe_cnn_state_dict.pth이 있어야 합니다.

identify_receipts.py는 사전학습된 cnn을 이용해서 영수증이 카페나무 영수증인지 아닌지 판단해주는 코드입니다.

실행방법은 다음과 같습니다.
1. 실행하고 127.0.0.1:8000/docs에 접속합니다.
2. /predict POST 엔드포인트를 클릭합니다.
3. Try it out -> File(choose file) 버튼 클릭해서 파일 선택
4. Excute해서 실행
그러면 해당 사진 판단 결과가 출력됩니다.

## my_cafe_cnn_state_dict.pth 파일 설명
이 파일은 identify_receipts.py 파일을 실행하는데 꼭 필요합니다.

이게 사전학습된 cnn 모델의 모든 정보를 담고 있기 때문에 그 모델을 불러와서 사용하려면 필요한 거죠.

그래서 이걸 다운로드하고 identify_receipts.py를 실행해야 됩니다.
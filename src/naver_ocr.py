import requests
import json
import re
from fastapi import FastAPI

app = FastAPI()

# ========== STEP 1: 이미지 파일 OCR 요청 ==========
def call_clova_ocr(image_path, api_url, secret_key):
    headers = {"X-OCR-SECRET": secret_key}
    data = {
        "message": json.dumps({
            "version": "V2",
            "requestId": "unique-request-id",
            "timestamp": 0,
            "images": [
                {"name": "receipt", "format": "jpg", "data": ""}
            ],
        })
    }

    with open(image_path, "rb") as f:
        files = {"file": f}
        response = requests.post(api_url, headers=headers, data=data, files=files, timeout=10)

    result = response.json()
    # 안전하게 접근
    try:
        return result["images"][0]["fields"]
    except (KeyError, IndexError):
        raise ValueError(f"OCR 응답 오류: {result}")

# ========== STEP 2: 텍스트 필드 파싱 함수 ==========
def parse_receipt_text(fields):
    lines = [field["inferText"].strip() for field in fields if field["inferText"].strip()]

    menu_price_pairs = []
    order_number = None
    total_amount = None

    ignore_keywords = [
        "주문", "대기", "번호", "POS", "BILL", "과세", "부가세", "합계", "받을금액",
        "받은금액", "카드결제", "금액", "세", "계", "현금", "결제", "KICC", "전표", "고객용",
        "매출전표", "CASHIER", "물품가액", "1", "0", "4900", "L-Regular", "LI.C.E", "ICE", "*", "[용]", "L Regular"
    ]

    i = 0
    while i < len(lines):
        line = lines[i]

        # 주문번호
        if line.upper() == "BILL:" and i + 1 < len(lines):
            if re.fullmatch(r"\d+", lines[i + 1]):
                order_number = lines[i + 1]

        # 메뉴 + 가격
        if re.fullmatch(r"\d{1,3}(,\d{3})*", line):
            if i > 0:
                menu_candidate = lines[i - 1]
                if not any(keyword in menu_candidate for keyword in ignore_keywords):
                    menu_price_pairs.append((menu_candidate, line))

        # 총 금액
        if "받을금액" in line or "합계" in line:
            if i + 1 < len(lines):
                amount = lines[i + 1]
                if re.fullmatch(r"\d{1,3}(,\d{3})*", amount):
                    total_amount = amount

        i += 1

    return {
        "order_number": order_number,
        "menu_items": menu_price_pairs,
        "total_amount": total_amount,
    }

# ========== STEP 3: FastAPI 엔드포인트 ========== 여기에 secret_key를 알맞게 써넣어야함.
@app.get("/")
def read_receipt():
    api_url = "https://a4dyolhyl2.apigw.ntruss.com/custom/v1/40636/e9a63827526c61487a35ffaa58677583b4b95176dd8359fdd514d361674b7a65/general"
    secret_key = "---노션에 적어 놓음---"
    image_file = "C:/Users/junie/Downloads/Quick Share/20250522_130512.jpg"

    try:
        fields = call_clova_ocr(image_file, api_url, secret_key)
        parsed = parse_receipt_text(fields)
        return {
            "주문번호": parsed["order_number"] or "없음",
            "메뉴목록": [{"이름": n, "가격": p} for n, p in parsed["menu_items"]],
            "총결제금액": parsed["total_amount"] or "없음",
        }
    except Exception as e:
        return {"error": str(e)}

from fastapi import FastAPI, UploadFile, File, HTTPException
from PIL import Image, ImageEnhance
import torch
import torch.nn as nn
import torchvision.models as models
import torchvision.transforms as transforms
import torch.nn.functional as F
import json
import io
from pathlib import Path
import random

app = FastAPI()

# ----------------------------
# 1️⃣ ReceiptEmbeddingModel 정의
# ----------------------------
class ReceiptEmbeddingModel(nn.Module):
    def __init__(self, embedding_dim=128):
        super().__init__()
        base = models.resnet18(pretrained=False)
        num_ftrs = base.fc.in_features
        base.fc = nn.Identity()
        self.base = base
        self.embedding = nn.Linear(num_ftrs, embedding_dim)

    def forward(self, x):
        x = self.base(x)
        x = self.embedding(x)
        x = F.normalize(x, p=2, dim=1)
        return x

# ----------------------------
# 2️⃣ 모델 로드
# ----------------------------
model_path = Path(__file__).parent / "receipt_embedding_model.pth"
model = ReceiptEmbeddingModel(embedding_dim=128)
model.load_state_dict(torch.load(model_path, map_location="cpu"))
model.eval()

# ----------------------------
# 3️⃣ 이미지 전처리
# ----------------------------
transform = transforms.Compose([
    transforms.Resize((224, 224)),
    transforms.ToTensor(),
    transforms.Normalize(mean=[0.485, 0.456, 0.406],
                         std=[0.229, 0.224, 0.225])
])

# ----------------------------
# 4️⃣ JSON 저장 경로
# ----------------------------
profile_json_path = Path(__file__).parent / "my_cafe_profile.json"

# ----------------------------
# 5️⃣ Augmentation 함수
# ----------------------------
def augment_image(image: Image.Image):
    # 좌우/상하 반전
    if random.random() < 0.5:
        image = image.transpose(Image.FLIP_LEFT_RIGHT)
    if random.random() < 0.5:
        image = image.transpose(Image.FLIP_TOP_BOTTOM)
    # 밝기 약간 조절
    if random.random() < 0.5:
        enhancer = ImageEnhance.Brightness(image)
        factor = random.uniform(0.9, 1.1)
        image = enhancer.enhance(factor)
    # 대비 약간 조절
    if random.random() < 0.5:
        enhancer = ImageEnhance.Contrast(image)
        factor = random.uniform(0.9, 1.1)
        image = enhancer.enhance(factor)
    return image

# ----------------------------
# 6️⃣ /register_profile : 3장 이상 업로드 + augmentation
# ----------------------------
@app.post("/register_profile")
async def register_profile(files: list[UploadFile] = File(...)):
    if len(files) < 3:
        raise HTTPException(status_code=400, detail="최소 3장 이상의 이미지가 필요합니다.")

    embeddings = []
    with torch.no_grad():
        for file in files:
            contents = await file.read()
            image = Image.open(io.BytesIO(contents)).convert("RGB")
            
            # 원본 임베딩
            img_tensor = transform(image).unsqueeze(0)
            embeddings.append(model(img_tensor).squeeze(0))
            
            # augmentation 2~3번 적용
            for _ in range(2):
                aug_img = augment_image(image)
                aug_tensor = transform(aug_img).unsqueeze(0)
                embeddings.append(model(aug_tensor).squeeze(0))
    
    # 평균 임베딩 계산
    mean_embedding = torch.stack(embeddings).mean(dim=0)
    mean_embedding_list = mean_embedding.tolist()

    # JSON 저장
    with open(profile_json_path, "w") as f:
        json.dump({"mean_embedding": mean_embedding_list}, f)

    return {"message": f"✅ {len(files)}장 + augmentation으로 평균 임베딩 저장 완료",
            "json_path": str(profile_json_path)}

# ----------------------------
# 7️⃣ /predict_receipt : 업로드된 단일 영수증 → 유사도 계산
# ----------------------------
@app.post("/predict_receipt")
async def predict_receipt(file: UploadFile = File(...), threshold: float = 0.98):
    if not profile_json_path.exists():
        raise HTTPException(status_code=400, detail="프로필이 등록되지 않았습니다. /register_profile 먼저 호출하세요.")

    # JSON에서 평균 임베딩 로드
    with open(profile_json_path, "r") as f:
        data = json.load(f)
    profile_embedding = torch.tensor(data["mean_embedding"])
    profile_embedding = F.normalize(profile_embedding, p=2, dim=0)

    # 업로드된 이미지 처리
    contents = await file.read()
    image = Image.open(io.BytesIO(contents)).convert("RGB")
    image = transform(image).unsqueeze(0)

    with torch.no_grad():
        new_emb = model(image).squeeze(0)
    
    similarity = F.cosine_similarity(new_emb, profile_embedding, dim=0).item()
    is_my_cafe = similarity >= threshold

    return {
        "similarity": round(similarity, 4),
        "label": "my_cafe" if is_my_cafe else "other_cafe",
        "threshold": threshold
    }

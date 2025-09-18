from fastapi import FastAPI, UploadFile, File
from PIL import Image
import torch
import torch.nn as nn
import torchvision.transforms as transforms
import io
from pathlib import Path

app = FastAPI()

# ===== 모델 정의 =====
image_size = 224

class SimpleCNN(nn.Module):
    def __init__(self):
        super(SimpleCNN, self).__init__()
        self.net = nn.Sequential(
            nn.Conv2d(3, 16, 3, padding=1), nn.ReLU(), nn.MaxPool2d(2),
            nn.Conv2d(16, 32, 3, padding=1), nn.ReLU(), nn.MaxPool2d(2),
            nn.Conv2d(32, 64, 3, padding=1), nn.ReLU(), nn.MaxPool2d(2),
            nn.Flatten(),
            nn.Linear(64 * (image_size // 8) * (image_size // 8), 128), nn.ReLU(),
            nn.Linear(128, 1)
        )

    def forward(self, x):
        return self.net(x)

# ===== 모델 불러오기 =====
model_path = Path(__file__).parent / "my_cafe_cnn_state_dict.pth"
model = SimpleCNN()
model.load_state_dict(torch.load(model_path, map_location=torch.device("cpu")))
model.eval()

# ===== 이미지 전처리 =====
transform = transforms.Compose([
    transforms.Resize((image_size, image_size)),
    transforms.ToTensor(),
    transforms.Normalize([0.5]*3, [0.5]*3)
])

@app.post("/predict")
async def predict(file: UploadFile = File(...)):
    # 업로드된 파일 → PIL 이미지 변환
    contents = await file.read()
    image = Image.open(io.BytesIO(contents)).convert("RGB")

    # 전처리
    image = transform(image).unsqueeze(0)  # (1, 3, 224, 224)

    # 예측
    with torch.no_grad():
        output = model(image)
        prob = torch.sigmoid(output).item()

    prediction = 1 if prob > 0.5 else 0  # 1 = 내 카페, 0 = 다른 카페

    result = {
        "probability": prob,
        "label": "my_cafe" if prediction == 1 else "other_cafe"
    }
    return result


from fastapi import FastAPI
from fastapi.middleware.cors import CORSMiddleware
from fastapi.staticfiles import StaticFiles
from routers.api import router as api_router
from routers.admin import router as admin_router
from models import init_db
import os

app = FastAPI(title="Fitness App API", version="1.0.0")

app.add_middleware(
    CORSMiddleware,
    allow_origins=["*"],
    allow_credentials=True,
    allow_methods=["*"],
    allow_headers=["*"],
)

app.include_router(api_router)
app.include_router(admin_router)

# Serve admin panel static files
static_dir = os.path.join(os.path.dirname(__file__), "static", "admin")
if os.path.exists(static_dir):
    app.mount("/admin/static", StaticFiles(directory=static_dir), name="admin_static")

@app.get("/admin")
@app.get("/admin/{rest_of_path:path}")
async def serve_admin():
    from fastapi.responses import FileResponse
    admin_html = os.path.join(os.path.dirname(__file__), "static", "admin", "index.html")
    return FileResponse(admin_html)

@app.on_event("startup")
def startup():
    init_db()
    print("Fitness App API started!")

@app.get("/")
def root():
    return {"message": "Fitness App API is running"}

if __name__ == "__main__":
    import uvicorn
    uvicorn.run(app, host="0.0.0.0", port=8000)

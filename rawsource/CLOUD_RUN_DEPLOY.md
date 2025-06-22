# Deploy a Google Cloud Run

## Configuración Lista para Cloud Run

### ✅ **Cambios Realizados:**

#### **Dockerfile:**
- Usuario no-root (requerimiento de Cloud Run)
- Variable `PORT` configurada
- Health check compatible
- Entrypoint optimizado para Cloud Run

#### **compose.yaml:**
- Configuración simplificada
- Variables de entorno preparadas
- Puerto configurado para Cloud Run

#### **cloudbuild.yaml:**
- Build automatizado
- Deploy a Cloud Run
- Variables de entorno configuradas

## 🚀 **Pasos para Deploy:**

### 1. **Configurar Google Cloud:**
```bash
# Instalar Google Cloud SDK
gcloud auth login
gcloud config set project YOUR_PROJECT_ID
gcloud services enable cloudbuild.googleapis.com
gcloud services enable run.googleapis.com
```

### 2. **Deploy Manual:**
```bash
# Construir imagen
docker build -t gcr.io/YOUR_PROJECT_ID/rawsource-app .

# Subir a Container Registry
docker push gcr.io/YOUR_PROJECT_ID/rawsource-app

# Deploy a Cloud Run
gcloud run deploy rawsource-app \
  --image gcr.io/YOUR_PROJECT_ID/rawsource-app \
  --platform managed \
  --region us-central1 \
  --allow-unauthenticated \
  --port 8190 \
  --memory 512Mi \
  --cpu 1 \
  --max-instances 10
```

### 3. **Deploy Automatizado (Recomendado):**
```bash
# Conectar repositorio a Cloud Build
gcloud builds submit --config cloudbuild.yaml
```

## 🔧 **Configuración de Variables de Entorno:**

Las variables están configuradas en `cloudbuild.yaml`:
- Base de datos: `34.59.103.55:5432`
- JWT Secret configurado
- Rate limiting configurado
- Logging configurado

## 📊 **Recursos Asignados:**
- **Memoria**: 512Mi
- **CPU**: 1 vCPU
- **Máximo de instancias**: 10
- **Puerto**: 8190

## 🔒 **Seguridad:**
- Usuario no-root
- Variables de entorno seguras
- Health checks configurados

## 🌐 **URL de Acceso:**
Después del deploy, Cloud Run proporcionará una URL como:
`https://rawsource-app-xxxxx-uc.a.run.app`

## 📝 **Notas Importantes:**
1. **Base de datos externa**: Asegúrate de que `34.59.103.55` sea accesible desde Cloud Run
2. **SSL**: El keystore debe estar en `src/main/resources/keystore.p12`
3. **Variables sensibles**: Considera usar Secret Manager para contraseñas en producción 
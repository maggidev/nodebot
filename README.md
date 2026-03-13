# WhatsApp Bot Container (Kotlin + Node.js Nativo)

Este projeto é um aplicativo Android nativo que encapsula um runtime Node.js para rodar um bot de WhatsApp baseado na biblioteca Baileys.

## Estrutura do Projeto

- **app/**: Código fonte Android (Kotlin) e recursos de UI (Material 3).
- **app/src/main/assets/nodejs-project/**: Backend Node.js que será extraído e executado no Android.
- **.github/workflows/android.yml**: Workflow para CI/CD no GitHub Actions.

## Como Usar

1. **Binários do Node.js**:
   - Você deve baixar os binários do Node.js para Android (arquiteturas `armeabi-v7a` e `arm64-v8a`).
   - Renomeie o binário para `node` e coloque-o dentro de `app/src/main/assets/nodejs-project/`.
   - Certifique-se de que o binário é compatível com a arquitetura do seu dispositivo (ex: J7 Neo usa `armeabi-v7a`).

2. **Build no Android Studio**:
   - Abra a pasta raiz no Android Studio.
   - Aguarde a sincronização do Gradle.
   - Conecte seu dispositivo e clique em "Run".

3. **Uso do App**:
   - Ao abrir, clique em **"Iniciar Bot"**.
   - O app extrairá os arquivos do Node.js para o armazenamento interno e iniciará o processo.
   - Escolha entre **QR Code** ou **Código de Pareamento**.
   - Acompanhe os logs em tempo real no console inferior.

## Requisitos de Resiliência

- O app utiliza um **Foreground Service** com notificação persistente.
- Implementa **WakeLock** para evitar que o processador entre em modo de espera.
- **Dica**: Desative a "Otimização de Bateria" para este app nas configurações do Android para garantir que o sistema não mate o processo em segundo plano.

## GitHub Actions

Ao subir este repositório para o GitHub, o workflow configurado irá:
1. Instalar as dependências do Node.js.
2. Compilar o APK de debug.
3. Disponibilizar o APK como um artefato na aba "Actions".

---
Desenvolvido por **Manus AI**

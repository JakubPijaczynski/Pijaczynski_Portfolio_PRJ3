# Portfolio push backend

Small LoopBack 4 API that sends Firebase push notifications to the portfolio Android app.

## Setup

1. Firebase console > Project settings > **Service accounts** > **Generate new private key**.
2. Save the downloaded file as `backend/firebase-key.json`
   (it is in `.gitignore` - never commit it, it is a secret).
3. Install and start:

```sh
npm install
npm start
```

Open http://localhost:3000/explorer to try the endpoints.

## Endpoints

| Method | Path | Body | Sends to |
| ------ | ---- | ---- | -------- |
| POST | `/notifications/topic` | `{"topic": "portfolio", "title": "...", "body": "..."}` | every device subscribed to the topic (the app subscribes to `portfolio`) |
| POST | `/notifications/device` | `{"token": "...", "title": "...", "body": "..."}` | one device (FCM token, logged by the app under the `FCM` tag) |

## Tests

```sh
npm test
```

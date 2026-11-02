# Portfolio push backend

Small LoopBack 4 API for the portfolio Android app: it stores saved places (CRUD, used by the app via Retrofit) and sends Firebase push notifications.

Data is kept in an in-memory database that is saved to `db.json` (no database server needed).

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
| GET | `/places` | - | list all saved places |
| GET | `/places/{id}` | - | one place |
| POST | `/places` | `{"name": "...", "description": "...", "latitude": 51.35, "longitude": 6.15}` | create a place |
| PUT | `/places/{id}` | same as POST | replace a place |
| DELETE | `/places/{id}` | - | delete a place |
| POST | `/notifications/device` | `{"token": "...", "title": "...", "body": "..."}` | one device (FCM token, logged by the app under the `FCM` tag) |

## Tests

```sh
npm test
```

import {BindingScope, injectable} from '@loopback/core';
import {HttpErrors} from '@loopback/rest';
import {App, cert, initializeApp} from 'firebase-admin/app';
import {getMessaging, Messaging} from 'firebase-admin/messaging';
import fs from 'fs';
import path from 'path';

/**
 * Sends push notifications through Firebase Cloud Messaging (FCM).
 *
 * Needs the Firebase service account key (the "private key" JSON from
 * Firebase console > Project settings > Service accounts).
 * By default it is read from `firebase-key.json` in the backend folder,
 * or from the path in the FIREBASE_KEY_PATH environment variable.
 * The key is a secret: it is listed in .gitignore and must never be committed.
 */
@injectable({scope: BindingScope.SINGLETON})
export class NotificationService {
  private firebaseApp?: App;

  // Initialise Firebase only when the first notification is sent,
  // so the API still starts (and shows a clear error) without a key.
  private messaging(): Messaging {
    if (!this.firebaseApp) {
      const keyPath = path.resolve(
        process.env.FIREBASE_KEY_PATH ?? 'firebase-key.json',
      );
      if (!fs.existsSync(keyPath)) {
        const error = new HttpErrors.InternalServerError(
          `Firebase key not found at ${keyPath}. Download it from Firebase console > Project settings > Service accounts.`,
        );
        error.expose = true; // LoopBack hides 5xx messages by default, show this one
        throw error;
      }
      const serviceAccount = JSON.parse(fs.readFileSync(keyPath, 'utf8'));
      this.firebaseApp = initializeApp({credential: cert(serviceAccount)});
    }
    return getMessaging(this.firebaseApp);
  }

  /** Send to one device, identified by its FCM registration token. */
  async sendToDevice(
    token: string,
    title: string,
    body: string,
  ): Promise<string> {
    return this.messaging().send({token, notification: {title, body}});
  }

  /** Send to every device subscribed to a topic (the app subscribes to "portfolio"). */
  async sendToTopic(
    topic: string,
    title: string,
    body: string,
  ): Promise<string> {
    return this.messaging().send({topic, notification: {title, body}});
  }
}

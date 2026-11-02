import {Client, expect} from '@loopback/testlab';
import {BackendApplication} from '../..';
import {setupApplication} from './test-helper';

describe('NotificationController', () => {
  let app: BackendApplication;
  let client: Client;

  before('setupApplication', async () => {
    // Point to a key that does not exist, so no real notification is sent
    process.env.FIREBASE_KEY_PATH = 'does-not-exist.json';
    ({app, client} = await setupApplication());
  });

  after(async () => {
    await app.stop();
    delete process.env.FIREBASE_KEY_PATH;
  });

  it('rejects a topic message without a title', async () => {
    await client
      .post('/notifications/topic')
      .send({topic: 'portfolio', body: 'Hello'})
      .expect(422);
  });

  it('rejects a device message without a token', async () => {
    await client
      .post('/notifications/device')
      .send({title: 'Hi', body: 'Hello'})
      .expect(422);
  });

  it('returns a clear error when the Firebase key is missing', async () => {
    const res = await client
      .post('/notifications/topic')
      .send({title: 'Hi', body: 'Hello'})
      .expect(500);
    expect(res.body.error.message).to.match(/Firebase key not found/);
  });
});
